package com.easyerp.domain.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easyerp.config.ModelMapper;
import com.easyerp.domain.repository.UnidadedeMedidaRepository;
import com.easyerp.model.dto.UnidadeMedidaResponse;

@Service
public class UnidadeMediddaService {
	@Autowired
	private UnidadedeMedidaRepository unidadedeMedidaRepository;
	@Autowired
	private ModelMapper modelMapper;
	public List<UnidadeMedidaResponse> pesquisar(){
		return modelMapper.convertList(unidadedeMedidaRepository.findAll(), UnidadeMedidaResponse::new);
	}

}
