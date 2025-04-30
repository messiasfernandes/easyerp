package com.easyerp.model.input;

import java.math.BigDecimal;

import org.hibernate.validator.constraints.EAN;

import jakarta.persistence.Transient;
import jakarta.validation.constraints.Size;

public record ProdutoVAlterar(Long id,
                              @Size(min = 13, message = "Codigo tem que 13 dígitos")
                              @EAN                
                              String codigoEan13,  Integer qtdeporPacote,UnidadeMedidaInput unidade,   String sku, BigDecimal qtdeEstoque) {

}
