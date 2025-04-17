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
import com.easyerp.domain.service.StorageService;
import com.easyerp.domain.service.StorageService2;
import com.easyerp.model.dto.ArquivoResponse;

import jakarta.activation.FileTypeMap;

@RestController
@RequestMapping("v1/arquivos")
public class ArquivoController implements ArquivosControllerOpenApi {

	private static final org.slf4j.Logger logger =  LoggerFactory.getLogger(ArquivoController.class);

    @Autowired
    private StorageService2 serviceStorage;
    
    @Autowired
    private StorageService storageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Override
    public ResponseEntity<List<ArquivoResponse>> upload(  @RequestParam List<MultipartFile> arquivo) {
       
            return ResponseEntity.status(HttpStatus.CREATED).body(storageService.salvar(arquivo));
    }

    @GetMapping("/{arquivo}")
    @Override
    public ResponseEntity<byte[]> getArquivo(@PathVariable String arquivo) throws IOException {
        logger.info("Buscando arquivo: {}", arquivo);
        try {
            File img = serviceStorage.buscarfoto(arquivo);
            byte[] conteudo = storageService.carregarFoto(img);
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
    
        
        storageService.deletar(nomeArquivo);

        return ResponseEntity.notFound().build();
    }
    
 
}
