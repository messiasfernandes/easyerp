package com.easyerp.model.dto;

import com.easyerp.domain.entidade.ProdutoVariacao;

public record ProdutoVariacaoResponse(Long id, String descricao, String ean13) {
	public ProdutoVariacaoResponse(ProdutoVariacao produtoVariacao) {
		this(produtoVariacao.getId(), produtoVariacao.getDescricao(), produtoVariacao.getCodigoEan13());
	}

}
