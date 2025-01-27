package com.easyerp.model.dto;

import java.math.BigDecimal;

import com.easyerp.domain.entidade.ItemMovimentacao;

public record ItemMovimetacaoResponse(Long id, String produto, String codigoEan, BigDecimal estoqueTotal,
		BigDecimal qtdeAnterior, BigDecimal qtdeMovimentada, Integer qtdeporVariacao) {
	public ItemMovimetacaoResponse(ItemMovimentacao item) {
		this(item.getId(), item.getProdutoVariacao().getProduto().getProdutoNome(),
				item.getProdutoVariacao().getCodigoEan13(),
				item.getProdutoVariacao().getProduto().getEstoque().getQuantidade(), item.getSaldoanterior(),
				item.getQuantidade(),
				item.getProdutoVariacao().getQtdeEstoque());
	}

}
