package com.easyerp.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.easyerp.domain.entidade.MovimentacaoEstoque;

public interface MovimentoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

}
