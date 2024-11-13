package com.easyerp.model.dto;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.easyerp.domain.entidade.Produto;

public record ProdutoResponse(Long id, String produto, MarcaResponse marca,EstoqueResponse estoque , BigDecimal custo, BigDecimal custoMedio,
		BigDecimal precoVenda,
		Set<ProdutoVariacaoResponse> variacoes)
{
	public ProdutoResponse(Produto produto) {
		this(produto.getId(), produto.getProdutoNome(),
				produto.getMarca() != null ? new MarcaResponse(produto.getMarca()) : null,
						 produto.getEstoque()!=null ? new EstoqueResponse(produto.getEstoque().getQuantidade()): null,
						produto.getCusto(),
				produto.getCustoMedio(), produto.getPrecoVenda(),
				produto.getVariacoes().stream().map(ProdutoVariacaoResponse::new).collect(Collectors.toSet())
				);

	}

}
