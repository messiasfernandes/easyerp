package com.easyerp.model.input;

import java.math.BigDecimal;
import java.util.Set;

import com.easyerp.domain.enumerados.TipoProduto;

import jakarta.validation.Valid;

public record ProdutoEditarInput(Long id, String produto, String descricao,  BigDecimal custo,
		BigDecimal custoMedio, BigDecimal precoVenda, TipoProduto tipoProduto,
		MarcaInput marca, SubCategoriarInput categoria,  @Valid Set<ProdutoVAlterar> variacoes ) {
}
