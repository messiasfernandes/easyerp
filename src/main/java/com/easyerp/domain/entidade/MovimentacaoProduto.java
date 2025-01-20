package com.easyerp.domain.entidade;

import java.time.LocalDateTime;

import com.easyerp.domain.enumerados.TipoMovimentacao;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class MovimentacaoProduto {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dataMovimentacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private TipoMovimentacao tipoMovimentacao;

    @Column(columnDefinition = "TEXT")
    private String observacao;
    

}
