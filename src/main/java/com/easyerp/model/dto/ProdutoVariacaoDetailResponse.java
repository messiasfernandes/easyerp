package com.easyerp.model.dto;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import com.easyerp.domain.entidade.ProdutoVariacao;
import com.easyerp.utils.anotacoes.FormatBigDecimal;
import com.fasterxml.jackson.annotation.JsonInclude;

public record ProdutoVariacaoDetailResponse(Long id, String descricao, String codigoEan13, String unidade, Integer qtdeEstoque,
		BigDecimal qtdeporPacote , @FormatBigDecimal BigDecimal desconto ,
		 Set<AtributoResponse> atributos,
	 Set<ComponenteResponse> componentes) {
  public ProdutoVariacaoDetailResponse(ProdutoVariacao produtoVariacao) {
	  this(produtoVariacao.getId(), produtoVariacao.getDescricao(), produtoVariacao.getCodigoEan13(),
			  produtoVariacao.getUnidadeMedida().getEmbalageNome(),
			  produtoVariacao.getQtdeEstoque(),produtoVariacao.getQtdeporPacote(), produtoVariacao.getDesconto(),
			  produtoVariacao.getAtributos().stream().map(AtributoResponse::new ).collect(Collectors.toSet()),
			  produtoVariacao.getComponentes().stream().map(ComponenteResponse::new).collect(Collectors.toSet())
			  );
	  
  }
}
