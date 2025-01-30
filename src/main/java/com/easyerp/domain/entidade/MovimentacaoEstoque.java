package com.easyerp.domain.entidade;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.easyerp.domain.enumerados.TipoMovimentacao;
import com.easyerp.model.input.MovimentacaoInput;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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

	public MovimentacaoEstoque(MovimentacaoInput movimentacaoInput) {
		this.dataMovimentacao= LocalDateTime.now();
		this.tipoMovimentacao= movimentacaoInput.tipoMovimentacao();
		this.observacao= movimentacaoInput.observacao();

	}

	public MovimentacaoEstoque() {
		// TODO Auto-generated constructor stub
	}
}
