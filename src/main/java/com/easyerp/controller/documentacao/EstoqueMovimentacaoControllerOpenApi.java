package com.easyerp.controller.documentacao;

import org.springframework.http.ResponseEntity;

import com.easyerp.model.dto.MovimentacaoResponse;
import com.easyerp.model.input.MovimentacaoInput;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Movimentações")
public interface EstoqueMovimentacaoControllerOpenApi extends CrossController {
	
	  @Operation(summary = "Adicionar estoque de um produto" , method = "Post")
	    @ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Movimentação criada com sucesso"),
	            @ApiResponse(responseCode = "400", description = "Requisição inválida") })
	     ResponseEntity <MovimentacaoResponse> movimentar( MovimentacaoInput estoqueMovimentacaoInput);

}
