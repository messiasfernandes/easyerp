package com.easyerp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.easyerp.controller.documentacao.CrossController;
import com.easyerp.domain.service.MarcaService;
import com.easyerp.model.dto.MarcaResponse;
import com.easyerp.model.input.MarcaCadastroInput;

import jakarta.validation.Valid;
@RestController

@RequestMapping("/v1/marcas")
public class MarcaController  implements CrossController {
	 @Autowired	
		private MarcaService marcaService;
	    @PostMapping
	    public ResponseEntity<MarcaResponse>criar(@RequestBody @Valid MarcaCadastroInput marcaCadastroInput){
	    	return ResponseEntity.status(HttpStatus.CREATED).body(marcaService.salvar(marcaCadastroInput));
	    }
	   // @GetMapping
	    public  ResponseEntity<List<MarcaResponse>> lista(){

	        return ResponseEntity.status(HttpStatus.OK).body(marcaService.listar());
	    }
	    @GetMapping
	    public ResponseEntity<Page <MarcaResponse>> listar(
	            @RequestParam(required = false, defaultValue = "") String parametro,
	            @RequestParam(value = "page", defaultValue = "0") Integer pagina,
	            @RequestParam(defaultValue = "10") Integer size, Pageable page) {

	        return ResponseEntity.status(HttpStatus.OK)
	                .body((marcaService.buscar(parametro, page)));

	    }

}
