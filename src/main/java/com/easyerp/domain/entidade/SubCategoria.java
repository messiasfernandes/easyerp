package com.easyerp.domain.entidade;




import com.easyerp.utils.TolowerCase;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Table(name = "tab_subcategorias")
@Entity
@Getter
@Setter

public class SubCategoria extends GeradorId {

	private static final long serialVersionUID = 1L;
	@Setter(value = AccessLevel.NONE)

	@Column(length = 60, nullable = false)
	private String subcategoriaNome;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn
	private Categoria categoria;

	public void setSubcategoriaNOme(String subcategoriaNome) {
		this.subcategoriaNome = TolowerCase.normalizarString(subcategoriaNome);
	}

//	public SubCategoria(SubCategoriaCreateInput subCategoriaCreateInput) {
//
//		System.out.println(subCategoriaCreateInput.subcategoria());
////		if (subCategoriaCreateInput.idCategoria() != null) {
////			this.categoria = new Categoria();
////			this.categoria.setId(subCategoriaCreateInput.idCategoria());
////		}
//
//		// Verifica se o nome da subcategoria não é nulo ou vazio
//		if (subCategoriaCreateInput.subcategoria() != null
//				&& !subCategoriaCreateInput.subcategoria().trim().isEmpty()) {
//			this.subcategoriaNome = TolowerCase.normalizarString(subCategoriaCreateInput.subcategoria());
//		} else {
//			throw new IllegalArgumentException("O nome da subcategoria não pode estar vazio");
//		}
//
//	}
//
//	public SubCategoria() {
//		// TODO Auto-generated constructor stub
//	}
}
