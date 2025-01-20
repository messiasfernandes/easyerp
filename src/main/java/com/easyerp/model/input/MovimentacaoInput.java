package com.easyerp.model.input;

import java.util.Set;


import com.easyerp.domain.enumerados.TipoMovimentacao;


public record MovimentacaoInput(Long idProduto, TipoMovimentacao tipoMovimentacao , 
		Set<ItemMovimentacoaInput>itens
) {

}
