package com.easyerp.model.dto;

import java.time.LocalDateTime;

import com.easyerp.domain.entidade.MovimentacaoEstoque;
import com.easyerp.domain.enumerados.TipoMovimentacao;


public record MovimentacaoResponse(Long id,  LocalDateTime datamovimentacao, TipoMovimentacao tipoMovimentacao) {
	public MovimentacaoResponse(MovimentacaoEstoque movimentacaoEstoque) {
		   this(movimentacaoEstoque.getId(), movimentacaoEstoque.getDataMovimentacao(), movimentacaoEstoque.getTipoMovimentacao());

	}

}
