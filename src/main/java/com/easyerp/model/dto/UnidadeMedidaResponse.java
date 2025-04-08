package com.easyerp.model.dto;

import com.easyerp.domain.entidade.UnidadeMedida;

public record UnidadeMedidaResponse(Long id, String descricao) {
	
	public UnidadeMedidaResponse(UnidadeMedida  unidadeMedida) {
		this(unidadeMedida.getId(), unidadeMedida.getEmbalageNome());
		
	}

}
