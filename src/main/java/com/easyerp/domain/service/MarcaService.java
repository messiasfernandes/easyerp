package com.easyerp.domain.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.easyerp.config.ModelMapper;
import com.easyerp.domain.entidade.Marca;
import com.easyerp.domain.query.MarcaSpec;
import com.easyerp.domain.repository.MarcaRepository;
import com.easyerp.domain.service.exeption.NegocioException;
import com.easyerp.model.dto.MarcaResponse;
import com.easyerp.model.input.MarcaCadastroInput;
import com.easyerp.utils.ServiceFuncoes;
import com.easyerp.utils.TolowerCase;

import jakarta.transaction.Transactional;

@Service
public class MarcaService extends ServiceFuncoes {
	@Autowired
	  private MarcaRepository  marcaRepository;

		
		private final ModelMapper marcaConversor = new ModelMapper();
	  @Transactional
	  public MarcaResponse salvar(MarcaCadastroInput marcaCadastroInput) {
		  var nome = TolowerCase.normalizarString(marcaCadastroInput.marca());
		  Marca marcaExistente = marcaRepository.buscar(nome);
		  Marca marca = marcaConversor.converter(marcaCadastroInput, Marca::new);

		  if (marcaExistente != null && !marcaExistente.equals(marca)) {
		      throw new NegocioException("Marca já cadastrada no banco de dados");
		  }

		  Marca marcaParaRetorno = (marcaExistente == null) ? marcaRepository.save(marca) : marcaExistente;
		  return marcaConversor.converter(marcaParaRetorno, MarcaResponse::new);
				  
				 
	  }
	  public List<MarcaResponse>listar(){
		 var listaMarca = marcaRepository.findAll();
		  return marcaConversor.convertList(listaMarca, MarcaResponse::new );
				// marcaConVerter.toCollectionDto(marcaRepository.findAll());
	  }
		public Page <MarcaResponse> buscar(String parametro, Pageable pageable) {
			MarcaSpec marcaSpec = new MarcaSpec();
		    Page<Marca> marcas;

		    if (parametro != null && ehnumero(parametro)) {
		        Long id = Sonumero(parametro);
		        marcas = marcaRepository.findAll(marcaSpec.buscarId(id), pageable);
		    } else {
		        parametro = TolowerCase.normalizarString(parametro);
		        marcas = marcaRepository.findAll(marcaSpec.buscar(parametro).or(marcaSpec.buscarMarca(parametro)), pageable);
		    }

		    return marcaConversor.convertPage(marcas, MarcaResponse::new);
		}

}
