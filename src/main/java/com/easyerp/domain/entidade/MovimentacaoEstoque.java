package com.easyerp.domain.entidade;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;


@Entity
@Data

public class MovimentacaoEstoque {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dataMovimentacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimentacao tipoMovimentacao;

    @Column(columnDefinition = "TEXT")
    private String observacao;
    
    @OneToMany(mappedBy = "movimentacao", cascade = CascadeType.ALL)
    private List<ItemMovimentacao> itens;

    public enum TipoMovimentacao {
    	ENTRADA, SAIDA;
    }
}
