package com.easyerp.domain.entidade;

import java.io.Serial;

import com.easyerp.model.input.MarcaCadastroInput;
import com.easyerp.utils.TolowerCase;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
@Table(name =  "tab_marcas")
@Getter
@Setter
@Entity
public class Marca extends GeradorId {

	@Serial
	private static final long serialVersionUID = 1L;
	@Column(length = 60, nullable = false)
	@Setter(value = AccessLevel.NONE)
	private String nomeMarca;

	public void setNomeMarca(String nomeMarca) {
		this.nomeMarca = TolowerCase.normalizarString(nomeMarca);
	}

	public Marca(MarcaCadastroInput marcaCadastroInput) {

		this.nomeMarca = TolowerCase.normalizarString(marcaCadastroInput.marca());
	}
	public Marca() {
		// TODO Auto-generated constructor stub
	}
}
