package com.easyerp.model.dto;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import com.easyerp.domain.entidade.ProdutoVariacao;
import com.fasterxml.jackson.annotation.JsonIgnore;

public record ProdutoVariacaoResponse(Long id, String descricao, String ean13, String unidade ,Integer qtdeEstoque, BigDecimal qtdeporEmbalagem,  String caracterisca,
		@JsonIgnore
	Set<AtributoResponse> caracteristicas , Set<ComponenteResponse>componentes) {
	public ProdutoVariacaoResponse(ProdutoVariacao produtoVariacao) {
		this(produtoVariacao.getId(), produtoVariacao.getDescricao(), 
				produtoVariacao.getCodigoEan13(), 
				produtoVariacao.getUnidadeMedida().getEmbalageNome(),
				produtoVariacao.getQtdeEstoque(),
				produtoVariacao.getQtdeporPacote(),
				concatenar(produtoVariacao.getAtributos().stream().map(AtributoResponse::new).collect(Collectors.toSet())), 
				
				 produtoVariacao.getAtributos().stream().map(AtributoResponse::new).collect(Collectors.toSet()),
				 produtoVariacao.getComponentes().stream().map(ComponenteResponse::new ).collect(Collectors.toSet())
				
				);
	}
	public static String concatenar(Set<AtributoResponse> caracteristicas) {
		if (caracteristicas == null || caracteristicas.isEmpty()) {
			return "Sem características";
		}
		return caracteristicas.stream().map(a -> a.chave() + ": " + a.valor()) // Acessa chave e valor do record
				.collect(Collectors.joining(" | ")); // Junta com ", "
	}
}
