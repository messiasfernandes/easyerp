package com.easyerp.model.input;

import java.math.BigDecimal;
import java.util.Set;

import com.easyerp.domain.enumerados.TipoProduto;
import com.easyerp.domain.service.exeption.NegocioException;

import jakarta.validation.Valid;

public record ProdutoCadastroInput(Long id, String produto, TipoProduto tipoProduto, BigDecimal custo,
        BigDecimal custoMedio,
        BigDecimal precoVenda,
       BigDecimal estoqueMinimo,
       BigDecimal estoqueMaximo,
         MarcaCadastroInput marca,
        SubCategoriaInput subCategoria,
        
        Set<@Valid VariacaoCadastroInput> variacoes) {
	
	  public void validar() {
	        if ( variacoes == null || variacoes.isEmpty()) {
	            throw new NegocioException("O produto deve ter pelo menos uma variação.");
	        }
	       
	    }
}
