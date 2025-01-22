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
    private MovimentacaoInput movimento;
	@Transactional
	public MovimentacaoResponse registrarMovimentacao(MovimentacaoInput movimentacaoInput) {
	
	
	
		return null;
	}

	private void processarSaidaEstoque(ItemMovimentacao item) {
	 
	

		

		
		
	}

	private void processarEntradaEstoque(ItemMovimentacao item) {
		

		
	
		
	

	}

	private void verificarMovimentacao(MovimentacaoEstoque movimentacaoEstoque, MovimentacaoInput movimentacaoInput) {
		if (movimentacaoEstoque.getTipoMovimentacao().equals(TipoMovimentacao.Entrada)) {
			movimentacaoEstoque.getItens().forEach(this::processarEntradaEstoque);
		} else {
			movimentacaoEstoque.getItens().forEach(this::processarSaidaEstoque);
		}

	}
	 private void validarQuantidadeTotal(BigDecimal quantidadeTotal, MovimentacaoInput  movimentacao) {
	
	 }	  
	 private BigDecimal novoEstoque(ItemMovimentacao item) {
		return null;
	 }
	 
	 private Produto buscarProduto(Long produtoId) {
	        return produtoRepository.findById(produtoId)
	                .orElseThrow(() -> new RegistroNaoEncontrado("Produto não encontrado para o ID: " + produtoId));
	    }
	 
	
}
