package com.easyerp.model.input;

import java.beans.Transient;

public record CategoriaInput(Long id , @Transient String categoria) {

}
