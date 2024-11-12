package com.easyerp.domain.entidade;


import com.easyerp.utils.TolowerCase;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Table(name = "tab_unidade_medidas")
@Getter
@Setter
@Entity
public class UnidadeMedida extends GeradorId {

	private static final long serialVersionUID = 1L;

	@Column(length = 80)
	@Setter(value = AccessLevel.NONE)
	private String embalageNome;

	@Column(length = 20)
	@Setter(value = AccessLevel.NONE)
	private String embalageSigla;

	public void setEmbalageNome(String embalageNome) {
		this.embalageNome = TolowerCase.normalizarString(embalageNome);
	}
	public void setEmbalageSigla(String embalageSigla) {
		this.embalageSigla = TolowerCase.normalizarString(embalageSigla);
	}

}