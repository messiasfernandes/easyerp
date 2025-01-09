package com.easyerp.model.input;

import java.math.BigDecimal;

public record VariacaoMovimentacaoInput(Long id, String descricao, String ean13, BigDecimal qtde) {

}
