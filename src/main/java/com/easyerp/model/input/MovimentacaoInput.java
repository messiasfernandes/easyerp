package com.easyerp.model.input;

import java.math.BigDecimal;
import java.util.Set;


import com.easyerp.domain.enumerados.TipoMovimentacao;


public record MovimentacaoInput(Long idProduto, String observacao, TipoMovimentacao tipoMovimentacao,  
		Set<ItemdeMovimentacaoInput>itens
) {

}
