package com.easyerp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easyerp.controller.documentacao.EstoqueMovimentacaoControllerOpenApi;
import com.easyerp.domain.service.MovimentacaoEstoqueService;
import com.easyerp.domain.service.MovimentacaoService;
import com.easyerp.model.dto.MovimentacaoResponse;
import com.easyerp.model.input.MovimentacaoInput;

import jakarta.validation.Valid;

@RequestMapping("v1/movimentacaoestoque")
@RestController
public class EstoqueMovimentacaoController implements EstoqueMovimentacaoControllerOpenApi {
	
	@Autowired
 private MovimentacaoEstoqueService movimentacaoEstoqueService;
	@Autowired
   private MovimentacaoService movimentacaoService;
	@PostMapping
	@Override
	public ResponseEntity<MovimentacaoResponse> movimentar( @Valid @RequestBody  MovimentacaoInput estoqueMovimentacaoInput) {
	
		return ResponseEntity.status(HttpStatus.CREATED).body(movimentacaoService.registrarMovimentacao(estoqueMovimentacaoInput));
	}
	
	

}
