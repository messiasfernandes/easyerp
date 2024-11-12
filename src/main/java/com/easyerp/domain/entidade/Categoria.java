package com.easyerp.domain.entidade;

import java.io.Serial;

import com.easyerp.utils.TolowerCase;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Table(name = "tab_categorias")
@Entity
@Setter
@Getter
public class Categoria extends GeradorId {
	@Serial
	private static final long serialVersionUID = 1L;
	@Setter(value = AccessLevel.NONE)
	@Column(length = 60)
	private String categoriaNome;

//	@OneToMany(fetch = FetchType.EAGER, mappedBy = "categoria", cascade = CascadeType.ALL, orphanRemoval = true)  
//	private Set<SubCategoria> subcategorias = new HashSet<>();
	public void setNomeCategoria(String nomeCategoria) {
		this.categoriaNome = TolowerCase.normalizarString(nomeCategoria);
	}

	
}
