package com.easyerp.model.input;

import jakarta.persistence.Transient;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.EAN;

import java.math.BigDecimal;
import java.util.Set;

public record ProdutoVAlterar(Long id,
                              @Size(min = 13, message = "Codigo tem que 13 dígitos")
                              @EAN                @Transient
                              String codigoEan13, String caracterisca, Integer multiplo, String sku, BigDecimal qtdeEstoque) {

}
