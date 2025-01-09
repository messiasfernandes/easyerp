package com.easyerp.domain.entidade;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import lombok.Getter;
import lombok.Setter;


@Entity
@Getter
@Setter

public class MovimentacaoEstoque {
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
    
    @OneToMany(mappedBy = "movimentacao", cascade = CascadeType.ALL)
    private Set<ItemMovimentacao> itens = new HashSet<>();;

    public enum TipoMovimentacao {
    	Entrada, Saida;
    }
}
