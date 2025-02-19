package com.easyerp.model.dto;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import com.easyerp.domain.entidade.ProdutoVariacao;
import com.easyerp.utils.anotacoes.FormatBigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProdutoVariacaoResponse(Long id, String descricao, String ean13, String unidade, Integer qtdeEstoque,
		BigDecimal qtdeporEmbalagem,  @FormatBigDecimal BigDecimal precoVenda, String caracterisca,
		@JsonIgnore Set<AtributoResponse> caracteristicas,  @JsonInclude(JsonInclude.Include.NON_EMPTY)  Set<ComponenteResponse> componentes) {
	public ProdutoVariacaoResponse(ProdutoVariacao produtoVariacao) {
		this(produtoVariacao.getId(), produtoVariacao.getDescricao(), produtoVariacao.getCodigoEan13(),
				produtoVariacao.getUnidadeMedida().getEmbalageNome(), produtoVariacao.getQtdeEstoque(),
				produtoVariacao.getQtdeporPacote(),
				CalcularPreco(produtoVariacao.getProduto().getPrecoVenda(), produtoVariacao.getDesconto(), produtoVariacao.getQtdeporPacote()),
				concatenar(
						produtoVariacao.getAtributos().stream().map(AtributoResponse::new).collect(Collectors.toSet())),

				produtoVariacao.getAtributos().stream().map(AtributoResponse::new).collect(Collectors.toSet()),
				produtoVariacao.getComponentes().stream().map(ComponenteResponse::new).collect(Collectors.toSet())

		);
	}

	public static String concatenar(Set<AtributoResponse> caracteristicas) {
		if (caracteristicas == null || caracteristicas.isEmpty()) {
			return "Sem características";
		}
		return caracteristicas.stream().map(a -> a.chave() + ": " + a.valor()) // Acessa chave e valor do record
				.collect(Collectors.joining(" | ")); // Junta com ", "
	}

	public static BigDecimal CalcularPreco(BigDecimal preco_Venda, BigDecimal desconto, BigDecimal qtdePacote) {
		BigDecimal resultado = BigDecimal.ZERO;
		if(desconto.signum()!=0 ){
			resultado = resultado.add(preco_Venda.multiply(qtdePacote));
			System.out.println(resultado+ "resulado");
			resultado =resultado.subtract(resultado.multiply(desconto));
		}else {
			resultado= preco_Venda;
		}
	
		return resultado;
				//BigDecimalUtil.format(resultado);
				//resultado.setScale(2, RoundingMode.HALF_UP);
	}
}
