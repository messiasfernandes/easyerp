package com.easyerp.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.easyerp.domain.entidade.Produto;
import com.easyerp.domain.query.ProdutoRepositoryCustom;
import com.easyerp.domain.query.ProdutoRepositoryProjectionCustom;
@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long>,   ProdutoRepositoryCustom {

//	@Query("SELECT new com.esyerp.model.dto.ProdutoResponse(p.id, p.produtoNome, new com.esyerp.model.MarcaResponse(m.id, m.nomeMarca), "
//			+ "p.custo, p.custoMedio, p.precoVenda) FROM Produto p LEFT JOIN p.marca m ")
//	Page<ProdutoResponse>pesquisa(Pageable page);
}
