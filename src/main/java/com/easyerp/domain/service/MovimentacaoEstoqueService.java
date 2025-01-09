package com.easyerp.domain.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easyerp.config.ModelMapper;
import com.easyerp.domain.entidade.Estoque;
import com.easyerp.domain.entidade.ItemMovimentacao;
import com.easyerp.domain.entidade.MovimentacaoEstoque;
import com.easyerp.domain.entidade.MovimentacaoEstoque.TipoMovimentacao;
import com.easyerp.domain.entidade.Produto;
import com.easyerp.domain.enumerados.TipoProduto;
import com.easyerp.domain.repository.MovimentoEstoqueRepository;
import com.easyerp.domain.repository.ProdutoRepository;
import com.easyerp.domain.service.exeption.RegistroNaoEncontrado;
import com.easyerp.model.dto.MovimentacaoResponse;
import com.easyerp.model.input.MovimentacaoInput;

import jakarta.transaction.Transactional;

@Service
public class MovimentacaoEstoqueService {
	@Autowired
	private MovimentoEstoqueRepository movimentoEstoqueRepository;
	@Autowired
	private ModelMapper movimentacaoEstoqueMapper;
	@Autowired
	private ProdutoRepository produtoRepository;

	@Transactional
	public MovimentacaoResponse registrarMovimentacao(MovimentacaoInput movimentacaoInput) {
		System.out.println ( "movimentacoa"+movimentacaoInput.idProduto());
		MovimentacaoEstoque movimentacaoEstoque = new MovimentacaoEstoque();
		movimentacaoEstoque.setDataMovimentacao(LocalDateTime.now());

		movimentacaoEstoque.setTipoMovimentacao(movimentacaoInput.tipoMovimentacao());

		Produto produto = new Produto();
		
		produto = produtoRepository.findById(movimentacaoInput.idProduto())
				.orElseThrow(() -> new RegistroNaoEncontrado("Produto não encontrado"));
            System.out.println(" produto"+produto.getProdutoNome());
		ItemMovimentacao item = new ItemMovimentacao();
		item.setProduto(produto);
		movimentacaoEstoque.getItens().add(item);
		movimentacaoEstoque.getItens().forEach(m -> m.setMovimentacao(movimentacaoEstoque));

		for (var ItemM : movimentacaoEstoque.getItens()) {
			for (var itemIp : movimentacaoInput.items()) {
				ItemM.setQuantidade(itemIp.qtde());

			}
			verificarMovimentacao(movimentacaoEstoque);
		}
		var movimetacaoSalva = movimentoEstoqueRepository.save(movimentacaoEstoque);
		return movimentacaoEstoqueMapper.converter(movimetacaoSalva, MovimentacaoResponse::new);
	}

	private void processarSaidaEstoque(ItemMovimentacao item) {
		Estoque estoque = item.getProduto().getEstoque();
		if (estoque == null) {

			estoque = new Estoque();
			estoque.setDataAlteracao(LocalDateTime.now());
			estoque.setDataCadastro(LocalDateTime.now());

			estoque.setProduto(item.getProduto());

			estoque.setQuantidade(BigDecimal.ZERO);

		}
		/// item.setSaldoAnterior(estoque.getQuantidade());
		estoque.setDataAlteracao(LocalDateTime.now());
		estoque.setQuantidade(estoque.getQuantidade().add(item.getQuantidade()));
		item.getProduto().setEstoque(estoque);
		if (item.getProduto().getTipoProduto().equals(TipoProduto.Kit)) {
			for (var produtoVariacao : item.getProduto().getVariacoes()) {
				produtoVariacao.setQtdeEstoque(produtoVariacao.calcularEstoque(produtoVariacao.getQtdeEstoque()));
			}
		}
		produtoRepository.save(item.getProduto());
	}

	private void processarEntradaEstoque(ItemMovimentacao item) {
		Estoque estoque = item.getProduto().getEstoque();
		if (estoque == null) {

			estoque = new Estoque();
			estoque.setDataAlteracao(LocalDateTime.now());
			estoque.setDataCadastro(LocalDateTime.now());

			estoque.setProduto(item.getProduto());

			estoque.setQuantidade(BigDecimal.ZERO);

		}

		estoque.setDataAlteracao(LocalDateTime.now());
		estoque.setQuantidade(estoque.getQuantidade().add(item.getQuantidade()));
		item.getProduto().setEstoque(estoque);
		if (item.getProduto().getTipoProduto().equals(TipoProduto.Kit)) {
			for (var produtoVariacao : item.getProduto().getVariacoes()) {
				produtoVariacao.setQtdeEstoque(produtoVariacao.calcularEstoque(produtoVariacao.getQtdeEstoque()));
			}
		}
		produtoRepository.save(item.getProduto());
	}

	private void verificarMovimentacao(MovimentacaoEstoque movimentacaoEstoque) {
		if (movimentacaoEstoque.getTipoMovimentacao().equals(TipoMovimentacao.Entrada)) {
			movimentacaoEstoque.getItens().forEach(this::processarEntradaEstoque);
		} else {
			movimentacaoEstoque.getItens().forEach(this::processarSaidaEstoque);
		}

	}

}
