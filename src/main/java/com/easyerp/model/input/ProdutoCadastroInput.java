package com.easyerp.model.input;

import java.math.BigDecimal;
import java.util.Set;

import com.easyerp.domain.service.exeption.NegocioException;

import jakarta.validation.Valid;

public record ProdutoCadastroInput(Long id, String produto, String descricao, BigDecimal custo,
        BigDecimal custoMedio,
        BigDecimal precoVenda,
       
        SubCategoriaInput subCategoriaInput,
        
        Set<@Valid ProdutoVariacaoCadastroInput> variacoes) {
	
	  public void validar() {
	        if ( variacoes == null || variacoes.isEmpty()) {
	            throw new NegocioException("O produto deve ter pelo menos uma variação.");
	        }
	       
	    }
}
