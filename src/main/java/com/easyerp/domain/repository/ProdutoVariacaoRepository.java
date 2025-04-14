package com.easyerp.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.easyerp.domain.entidade.ProdutoVariacao;


public interface ProdutoVariacaoRepository extends JpaRepository<ProdutoVariacao, Long> {
	@Query("FROM ProdutoVariacao pv WHERE pv.produto.id = :id AND pv.qtdeporPacote = 1 and pv.produto.tipoProduto =Kit")
	Boolean buscaqtePacote(@Param("id") Long id);
	
	@Query("SELECT COUNT(pv) > 0 FROM ProdutoVariacao pv WHERE pv.id = :id AND pv.qtdeporPacote = 1 AND pv.produto.tipoProduto = 'Kit'")
	boolean existeVariacaoComQtdePacote(@Param("id") Long id);
}
