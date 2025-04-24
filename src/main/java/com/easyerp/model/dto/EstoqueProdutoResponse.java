package com.easyerp.model.dto;

import java.math.BigDecimal;

import com.easyerp.domain.entidade.Estoque;

public record EstoqueProdutoResponse(Long id, String produto, BigDecimal qtde) {
	
	public EstoqueProdutoResponse(Estoque estoque) {
		this(estoque.getId(), estoque.getProduto().getProdutoNome(), estoque.getQuantidade());
	}

}
