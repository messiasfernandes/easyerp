package com.easyerp.domain.entidade;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Digits;
import lombok.Data;

@Entity
@Data
public class ItemMovimentacao {
	     @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @ManyToOne
	    @JoinColumn(name = "movimentacao_id", nullable = false)
	    private MovimentacaoEstoque movimentacao;

	    @ManyToOne
	    @JoinColumn(name = "produtovariacao_id", nullable = false)
	    private ProdutoVariacao produtoVariacao;
	    @Digits(integer = 9, fraction = 4)
	    @Column(nullable = false)
	    private BigDecimal quantidade= BigDecimal.ZERO;

}
