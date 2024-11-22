package com.easyerp.domain.entidade;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import com.easyerp.model.input.ComponenteCadastroInput;
import com.easyerp.model.input.VariacaoCadastroInput;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import lombok.Getter;
import lombok.Setter;

@Table(name = "tab_componentes")
@Setter
@Getter
@Entity
public class Componente extends GeradorId {

	@Serial
	private static final long serialVersionUID = 1L;

	@Digits(integer = 9, fraction = 3)
	@Column
	private BigDecimal qtde;

	@Digits(integer = 9, fraction = 3)
	private BigDecimal custodeProducao;
	@ManyToOne(fetch = FetchType.EAGER)
	private ProdutoVariacao variacao;
	@ManyToMany(fetch = FetchType.EAGER, mappedBy = "componentes")
	private Set<ProdutoVariacao> variacoes = new HashSet<>();

	public Componente(ComponenteCadastroInput componenteCadastroInput) {
		this.custodeProducao = componenteCadastroInput.custodeProducao();
		this.qtde = componenteCadastroInput.qtde();
		variacao = new ProdutoVariacao();
		variacao.getProduto()
				.setCusto(componenteCadastroInput.precoCusto().multiply(componenteCadastroInput.precoCusto()).multiply(componenteCadastroInput.qtde()));
		var valor=componenteCadastroInput.precoCusto().multiply(componenteCadastroInput.qtde());
		
		System.out.println(valor+ "custo" + qtde  );
		variacao.getProduto().setPrecoVenda(componenteCadastroInput.precoVenda()
				.multiply(componenteCadastroInput.qtde().add(componenteCadastroInput.custodeProducao())));
		variacao.getProduto().setCusto(valor);
		variacao.setId(componenteCadastroInput.produtoId());
		variacao.getComponentes().forEach(c -> c.setVariacao(variacao));
		//variacao.getComponentes().add(this);
		
	}

	public Componente() {

	}
	

}
