package com.easyerp.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easyerp.config.ModelMapper;
import com.easyerp.domain.repository.MovimentoEstoqueRepository;
import com.easyerp.model.dto.MovimentacaoReponse;
import com.easyerp.model.input.MovimentacaoInput;

import jakarta.transaction.Transactional;
@Service
public class MovimentacaoEstoque {
	@Autowired
   private MovimentoEstoqueRepository movimentoEstoqueRepository;
	@Autowired
   private ModelMapper movimentacaoEstoqueMapper;
	
	@Transactional
	public MovimentacaoReponse registrarMovimentacao(MovimentacaoInput movimentacaoInput) {
return null;
	}
}
