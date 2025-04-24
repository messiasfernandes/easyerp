package com.easyerp.model.input;

import java.math.BigDecimal;

import org.hibernate.validator.constraints.EAN;

import jakarta.persistence.Transient;
import jakarta.validation.constraints.Size;

public record ProdutoVAlterar(Long id,
                              @Size(min = 13, message = "Codigo tem que 13 dígitos")
                              @EAN                @Transient
                              String codigoEan13, String caracterisca, Integer qtdeporPacote, String sku, BigDecimal qtdeEstoque) {

}
