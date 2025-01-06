package com.easyerp.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.easyerp.domain.entidade.MovimentacaoEstoque;
@Repository
public interface MovimentoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

}
