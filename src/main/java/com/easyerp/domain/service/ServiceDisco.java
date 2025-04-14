package com.easyerp.domain.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.easyerp.domain.service.exeption.ArquivoInvalidoException;
import com.easyerp.domain.service.exeption.ArquivoSizeExeption;
import com.easyerp.model.dto.ArquivoResponse;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.name.Rename;
@Service
public class ServiceDisco {
	
	private Path local;

	@Value("${storage.disco}")
	private String raiz;
	@Value("${storage.foto}")
	private String localfoto;
	@Value("${storage.xml}")
	private String arquivo_xml;
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


	public List<ArquivoResponse> salvar(List<MultipartFile> files) {
		List<ArquivoResponse> arquivos = new ArrayList<>();

		for (MultipartFile file : files) {
			ArquivoResponse  arquivo = criarArquivoDto(file);
			

	
			validarArquivo(file);
			try { 

				// arquivo.setUrl( );
				/// arquivo.setUrl(arquivo.add(WebMvcLinkBuilder.linkTo(ArquivoControler.class).slash(arquivo.getNomeArquivo()).withSelfRel()).toString());

				arquivos.add(arquivo);
				file.transferTo(new File(local.toAbsolutePath().toString(),
						FileSystems.getDefault().getSeparator() + file.getOriginalFilename()));
				gerarThumbnail(file);
			//	Thumbnails.of(this.local.resolve(file.getOriginalFilename()).toString()).size(400, 280)
					///	.toFiles(Rename.NO_CHANGE);
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return arquivos;
	}

	private void criarPasta() {
		System.out.println("criou pasta");
		try {

			Files.createDirectories(this.local);

		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	public byte[] carregarFoto(File foto) throws IOException {

		try {
			return Files.readAllBytes(foto.toPath());
		} catch (IOException e) {

			e.printStackTrace();

		}

		return Files.readAllBytes(foto.toPath());
	}

	public List<byte[]> cacrregarImagem(List<File> fotos) throws IOException {

		List<byte[]> files = new ArrayList<>();
		for (int i = 0; i < fotos.size(); i++) {
			byte[] data = new byte[100];
			data = Files.readAllBytes(fotos.get(i).toPath());

			files.add(data);
		}
		return files;

	}

	public File buscarfoto(String foto) {

		File img = transferirouBuscar(foto);

		return img;

	}

	public List<File> pesquisrfoto(List<String> fotos) {
		List<File> aruqivos = new ArrayList<>();
		for (String foto : fotos) {

			aruqivos.add(transferirouBuscar(foto));

		}
		return aruqivos;

	}

	private Path caminho() {

		return local = Paths.get(raiz, localfoto, FileSystems.getDefault().getSeparator());
	}

	private File transferirouBuscar(String nomeArquivo) {

		return new File(caminho().toAbsolutePath().toString(), FileSystems.getDefault().getSeparator() + nomeArquivo);
	}

	public boolean delete(String filename) {
		try {
			Path file = caminho().resolve(filename);
			return Files.deleteIfExists(file);
		} catch (IOException e) {
			throw new RuntimeException("Error: " + e.getMessage());
		}
	}
	
	  private void validarArquivo(MultipartFile file) {
		   long tamanhoMaximo = 5 * 1024 * 1024; // 5MB
		    if (file.getSize() > tamanhoMaximo) {
		        throw new ArquivoSizeExeption(  file.getSize() , new Throwable( " O arquivo excede o tamanho máximo permitido de 5MB."));
		    }
		   String contentType = file.getContentType();
		   System.out.println("pasou aqui ");
	       if (!contentType.equals("image/jpeg") && !contentType.equals("image/png")) {
	           throw new ArquivoInvalidoException("Tipo de arquivo inválido. Apenas JPG e PNG são permitidos.");
	       }
				
	   }
	  
		private void gerarThumbnail(MultipartFile file) throws IOException {
			
			
			Thumbnails.of(this.local.resolve(file.getOriginalFilename()).toString()).size(400, 280)
			.toFiles(Rename.NO_CHANGE);
			//logger.debug("Thumbnail gerado para {}", file.getOriginalFilename());
	
	
	}
		private ArquivoResponse criarArquivoDto(MultipartFile file) {
			ArquivoResponse arquivo = new ArquivoResponse(file.getOriginalFilename(), "thumbnail." + file.getOriginalFilename(),
					file.getContentType(), file.getSize());

			return arquivo;
		}
}


