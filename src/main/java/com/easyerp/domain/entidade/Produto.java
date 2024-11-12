package com.easyerp.domain.entidade;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import lombok.Getter;
import lombok.Setter;
@Table(name = "tab_produtos")
@Getter
@Setter
@Entity
public class Produto extends GeradorId {


	@Serial
	private static final long serialVersionUID = 1L;
	@Column(length = 150, nullable = false)
	private String produtoNome;

	@OneToOne(mappedBy = "produto", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private Estoque estoque;
	@ManyToOne(fetch = FetchType.EAGER, optional = true)
	@JoinColumn(name = "marca_id")
	private Marca marca;
	//@Column(length = 30, nullable = false)
//	@Enumerated(EnumType.STRING)
//	private TipoProduto tipoProduto;
	@ManyToOne(fetch = FetchType.EAGER, optional = true)
	@JoinColumn(name = "subcategoria_id")
	private SubCategoria subCategoria;
	@Digits(integer = 9, fraction = 4)
	private BigDecimal custo=BigDecimal.ZERO;
	@Digits(integer = 9, fraction = 4)
	private BigDecimal custoMedio=BigDecimal.ZERO;
	@Digits(integer = 9, fraction = 4)
	private BigDecimal precoVenda= BigDecimal.ZERO;
	@Digits(integer = 9, fraction = 4)
	private BigDecimal estoqueMinimo= BigDecimal.ZERO;
	@Digits(integer = 9, fraction = 4)
	private BigDecimal estoqueMaximo= BigDecimal.ZERO;
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<ProdutoVariacao> variacoes = new HashSet<>();


}
