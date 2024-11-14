package com.easyerp.controller.documentacao;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import com.easyerp.model.dto.ProdutoResponse;
import com.easyerp.model.input.ProdutoCadastroInput;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Produtos")
public interface ProdutoControllerOpenApi  extends CrossController{

    @Operation(summary = "Listar Produtos", method = "GET")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Produto  Encontrado", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = ProdutoResponse.class)) }),
            @ApiResponse(responseCode = "400", description = "Requisição Invalída", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content) })
    ResponseEntity<Page<ProdutoResponse>> listar(String parametro, Integer pagina, Integer size, Pageable page);

    @Operation(summary = "Cria um novo Produto", method = "Post")
    @ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Produto criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida") })
    ResponseEntity<ProdutoResponse> create(ProdutoCadastroInput produtoCadastroInput);
//    @Operation(summary = "Atualizar um novo Produto", method = "Put")
//    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Produto criado com sucesso"),
//            @ApiResponse(responseCode = "400", description = "Requisição inválida") })
//    ResponseEntity<ProdutoResponse> update (ProdutoEditarInput produtoEditarInput);
//    
    @Operation(summary = "Excluir um Produto por ID", method = "Delete")
	ResponseEntity<Void> delete(@Param(value = "ID de uma Produto") Long id);
    
    @Operation(summary = "Buscar Produto por id", method = "GET")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Produto  Encontrado", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = ProdutoResponse.class)) }),
            @ApiResponse(responseCode = "400", description = "Requisição Invalída", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content) })
	public ResponseEntity<ProdutoResponse>detalhar(@PathVariable Long id);
}
