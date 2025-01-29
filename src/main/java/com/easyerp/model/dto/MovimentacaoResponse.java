package com.easyerp.model.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import com.easyerp.domain.entidade.MovimentacaoEstoque;
import com.easyerp.domain.enumerados.TipoMovimentacao;


public record MovimentacaoResponse(Long id,  LocalDateTime datamovimentacao, String observacao, TipoMovimentacao tipoMovimentacao, Set<ItemMovimetacaoResponse> items) {
	public MovimentacaoResponse(MovimentacaoEstoque movimentacaoEstoque) {
		   this(movimentacaoEstoque.getId(), movimentacaoEstoque.getDataMovimentacao(),movimentacaoEstoque.getObservacao(),  movimentacaoEstoque.getTipoMovimentacao(),
				   movimentacaoEstoque.getItens().stream().map(ItemMovimetacaoResponse::new).collect(Collectors.toSet()));

	}

}
