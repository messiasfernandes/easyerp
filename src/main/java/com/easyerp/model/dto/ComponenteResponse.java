package com.easyerp.model.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.easyerp.domain.entidade.Componente;

public record ComponenteResponse(Long id, String produto ,
		 String codigoEan13 ,BigDecimal qtde, BigDecimal custodeProducao, BigDecimal total)  {
	
	public ComponenteResponse(Componente componente) {

		this(
			    componente.getId(),
			  
			    componente.getVariacao().getProduto().getProdutoNome(),
               componente.getVariacao().getCodigoEan13(),
			    componente.getQtde() != null ? componente.getQtde() : BigDecimal.ZERO,
			    		
			    componente.getCustodeProducao() != null ? componente.getCustodeProducao() : BigDecimal.ZERO,
			  
					   componente.getVariacao().getProduto().getCusto().multiply(componente.getQtde())
					   .setScale(3,RoundingMode.HALF_EVEN));
	}
}
