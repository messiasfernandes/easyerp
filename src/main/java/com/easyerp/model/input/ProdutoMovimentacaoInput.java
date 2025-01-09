package com.easyerp.model.input;

import java.util.Set;

public record ProdutoMovimentacaoInput(String produto, Set<VariacaoMovimentacaoInput>variacoes) {

}
