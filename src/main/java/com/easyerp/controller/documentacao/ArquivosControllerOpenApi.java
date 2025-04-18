package com.easyerp.controller.documentacao;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.easyerp.controller.exeption.Problema;
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
					@Content(mediaType = "application/json", schema = @Schema(implementation = ArquivoResponse.class)) }),
			@ApiResponse(responseCode = "415", description = "Tipo de arquivo não suportado (Apenas JPG e PNG permitidos)", content = { // Descrição
																																		// mais
																																		// clara
					@Content(mediaType = "application/json", schema = @Schema(implementation = Problema.class)) // Adicionado
																												// conteúdo
																												// consistente
			}),
			@ApiResponse(responseCode = "400", description = "Requisição inválida (ex: parâmetro 'arquivo' ausente)", content = { // Descrição
																																	// exemplo
					@Content(mediaType = "application/json", schema = @Schema(implementation = Problema.class)) // Adicionado
																												// conteúdo
																												// consistente
			}),
			@ApiResponse(responseCode = "413", description = "Payload Too Large - O arquivo enviado excede o tamanho máximo permitido", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = Problema.class)) // Adicionado
																												// mediaType
			}) })
	ResponseEntity<List<ArquivoResponse>> upload(@RequestParam("arquivo") List<MultipartFile> arquivo);

	@Operation(summary = "Recupera um arquivo pelo nome")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Arquivo encontrado", content = {
					@Content(mediaType = "image/*") }),
			@ApiResponse(responseCode = "404", description = "Arquivo não encontrado", content = @Content),
			@ApiResponse(responseCode = "413", description = "Payload Too Large - Arquivo excede o tamanho máximo", content = @Content) })
	ResponseEntity<byte[]> getArquivo(@PathVariable String arquivo) throws IOException;

	@Operation(summary = "Exclui um arquivo pelo nome")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Arquivo excluído com sucesso", 
                     content = @Content), // SEM conteúdo para 204

        @ApiResponse(responseCode = "404", description = "Arquivo não encontrado", content = { 
                     @Content(mediaType = "application/json", schema = @Schema(implementation = Problema.class)) // Assumindo que seu handler 404 retorna Problema
                 }),

        @ApiResponse(responseCode = "403", description = "Acesso negado - Sem permissão para excluir este arquivo", content = {
                     @Content(mediaType = "application/json", schema = @Schema(implementation = Problema.class)) // Assumindo que seu handler 403 retorna Problema
                 }),

        // Opcional: Adicione 401 se a autenticação for relevante e puder falhar neste ponto (geralmente tratada antes)
        // @ApiResponse(responseCode = "401", description = "Não autenticado", content = { ... }),

        @ApiResponse(responseCode = "500", description = "Erro interno no servidor ao tentar excluir o arquivo", content = {
                     @Content(mediaType = "application/json", schema = @Schema(implementation = Problema.class)) // Assumindo que seu handler 500 retorna Problema
                 })
    })
    ResponseEntity<Void> deleteArquivo(@PathVariable String nomeArquivo);
}