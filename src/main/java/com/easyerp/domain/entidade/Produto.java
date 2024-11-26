package com.easyerp.domain.entidade;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.easyerp.domain.enumerados.TipoProduto;
import com.easyerp.model.input.MarcaCadastroInput;
import com.easyerp.model.input.ProdutoCadastroInput;
import com.easyerp.model.input.SubCategoriaInput;
import com.easyerp.utils.TolowerCase;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
	
     @Column(length = 30)
	@Enumerated(EnumType.STRING)
	private TipoProduto tipoProduto;
	@ManyToOne(fetch = FetchType.EAGER, optional = true)
	@JoinColumn(name = "subcategoria_id")
	private SubCategoria subCategoria;
	@Digits(integer = 9, fraction = 4)
	private BigDecimal custo = BigDecimal.ZERO;
	@Digits(integer = 9, fraction = 4)
	private BigDecimal custoMedio = BigDecimal.ZERO;
	@Digits(integer = 9, fraction = 4)
	private BigDecimal precoVenda = BigDecimal.ZERO;
	@Digits(integer = 9, fraction = 4)
	private BigDecimal estoqueMinimo = BigDecimal.ZERO;
	@Digits(integer = 9, fraction = 4)
	private BigDecimal estoqueMaximo = BigDecimal.ZERO;
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<ProdutoVariacao> variacoes = new HashSet<>();

	public Produto() {

	}

	public Produto(ProdutoCadastroInput produtoCadastroInput) {
		this.produtoNome = TolowerCase.normalizarString(produtoCadastroInput.produto());
		this.precoVenda = produtoCadastroInput.precoVenda();
		this.custo = produtoCadastroInput.custo();
		this.custoMedio = produtoCadastroInput.custoMedio();
		this.marca = criarMarca(produtoCadastroInput.marca());
		this.estoque = criarEstoque();
		this.subCategoria = criarSubCategoria(produtoCadastroInput.subCategoria());
		this.variacoes = produtoCadastroInput.variacoes().stream().map(ProdutoVariacao::new)
				.collect(Collectors.toSet());
		if (variacoes.stream().anyMatch(v -> !v.getComponentes() .isEmpty())) {
			if(custo.signum()==0) {
           calcularPrecoCusto(produtoCadastroInput);
			}
			if(precoVenda.signum()==0) {
           calcularPrecoVenda(produtoCadastroInput);
			}
		}
	}

	private BigDecimal calcularPrecoVenda(ProdutoCadastroInput produtoCadastroInput) {
		 precoVenda = produtoCadastroInput.variacoes().stream()
				    .map(v -> 
				        v.componentes().stream()
				            .map(c -> c.precoVenda().multiply(c.qtde().add(c.custodeProducao())))
				            .reduce(BigDecimal.ZERO, BigDecimal::add)
				    )
				    .reduce(BigDecimal.ZERO, BigDecimal::add);
			return precoVenda;
					
		
	}

	private SubCategoria criarSubCategoria(SubCategoriaInput subCategoriaInput) {
		if (subCategoriaInput == null) {
			return null;
		}
		this.subCategoria = new SubCategoria();
		this.subCategoria.setId(subCategoriaInput.id());
		return subCategoria;
	}

	private Marca criarMarca(MarcaCadastroInput marcaInput) {
		if (marcaInput == null) {
			return null;
		}
		Marca marca = new Marca();
		marca.setId(marcaInput.id());
		return marca;
	}

	private Estoque criarEstoque() {
		Estoque estoque = new Estoque();
		estoque.setDataAlteracao(LocalDateTime.now());
		estoque.setDataCadastro(LocalDateTime.now());
		estoque.setQuantidade(BigDecimal.ZERO);
		estoque.setProduto(this);
		return estoque;
	}
	private BigDecimal calcularPrecoCusto (ProdutoCadastroInput produtoCadastroInput) {
		 custo = produtoCadastroInput.variacoes().stream()
			    .map(v -> 
			        v.componentes().stream()
			            .map(c -> c.precoCusto().multiply(c.qtde().add(c.custodeProducao())))
			            .reduce(BigDecimal.ZERO, BigDecimal::add)
			    )
			    .reduce(BigDecimal.ZERO, BigDecimal::add);
		return custo;
				
			//	variacaoCadastroInput.componentes().stream()
				//.map(c -> c.custodeProducao().add(c.precoCusto().multiply(c.qtde())))
				//.reduce(BigDecimal.ZERO, BigDecimal::add);
	}
}
