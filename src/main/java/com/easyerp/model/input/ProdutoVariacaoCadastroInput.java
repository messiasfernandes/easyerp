package com.easyerp.model.input;

import java.util.Set;

import org.hibernate.validator.constraints.EAN;

import jakarta.validation.constraints.NotBlank;

public record ProdutoVariacaoCadastroInput(Long id, @NotBlank @EAN String codigoEan13, 
		 Integer multiplicador,String imagemProduto, Integer qtdeEstoque,
		 Set<CaracteristicasInput>caracteristicas) {

}
