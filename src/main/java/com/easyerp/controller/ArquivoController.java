package com.easyerp.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.easyerp.controller.documentacao.ArquivosControllerOpenApi;
import com.easyerp.domain.service.ServiceDisco;
import com.easyerp.domain.service.StorageService;
import com.easyerp.domain.service.exeption.ArquivoInvalidoException;
import com.easyerp.domain.service.exeption.ExtensaoArquivoInvalidaException;
import com.easyerp.model.dto.ArquivoResponse;


import jakarta.activation.FileTypeMap;
import jakarta.validation.Valid;

@RestController
@RequestMapping("v1/arquivos")
public class ArquivoController implements ArquivosControllerOpenApi {

	private static final org.slf4j.Logger logger =  LoggerFactory.getLogger(ArquivoController.class);

    @Autowired
    private StorageService serviceStorage;
    
    @Autowired
    private ServiceDisco serviceDisco;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Override
    public ResponseEntity<List<ArquivoResponse>> upload(  @RequestParam List<MultipartFile> arquivo) {
        logger.info("Recebendo upload de {} arquivos", arquivo.size());
        try {
            List<ArquivoResponse> arquivosSalvos = serviceStorage.salvar(arquivo);
            return ResponseEntity.status(HttpStatus.CREATED).body(arquivosSalvos);
        } catch (Exception e) {
            logger.error("Erro ao processar upload de arquivos", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{arquivo}")
    @Override
    public ResponseEntity<byte[]> getArquivo(@PathVariable String arquivo) throws IOException {
        logger.info("Buscando arquivo: {}", arquivo);
        try {
            File img = serviceStorage.buscarfoto(arquivo);
            byte[] conteudo = serviceStorage.carregarFoto(img);
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf(FileTypeMap.getDefaultFileTypeMap().getContentType(img)))
                    .body(conteudo);
        } catch (FileNotFoundException e) {
            logger.warn("Arquivo não encontrado: {}", arquivo);
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            logger.error("Erro ao carregar arquivo: {}", arquivo, e);
            throw e; // Propagado para ser tratado por um ExceptionHandler global, se houver
        }
    }

    @DeleteMapping("/{nomeArquivo}")
    public ResponseEntity<Void> deleteArquivo(@PathVariable String nomeArquivo) {
        logger.info("Tentando deletar arquivo: {}", nomeArquivo);
        boolean deleted = serviceStorage.delete(nomeArquivo);
        if (deleted) {
            logger.info("Arquivo {} deletado com sucesso", nomeArquivo);
            return ResponseEntity.noContent().build();
        }
        logger.warn("Arquivo {} não encontrado para deleção", nomeArquivo);
        return ResponseEntity.notFound().build();
    }

    public ResponseEntity<String> uploadImagem(@RequestParam MultipartFile arquivo) {
       
            validarArquivo(arquivo);
            // Se chegou aqui, o arquivo é válido
            return ResponseEntity.ok("Arquivo enviado com sucesso!");
       
        
    }
    private void validarArquivo(MultipartFile arquivo) {
        if (arquivo.isEmpty()) {
            throw new ArquivoInvalidoException("O arquivo está vazio.");
        }

        String contentType = arquivo.getContentType();
        if (!contentType.equals("image/jpeg") && !contentType.equals("image/png")) {
            throw new ArquivoInvalidoException("Tipo de arquivo inválido. Apenas JPG e PNG são permitidos.");
        }

        long tamanhoMaximo = 5 * 1024 * 1024; // 5MB
        if (arquivo.getSize() > tamanhoMaximo) {
            throw new ArquivoInvalidoException("O arquivo excede o tamanho máximo permitido de 5MB.");
        }
    }
}
