package com.easyerp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easyerp.domain.service.UnidadeMediddaService;
import com.easyerp.model.dto.UnidadeMedidaResponse;
@CrossOrigin
@RequestMapping("v1/unidademedidas")
@RestController
public class UnidadeMedidaController {
	@Autowired
	private UnidadeMediddaService unidadeMediddaService;
	@GetMapping
	public ResponseEntity<List<UnidadeMedidaResponse>>listar(){
		return ResponseEntity.status(HttpStatus.OK).body(unidadeMediddaService.pesquisar());
	}

}
