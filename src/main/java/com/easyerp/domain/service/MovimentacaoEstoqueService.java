package com.easyerp.domain.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easyerp.config.ModelMapper;
import com.easyerp.domain.entidade.Estoque;
import com.easyerp.domain.entidade.ItemMovimentacao;
import com.easyerp.domain.entidade.MovimentacaoEstoque;
import com.easyerp.domain.entidade.Produto;
import com.easyerp.domain.entidade.ProdutoVariacao;
import com.easyerp.domain.enumerados.TipoMovimentacao;
import com.easyerp.domain.enumerados.TipoProduto;
import com.easyerp.domain.repository.MovimentoEstoqueRepository;
import com.easyerp.domain.repository.ProdutoRepository;
import com.easyerp.domain.service.exeption.NegocioException;
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
		validarMovimentacaoInput(movimentacaoInput);
		MovimentacaoEstoque movimentacaoEstoque = movimentacaoEstoqueMapper.converter(movimentacaoInput,
				MovimentacaoEstoque::new);
		Produto produto = buscarProduto(movimentacaoInput.idProduto());

		 verificarMovimentacao(movimentacaoEstoque, produto, movimentacaoInput);

		MovimentacaoEstoque movimetacaoSalva = movimentoEstoqueRepository.save(movimentacaoEstoque);
		return movimentacaoEstoqueMapper.converter(movimetacaoSalva, MovimentacaoResponse::new);
	}

	private void verificarMovimentacao(MovimentacaoEstoque movimentacaoEstoque, Produto produto,
			MovimentacaoInput movimentacaoInput) {

		if (movimentacaoEstoque.getTipoMovimentacao().equals(TipoMovimentacao.Entrada)) {
			entradaEstoque(movimentacaoEstoque, movimentacaoInput, produto);

		} else {

			saidaEstoque(movimentacaoEstoque, movimentacaoInput, produto);
		}
	}

	private void validarMovimentacaoInput(MovimentacaoInput movimentacaoInput) {
		if (movimentacaoInput == null || movimentacaoInput.itens().isEmpty()) {
			throw new NegocioException("A movimentação deve conter ao menos um item.");
		}

		if (movimentacaoInput.idProduto() == null) {
			throw new NegocioException("ID do produto não pode ser nulo.");
		}
	}

	private Produto buscarProduto(Long produtoId) {
		return produtoRepository.findById(produtoId)
				.orElseThrow(() -> new RegistroNaoEncontrado("Produto não encontrado para o ID: " + produtoId));
	}

	private void entradaEstoque(MovimentacaoEstoque movimentacaoEstoque, MovimentacaoInput movimentacaoInput,
			Produto produto) {

	}

	private void saidaEstoque(MovimentacaoEstoque movimentacaoEstoque, MovimentacaoInput movimentacaoInput,
			Produto produto) {

	}

	private void atualizaVariacaoKit(Estoque estoque, BigDecimal quantidade, ItemMovimentacao item) {
		if ((item.getMovimentacao().getTipoMovimentacao().equals(TipoMovimentacao.Saida))&&  (item.getProdutoVariacao().getProduto().getTipoProduto().equals(TipoProduto.Kit))) {
			System.out.println(item.getProdutoVariacao().getQtdeEstoque() + "estoque variaçaos");
			
		for(var v : item.getProdutoVariacao().getProduto().getVariacoes()) {
			v.setQtdeEstoque(v.calcularEstoque(estoque.getQuantidade().intValue()));
				System.out.println(v.getQtdeEstoque() + "estoque variaçaos");
			}
		

			System.out.println(item.getProdutoVariacao().getQtdeEstoque() + "qtde saida");
		}
		
	}

	private void atualizaVariacao() {

	}
	
	private ProdutoVariacao buscarVariacao(Produto produto, Long idVariacao) {
		return produto.getVariacoes().stream().filter(v -> v.getId().equals(idVariacao)).findFirst()
				.orElseThrow(() -> new NegocioException("Variação não encontrada para o ID: " + idVariacao));
	}
	private ItemMovimentacao criarItemMovimentacao(MovimentacaoEstoque movimentacaoEstoque, BigDecimal saldoAnterior,
			ProdutoVariacao variacao, BigDecimal qtde, TipoMovimentacao tipoMovimentacao) {
		ItemMovimentacao item = new ItemMovimentacao();
		if (qtde.signum() != 0) {

			item.setMovimentacao(movimentacaoEstoque);
			item.setProdutoVariacao(variacao);

			item.setQuantidade((qtde));
			item.setSaldoanterior(saldoAnterior);
		}

		return item;
	}
	private Estoque inicializarEstoque(Produto produto) {
		Estoque estoque = new Estoque();
		estoque.setQuantidade(BigDecimal.ZERO);
		estoque.setDataAlteracao(LocalDateTime.now());
		estoque.setDataCadastro(LocalDateTime.now());
		estoque.setProduto(produto);
		produto.setEstoque(estoque);
		return estoque;
	}
}
