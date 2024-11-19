package com.easyerp.model.input;

import java.math.BigDecimal;
import java.util.Set;

public record VariacaoCadastroInput(
		Long id,
        String codigoEan13,
        String descricao,
        Boolean ativo,
		String imagemProduto,
		BigDecimal qtdeporPacote ,
		BigDecimal custoAdicional,
		UnidadeMedidaInput unidadeInput,
		Set<AtributoCadastroInput>atributos,
		Set<ComponenteCadastroInput>componentes
		
		) {

}
