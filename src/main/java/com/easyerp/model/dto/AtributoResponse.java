package com.easyerp.model.dto;

import com.easyerp.domain.entidade.Atributo;

public record AtributoResponse(String chave, String valor) {
  public AtributoResponse(Atributo atributo) {
	this(atributo.getChave(), atributo.getValor());
}
}
