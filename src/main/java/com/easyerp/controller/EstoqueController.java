package com.easyerp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easyerp.config.ModelMapper;
import com.easyerp.domain.entidade.Estoque;
import com.easyerp.domain.repository.EstoqueRepository;
import com.easyerp.model.dto.EstoqueProdutoResponse;
@RestController
@RequestMapping("/v1/estoque")
public class EstoqueController {
	@Autowired
	private EstoqueRepository estoqueRepository;
	@Autowired
	private ModelMapper estoqueMapper;
	@GetMapping
	public ResponseEntity<List<EstoqueProdutoResponse>>buscar(){
		return ResponseEntity.status(HttpStatus.OK).body(estoqueMapper.convertList(estoqueRepository.findAll(), EstoqueProdutoResponse::new ));
	}

}
