package com.easyerp.domain.entidade;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import lombok.Getter;
import lombok.Setter;

@Table(name = "tab_estoque")
@Entity
@Getter
@Setter
public class Estoque {


	@Id
	private Long id;
	@OneToOne(fetch = FetchType.EAGER)
	@MapsId
	private Produto produto;
	@Digits(integer = 9, fraction = 4)
	@Column
	private BigDecimal quantidade = BigDecimal.ZERO;
	private LocalDateTime dataCadastro;
	private LocalDateTime dataAlteracao;

}
