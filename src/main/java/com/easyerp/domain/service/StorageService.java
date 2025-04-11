package com.easyerp.domain.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import com.easyerp.domain.service.exeption.ArquivoInvalidoException;
import com.easyerp.domain.service.exeption.ArquivoSizeExeption;
import com.easyerp.domain.service.exeption.ExtensaoArquivoInvalidaException;
import com.easyerp.domain.service.exeption.StorageException;
import com.easyerp.model.dto.ArquivoResponse;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.name.Rename;
import net.coobird.thumbnailator.tasks.UnsupportedFormatException;

@Service
public class StorageService {

	private   Logger logger = LoggerFactory.getLogger(StorageService.class);
	private  Path local;

	@Value("${storage.disco}")
	private String raiz;
	@Value("${storage.foto}")
	private String localfoto;
	@Value("${storage.foto}")
	private String diretoriofoto;
	public StorageService() {
     
    }

	@jakarta.annotation.PostConstruct
	public void init() {
		   System.out.println("INIT StorageService:");
		    System.out.println("Raiz: " + raiz);
		    System.out.println("Foto: " + localfoto);
		  caminho();
			System.out.println("pasou aqui"+ caminho());
			System.out.println(caminho());	
	
		criarPasta();
	}

	private void criarPasta() {
		
		try {
   
	           Files.createDirectories(this.local);
	           logger.info("Diretório de armazenamento criado em: {}", local.toAbsolutePath());
	  
	    } catch (IOException e) {
	        logger.error("Erro ao criar diretório de armazenamento: {}", local.toAbsolutePath(), e);
	        throw new RuntimeException("Erro ao criar diretório: " + e.getMessage());
	    }
	}

	public List<ArquivoResponse> salvar(List<MultipartFile> files) throws IOException {

       
	
		List<ArquivoResponse> arquivos = new ArrayList<>();

		for (MultipartFile file : files) {
			
				validarArquivo(file);
				ArquivoResponse arquivo = criarArquivoDto(file);
				arquivos.add(arquivo);
				file.transferTo(new File(local.toAbsolutePath().toString(),
						FileSystems.getDefault().getSeparator() + file.getOriginalFilename()));
		
						gerarThumbnail(file);
		    
		}
		return arquivos;
	
	}

	private ArquivoResponse criarArquivoDto(MultipartFile file) {
		ArquivoResponse arquivo = new ArquivoResponse(file.getOriginalFilename(), "thumbnail." + file.getOriginalFilename(),
				file.getContentType(), file.getSize());

		return arquivo;
	}

	private void gerarThumbnail(MultipartFile file) throws IOException {
	
	
			Thumbnails.of(this.local.resolve(file.getOriginalFilename()).toString()).size(400, 280)
			.toFiles(Rename.NO_CHANGE);
			logger.debug("Thumbnail gerado para {}", file.getOriginalFilename());
	
	
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
	
	private Path caminho() {

		return local = Paths.get(raiz, localfoto, FileSystems.getDefault().getSeparator());
	}
   private void validarArquivo(MultipartFile file) {
	   long tamanhoMaximo = 5 * 1024 * 1024; // 5MB
	   if (file.isEmpty()) {
			
			    throw new ArquivoInvalidoException("O arquivo está vazio.");
			}  String contentType = file.getContentType();
	        if (!contentType.equals("image/jpeg") && !contentType.equals("image/png")) {
	            throw new ExtensaoArquivoInvalidaException("Tipo de arquivo inválido. Apenas JPG e PNG são permitidos.");
	        }
	        String contentType1 = file.getContentType();
	        if (contentType1 == null || 
	           (!contentType1.equals("image/jpeg") && !contentType1.equals("image/png"))) {
	            throw new ExtensaoArquivoInvalidaException("Tipo de arquivo inválido. Apenas JPG e PNG são permitidos.");
	        }

	        // Valida extensão do nome do arquivo também
//	        String nomeArquivo = file.getOriginalFilename();
//	        if (nomeArquivo == null || 
//	           (!nomeArquivo.toLowerCase().endsWith(".jpg") && 
//	            !nomeArquivo.toLowerCase().endsWith(".jpeg") && 
//	            !nomeArquivo.toLowerCase().endsWith(".png"))) {
//	            throw new ExtensaoArquivoInvalidaException("Extensão de arquivo inválida. Apenas JPG e PNG são permitidos.");
//	        }

			
   }
   
   
}
