package com.easyerp.domain.entidade;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import lombok.Getter;
import lombok.Setter;
@Table(name = "tab_produtos_variacoes")
@Getter
@Setter
@Entity
public class ProdutoVariacao extends GeradorId {

	@Serial
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(nullable = false)
	private Produto produto = new Produto();
	@Column(length = 255)
	private String descricao;
	@Column(length = 13)
	private String codigoEan13;
	@Digits(integer = 9, fraction = 4)
	private BigDecimal custoAdicional = BigDecimal.ZERO;
	@Digits(integer = 9, fraction = 4)
	private BigDecimal qtdeporPacote = BigDecimal.ONE;
	@Digits(integer = 9, fraction = 4)
	private BigDecimal qtdeEstoque = BigDecimal.ONE;
	@Column(length = 200)
	private String imagemProduto;
	@ManyToOne(fetch = FetchType.EAGER, optional = true)
	private UnidadeMedida unidadeMedida;
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "tab_variacao_atributo", joinColumns = @JoinColumn(name = "variacao_id"))
	@BatchSize(size = 10)
	private Set<Atributo> atributos = new HashSet<>();
	@Fetch(FetchMode.SUBSELECT)
	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinTable(name = "variacao_componente", joinColumns = @JoinColumn(name = "variacao_id"), inverseJoinColumns = @JoinColumn(name = "componente_id"))
	private Set<Componente> componentes = new HashSet<>();
}
