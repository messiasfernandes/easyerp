package com.easyerp.controller.documentacao;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.easyerp.model.dto.ArquivoResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


@Tag(name = "arquivos")
public interface ArquivosControllerOpenApi extends CrossController {
	
	@Operation(summary = "Envia arquivos para o servidor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Arquivos enviados com sucesso", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ArquivoResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content)
    })
    ResponseEntity<List<ArquivoResponse>> upload(@RequestParam("arquivo") List<MultipartFile> arquivo);

    @Operation(summary = "Recupera um arquivo pelo nome")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Arquivo encontrado", content = {
                    @Content(mediaType = "image/*")}),
            @ApiResponse(responseCode = "404", description = "Arquivo não encontrado", content = @Content)
    })
    ResponseEntity<byte[]> getArquivo(@PathVariable String arquivo) throws IOException;
}


