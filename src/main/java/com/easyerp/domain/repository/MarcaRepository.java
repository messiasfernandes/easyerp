package com.easyerp.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.easyerp.domain.entidade.Marca;

public interface MarcaRepository extends JpaRepository<Marca, Long>, JpaSpecificationExecutor <Marca>{
	@Query("from Marca m where m.nomeMarca = :nome")
	Marca buscar(String nome);

}
