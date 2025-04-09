package com.easyerp.domain.service;

import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.easyerp.domain.service.exeption.StorageException;
import com.easyerp.model.dto.ArquivoResponse;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class StorageService {

	private   Logger logger = LoggerFactory.getLogger(StorageService.class);
	private  Path local;

	@Value("${storage.disco}")
	private String raiz;
	@Value("${storage.foto}")
	private String localfoto;

	public StorageService() {
     
    }

	@jakarta.annotation.PostConstruct
	public void init() {
		   this.local = Paths.get(raiz, localfoto);
		criarPasta();
	}

	private void criarPasta() {
		try {
	        if (Files.exists(this.local)) {
	            logger.info("Diretório já existe: {}", local.toAbsolutePath());
	        } else {
	            Files.createDirectories(this.local);
	            logger.info("Diretório de armazenamento criado em: {}", local.toAbsolutePath());
	        }
	    } catch (IOException e) {
	        logger.error("Erro ao criar diretório de armazenamento: {}", local.toAbsolutePath(), e);
	        throw new RuntimeException("Erro ao criar diretório: " + e.getMessage());
	    }
	}

	public List<ArquivoResponse> salvar(List<MultipartFile> files) {
		List<ArquivoResponse> arquivos = new ArrayList<>();
		for (MultipartFile file : files) {
			try {
				if (file.getSize() == 0) {
	                logger.warn("Arquivo {} está vazio", file.getOriginalFilename());
	                throw new IllegalArgumentException("Arquivo vazio: " + file.getOriginalFilename());
	            }

	            // Validar a extensão do arquivo
	            String nomeArquivo = file.getOriginalFilename();
	            if (nomeArquivo == null || 
	                !(nomeArquivo.toLowerCase().endsWith(".png") || nomeArquivo.toLowerCase().endsWith(".jpg") || nomeArquivo.toLowerCase().endsWith(".jpeg"))) {
	                logger.warn("Extensão de arquivo não permitida: {}. Apenas PNG e JPG são aceitos.", nomeArquivo);
	                throw new IllegalArgumentException("Extensão de arquivo não permitida: " + nomeArquivo + ". Apenas PNG e JPG são aceitos.");
	            }

	            // Validar o tipo de arquivo (contentType)
	            String contentType = file.getContentType();
	            if (contentType == null || 
	                !(contentType.equalsIgnoreCase("image/png") || contentType.equalsIgnoreCase("image/jpeg") || contentType.equalsIgnoreCase("image/jpg"))) {
	                logger.warn("Formato de arquivo não permitido: {}. Apenas PNG e JPG são aceitos.", contentType);
	                throw new IllegalArgumentException("Formato de arquivo não permitido: " + nomeArquivo + ". Apenas PNG e JPG são aceitos.");
	            }
				ArquivoResponse arquivo = criarArquivoDto(file);
				System.out.println(arquivo.nomeArquivo()+"arquivo");
				Path destino = local.resolve(file.getOriginalFilename());
				file.transferTo(destino.toFile());
				gerarThumbnail(destino);
				arquivos.add(arquivo);
				logger.info("Arquivo {} salvo com sucesso", file.getOriginalFilename());
			} catch (IOException e) {
				logger.error("Erro ao salvar arquivo {}", file.getOriginalFilename(), e);
				throw new StorageException("Erro ao salvar arquivo " + file.getOriginalFilename(), e);
			}
		}
		return arquivos;
	}

	private ArquivoResponse criarArquivoDto(MultipartFile file) {
		ArquivoResponse arquivo = new ArquivoResponse(file.getOriginalFilename(), "thumbnail." + file.getOriginalFilename(),
				file.getContentType(), file.getSize());

		return arquivo;
	}

	private void gerarThumbnail(Path arquivo) throws IOException {
		Thumbnails.of(arquivo.toString()).size(400, 280).toFiles(net.coobird.thumbnailator.name.Rename.NO_CHANGE);
		logger.debug("Thumbnail gerado para {}", arquivo.getFileName());
	}

	public byte[] carregarFoto(File foto) throws IOException {
		if (!foto.exists()) {
			throw new FileNotFoundException("Arquivo não encontrado: " + foto.getPath());
		}
		return Files.readAllBytes(foto.toPath());
	}

	public File buscarfoto(String foto) throws FileNotFoundException {
		File img = new File(local.toAbsolutePath().toString(), foto);
		if (!img.exists()) {
			throw new FileNotFoundException("Foto não encontrada: " + foto);
		}
		return img;
	}

	public boolean delete(String filename) {
		try {
			Path file = local.resolve(filename);
			return Files.deleteIfExists(file);
		} catch (IOException e) {
			logger.error("Erro ao deletar arquivo {}", filename, e);
			  throw new StorageException("Erro ao deletar arquivo " + filename, e);
		}
	}

}
