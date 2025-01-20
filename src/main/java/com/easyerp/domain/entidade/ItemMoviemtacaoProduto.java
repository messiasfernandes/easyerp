package com.easyerp.domain.entidade;

import java.math.BigDecimal;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Digits;

public class ItemMoviemtacaoProduto {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Digits(integer = 9, fraction = 3)
	private BigDecimal qtde = BigDecimal.ZERO;
	@Digits(integer = 9, fraction = 3)
	private BigDecimal saldoAnterior = BigDecimal.ZERO;
	@Digits(integer = 9, fraction = 3)
	private BigDecimal estoqueTotal = BigDecimal.ZERO;
	

}
