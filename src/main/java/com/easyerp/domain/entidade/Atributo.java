package com.easyerp.domain.entidade;



import com.easyerp.model.input.AtributoCadastroInput;
import com.easyerp.utils.TolowerCase;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode
@Getter
@Setter
@Embeddable
public class Atributo {

	@Setter(value = AccessLevel.NONE)
	@Column(length = 60)
	private String chave;

	@Setter(value = AccessLevel.NONE)
	@Column(length = 60)
	private String valor;

	public Atributo() {

	}

	public Atributo(AtributoCadastroInput atributoInput) {
		this.chave = TolowerCase.normalizarString(atributoInput.chave());
		this.valor = TolowerCase.normalizarString(atributoInput.valor());
	}


}
