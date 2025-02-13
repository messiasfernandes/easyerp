package com.easyerp.model.dto;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import com.easyerp.domain.entidade.Produto;
import com.fasterxml.jackson.annotation.JsonInclude;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProdutoResponse(Long id, String produto, BigDecimal custo, BigDecimal custoMedio, BigDecimal precoVenda,
		 EstoqueResponse estoque, MarcaResponse marca, Set<ProdutoVariacaoResponse> variacoes) {
	public ProdutoResponse(Produto produto) {
		this(produto.getId(), produto.getProdutoNome(), produto.getCusto(), produto.getCustoMedio(),
				produto.getPrecoVenda(), 
			///   produto.getSubCategoria()!=null?,	produto.getSubCategoria().getSubcategoriaNome(),
				produto.getEstoque() != null ? new EstoqueResponse(produto.getEstoque().getQuantidade()) : null,
				produto.getMarca() != null ? new MarcaResponse(produto.getMarca()) : null,

				produto.getVariacoes().stream().map(ProdutoVariacaoResponse::new).collect(Collectors.toSet()));

	}

}
