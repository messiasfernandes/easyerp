package com.easyerp.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easyerp.config.ModelMapper;
import com.easyerp.domain.repository.MovimentoEstoqueRepository;
import com.easyerp.domain.repository.ProdutoRepository;
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
		
		var movientacao = produtoRepository.getReferenceById(movimentacaoInput.idProduto());
return null;
	}
}
