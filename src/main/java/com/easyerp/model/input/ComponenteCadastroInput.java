package com.easyerp.model.input;

import java.math.BigDecimal;

public record ComponenteCadastroInput( Long produtoId, String produto, BigDecimal qtde, 
		BigDecimal custodeProducao, BigDecimal precoCusto, BigDecimal precoVenda, BigDecimal subTotal) {

}
