package com.easyerp.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.easyerp.domain.entidade.UnidadeMedida;
@Repository
public interface UnidadedeMedidaRepository extends JpaRepository<UnidadeMedida, Long> {

}
