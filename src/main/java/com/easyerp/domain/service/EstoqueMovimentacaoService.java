package com.easyerp.domain.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easyerp.config.ModelMapper;
import com.easyerp.domain.entidade.Estoque;
import com.easyerp.domain.entidade.MovimentacaoEstoque;
import com.easyerp.domain.entidade.Produto;
import com.easyerp.domain.enumerados.TipoMovimentacao;
import com.easyerp.domain.enumerados.TipoProduto;
import com.easyerp.domain.repository.MovimentoEstoqueRepository;
import com.easyerp.domain.repository.ProdutoRepository;
import com.easyerp.model.dto.MovimentacaoResponse;
import com.easyerp.model.input.MovimentacaoInput;

import jakarta.transaction.Transactional;

@Service
public class EstoqueMovimentacaoService {

	
	@Autowired
	private MovimentoEstoqueRepository movimentoEstoqueRepository;
	@Autowired
	private ModelMapper movimentacaoEstoqueMapper;
	@Autowired
	private ProdutoRepository produtoRepository;

	@Transactional
	public MovimentacaoResponse registroMovimentacao(MovimentacaoInput movimentacaoInput) {

		Produto produto = buscarProduto(movimentacaoInput.idProduto());

		MovimentacaoEstoque movimentacaoEstoque = movimentacaoEstoqueMapper.converter(movimentacaoInput,
				MovimentacaoEstoque::new);
		this.verificarMovimentacao(movimentacaoEstoque, produto, movimentacaoInput);
		MovimentacaoEstoque movimetacaoSalva = movimentoEstoqueRepository.save(movimentacaoEstoque);
		return movimentacaoEstoqueMapper.converter(movimetacaoSalva, MovimentacaoResponse::new);
	}

	private Produto buscarProduto(Long idProduto) {
		// TODO Auto-generated method stub
		return null;
	}

	private void verificarMovimentacao(MovimentacaoEstoque movimentacaoEstoque, Produto produto,
			MovimentacaoInput movimentacaoInput) {

		if (movimentacaoEstoque.getTipoMovimentacao().equals(TipoMovimentacao.Entrada)) {
			entradaEstoque(movimentacaoEstoque, movimentacaoInput, produto);

		} else {

			saidaEstoque(movimentacaoEstoque, movimentacaoInput, produto);
		}
	}

	private void saidaEstoque(MovimentacaoEstoque movimentacaoEstoque, MovimentacaoInput movimentacaoInput,
			Produto produto) {
		// TODO Auto-generated method stub
		
	}

	private void entradaEstoque(MovimentacaoEstoque movimentacaoEstoque, MovimentacaoInput movimentacaoInput,
			Produto produto) {
		if(produto.getTipoProduto().equals(TipoProduto.Kit)) {
			processarItemKit();
		}else {
		     processaItem();	
		}
		
	}

	private void processaItem() {
		// TODO Auto-generated method stub
		
	}

	private void processarItemKit(MovimentacaoEstoque movimentacaoEstoque, MovimentacaoInput movimentacaoInput,
			Produto produto) {
		Estoque estoque = new Estoque();
		estoque = atualizarQtdeTotal(produto, estoque);
		BigDecimal qteAnterior = estoque.getQuantidade();
		estoque.setQuantidade(estoque.getQuantidade().add(movimentacaoInput.qtdeProduto()));
		
	}
	private Estoque atualizarQtdeTotal(Produto produto, Estoque estoque) {
		if (produto.getEstoque() == null) {

			estoque = inicializarEstoque(produto);
		} else {
			estoque = produto.getEstoque();
		}

		return estoque;

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
