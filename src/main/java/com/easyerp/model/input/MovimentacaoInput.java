package com.easyerp.model.input;

import java.util.Set;

import com.easyerp.domain.entidade.MovimentacaoEstoque.TipoMovimentacao;

public record MovimentacaoInput(Long idProduto, TipoMovimentacao tipoMovimentacao , 
		Set<ItemMovimentacoaInput>items
) {

}
