package com.easyerp.model.input;

import jakarta.validation.constraints.NotNull;

public record MarcaCadastroInput(Long id, @NotNull  String marca) {
}
