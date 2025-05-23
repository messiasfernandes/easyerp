package com.easyerp.model.dto;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import com.easyerp.domain.entidade.Produto;
import com.easyerp.domain.enumerados.TipoProduto;
import com.easyerp.utils.anotacoes.FormatBigDecimal;
//@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProdutoResponse(Long id, String produto,TipoProduto tipoProduto, BigDecimal estoqueMinimo,BigDecimal estoqueMaximo,
		@FormatBigDecimal BigDecimal custo,@FormatBigDecimal BigDecimal custoMedio,@FormatBigDecimal BigDecimal precoVenda,
		 EstoqueResponse estoque, MarcaResponse marca, SupCategoriaResponse subcategoria,   Set<ProdutoVariacaoDetailResponse> variacoes) {
	public ProdutoResponse(Produto produto) {
		this(produto.getId(), produto.getProdutoNome(), produto.getTipoProduto(), produto.getEstoqueMinimo(), produto.getEstoqueMaximo(), produto.getCusto(), produto.getCustoMedio(),
				produto.getPrecoVenda(), 
			///   produto.getSubCategoria()!=null?,	produto.getSubCategoria().getSubcategoriaNome(),
				produto.getEstoque() != null ? new EstoqueResponse(produto.getEstoque().getQuantidade().intValue()) : null,
				produto.getMarca() != null ? new MarcaResponse(produto.getMarca()) : null,
						produto.getSubCategoria() != null ? new SupCategoriaResponse(produto.getSubCategoria()) : null,
				produto.getVariacoes().stream().map(ProdutoVariacaoDetailResponse::new).collect(Collectors.toSet()));

	}

}
