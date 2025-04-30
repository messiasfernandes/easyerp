package com.easyerp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.easyerp.controller.documentacao.ProdutoControllerOpenApi;
import com.easyerp.domain.service.ProdutoService;
import com.easyerp.model.dto.ProdutoResponse;
import com.easyerp.model.input.ProdutoCadastroInput;
import com.easyerp.model.input.ProdutoEditarInput;

import jakarta.validation.Valid;

@RequestMapping("v1/produtos")
@RestController
public class ProdutoController implements ProdutoControllerOpenApi {
	@Autowired
	private ProdutoService produtoService;

	@GetMapping
	public ResponseEntity<Page<ProdutoResponse>> listar(
			@RequestParam(required = false, defaultValue = "") String parametro,
			@RequestParam(value = "page", defaultValue = "0") Integer pagina,
			@RequestParam(defaultValue = "10") Integer size, Pageable page) {
		return ResponseEntity.status(HttpStatus.OK).body(produtoService.listar(parametro, page));
	}

	@PostMapping
	@Override
	public ResponseEntity<ProdutoResponse> create(@Valid @RequestBody ProdutoCadastroInput produtoCadastroInput) {

		return ResponseEntity.status(HttpStatus.CREATED).body(produtoService.salvar(produtoCadastroInput));
	}
	@PutMapping
	
	public ResponseEntity<ProdutoResponse> update (@Valid @RequestBody ProdutoEditarInput produtoEditarInput) {

		return ResponseEntity.status(HttpStatus.OK).body(produtoService.atualizar(produtoEditarInput ));
	}
	@DeleteMapping("{id}")
	@Override
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		produtoService.excluir(id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@GetMapping("{id}")
	@Override
	public ResponseEntity<ProdutoResponse> detalhar(@PathVariable Long id) {

		return ResponseEntity.status(HttpStatus.OK).body(produtoService.buscarPorId(id));
	}

}
