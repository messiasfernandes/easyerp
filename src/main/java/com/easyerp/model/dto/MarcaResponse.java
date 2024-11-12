package com.easyerp.model.dto;

import com.easyerp.domain.entidade.Marca;

public record MarcaResponse(Long id, String marca) {
	public MarcaResponse(Marca marca) {
		this(marca.getId(), marca.getNomeMarca());
	}

}
