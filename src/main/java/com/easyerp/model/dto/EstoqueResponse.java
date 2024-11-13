package com.easyerp.model.dto;

import java.math.BigDecimal;

import com.easyerp.domain.entidade.Estoque;

public record EstoqueResponse(BigDecimal estoqueAtual) {
	public EstoqueResponse(Estoque estoque) {
		this(estoque.getQuantidade());
	}
}
