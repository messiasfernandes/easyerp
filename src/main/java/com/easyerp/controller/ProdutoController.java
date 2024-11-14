package com.easyerp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.easyerp.controller.documentacao.ProdutoControllerOpenApi;
import com.easyerp.domain.service.ProdutoService;
import com.easyerp.model.dto.ProdutoResponse;
import com.easyerp.model.input.ProdutoCadastroInput;

@RequestMapping("v1/produtos")
@RestController
public class ProdutoController implements ProdutoControllerOpenApi {
	@Autowired
	private ProdutoService produtoService;
	@GetMapping
	public ResponseEntity <Page <ProdutoResponse>> listar(
            @RequestParam(required = false, defaultValue = "") String parametro,
            @RequestParam(value = "page", defaultValue = "0") Integer pagina,
            @RequestParam(defaultValue = "10") Integer size, Pageable page) {
		return  ResponseEntity.status(HttpStatus.OK).body(produtoService.listar(parametro, page));
	}
	@Override
	public ResponseEntity<ProdutoResponse> create(ProdutoCadastroInput produtoCadastroInput) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public ResponseEntity<Void> delete(Long id) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public ResponseEntity<ProdutoResponse> detalhar(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

}
