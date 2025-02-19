package com.easyerp.model.dto;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import com.easyerp.domain.entidade.Produto;
import com.easyerp.utils.anotacoes.FormatBigDecimal;
import com.fasterxml.jackson.annotation.JsonInclude;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProdutoResponse(Long id, String produto,@FormatBigDecimal BigDecimal custo,@FormatBigDecimal BigDecimal custoMedio,@FormatBigDecimal BigDecimal precoVenda,
		 EstoqueResponse estoque, MarcaResponse marca, Set<ProdutoVariacaoResponse> variacoes) {
	public ProdutoResponse(Produto produto) {
		this(produto.getId(), produto.getProdutoNome(), produto.getCusto(), produto.getCustoMedio(),
				produto.getPrecoVenda(), 
			///   produto.getSubCategoria()!=null?,	produto.getSubCategoria().getSubcategoriaNome(),
				produto.getEstoque() != null ? new EstoqueResponse(produto.getEstoque().getQuantidade().intValue()) : null,
				produto.getMarca() != null ? new MarcaResponse(produto.getMarca()) : null,

				produto.getVariacoes().stream().map(ProdutoVariacaoResponse::new).collect(Collectors.toSet()));

	}

}
