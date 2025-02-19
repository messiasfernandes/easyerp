package com.easyerp.model.dto;

import com.easyerp.domain.entidade.Estoque;

public record EstoqueResponse(Integer estoqueAtual) {
	public EstoqueResponse(Estoque estoque) {
		this(estoque.getQuantidade().intValue());
	}
}
