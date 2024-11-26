package com.easyerp.model.input;

import java.beans.Transient;

public record MarcaInput(Long id, @Transient String marca) {

}
