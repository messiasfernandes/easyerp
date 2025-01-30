package com.easyerp.model.dto;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import com.easyerp.domain.entidade.ItemMovimentacao;

public record ItemMovimetacaoResponse(Long id, String produto, String codigoEan,  String caracteristica, BigDecimal estoqueTotal,
		BigDecimal qtdeAnterior, BigDecimal qtdeMovimentada, Integer qtdeporVariacao) {
	public ItemMovimetacaoResponse(ItemMovimentacao item) {
		this(item.getId(), item.getProdutoVariacao().getProduto().getProdutoNome(),
				item.getProdutoVariacao().getCodigoEan13(),
				concatenar(item.getProdutoVariacao().getAtributos().stream().map(AtributoResponse::new)
						.collect(Collectors.toSet())),
				
				item.getProdutoVariacao().getProduto().getEstoque().getQuantidade(), item.getSaldoanterior(),
				item.getQuantidade(),
				item.getProdutoVariacao().getQtdeEstoque()
				
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
