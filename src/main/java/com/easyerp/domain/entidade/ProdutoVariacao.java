package com.easyerp.domain.entidade;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.easyerp.domain.service.exeption.NegocioException;
import com.easyerp.model.input.VariacaoCadastroInput;
import com.easyerp.utils.CodigoBarraEAN;
import com.easyerp.utils.GeradordeCodigo;
import com.easyerp.utils.TolowerCase;

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
public class ProdutoVariacao implements Serializable {

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
	private BigDecimal qtdeEstoque = BigDecimal.ZERO;
	@Column(length = 200)
	private String imagemProduto;
	@ManyToOne(fetch = FetchType.EAGER, optional = true)
	private UnidadeMedida unidadeMedida = new UnidadeMedida();
	private Boolean ativo = true;
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "tab_variacao_atributo", joinColumns = @JoinColumn(name = "variacao_id"))
	@BatchSize(size = 10)
	private Set<Atributo> atributos = new HashSet<>();
	@Fetch(FetchMode.SUBSELECT)
	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinTable(name = "variacao_componente", joinColumns = @JoinColumn(name = "variacao_id"), inverseJoinColumns = @JoinColumn(name = "componente_id"))
	private Set<Componente> componentes = new HashSet<>();

	public ProdutoVariacao(VariacaoCadastroInput variacaoCadastroInput) {
		if ((variacaoCadastroInput.codigoEan13() == null || variacaoCadastroInput.codigoEan13().isBlank())) {
			this.codigoEan13 = GeradordeCodigo.CriarEAN13();
		} else {
			// 2. Caso contrário, use o código fornecido (ou null se não houver):
			this.codigoEan13 = Optional.ofNullable(variacaoCadastroInput.codigoEan13())
					.filter(codigo -> !codigo.isBlank()).map(CodigoBarraEAN::new).flatMap(CodigoBarraEAN::validar)
					.map(CodigoBarraEAN::getCodigoBarra)
					.orElseThrow(() -> new NegocioException("codigo Ean  digitado e inválido"));
		}
		if (!variacaoCadastroInput.componentes().isEmpty()) {
			this.componentes = variacaoCadastroInput.componentes().stream().map(Componente::new)
					.collect(Collectors.toSet());
			this.componentes.forEach(componente -> componente.getVariacoes().add(this));
		}
		if (variacaoCadastroInput.custoAdicional().signum() > 0) {
			this.custoAdicional = variacaoCadastroInput.custoAdicional();
		}
		
		this.atributos = variacaoCadastroInput.atributos().stream().map(Atributo::new).collect(Collectors.toSet());
		this.qtdeporPacote = variacaoCadastroInput.qtdeporPacote();
		this.ativo = variacaoCadastroInput.ativo();
		this.unidadeMedida.setId(variacaoCadastroInput.unidadeInput().id());
		this.descricao = TolowerCase.normalizarString(variacaoCadastroInput.descricao());
		
	}

	public ProdutoVariacao() {

	}

}
