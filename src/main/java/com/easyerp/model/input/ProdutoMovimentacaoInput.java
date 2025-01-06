package com.easyerp.model.input;

import java.util.List;

public record ProdutoMovimentacaoInput(Long idProduto, List<VariacaoMovimentacaoInput>variacaoMovimentacaoInputs) {

}
