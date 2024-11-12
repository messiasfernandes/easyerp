package com.easyerp.domain.query;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.easyerp.domain.entidade.Produto;
import com.easyerp.domain.entidade.ProdutoVariacao;
import com.easyerp.model.dto.MarcaResponse;
import com.easyerp.model.dto.ProdutoResponse;
import com.easyerp.model.dto.ProdutoVariacaoResponse;
import com.easyerp.utils.ServiceFuncoes;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

public class ProdutoRepositoryProjectionCustomImpl  extends ServiceFuncoes implements ProdutoRepositoryProjectionCustom   {
	@PersistenceContext
	private EntityManager entityManager;
	@Override
	public Page<ProdutoResponse> buscarProdutos(String parametro, Pageable page) {
        List<Long> produtoIds = fetchProdutoIds(parametro, page);

        TypedQuery<ProdutoVariacao> variacaoQuery = entityManager.createQuery(
                "SELECT DISTINCT  v FROM ProdutoVariacao v LEFT JOIN FETCH v.atributos a   WHERE v.produto.id IN :produtoIds", ProdutoVariacao.class);
        variacaoQuery.setParameter("produtoIds", produtoIds);
        List<ProdutoVariacao> variacoes = variacaoQuery.getResultList();

        Map<Long, Set<ProdutoVariacaoResponse>> variacoesPorProduto = variacoes.stream()
                .map(ProdutoVariacaoResponse::new) // Construtor do ProdutoVariacaoResponse recebendo ProdutoVariacao
                .collect(Collectors.groupingBy(
                        ProdutoVariacaoResponse::id,
                        Collectors.toSet()));

        TypedQuery<Produto> produtoQuery = entityManager.createQuery(
                        "SELECT p FROM Produto p LEFT JOIN FETCH p.marca  LEFT JOIN FETCH p.subCategoria c  LEFT JOIN FETCH p.variacoes v   WHERE p.id IN :produtoIds", Produto.class)
                .setParameter("produtoIds", produtoIds);
                
        produtoQuery.setFirstResult((int) page.getOffset());
        produtoQuery.setMaxResults(page.getPageSize());
        List<Produto> produtosList = produtoQuery.getResultList();



        List<ProdutoResponse> resultados = produtosList.stream()
                .map(p -> new ProdutoResponse(
                        p.getId(),
                        p.getProdutoNome(),
                        p.getMarca() != null ? new MarcaResponse(p.getMarca().getId(), p.getMarca().getNomeMarca()) : null,
                        p.getCusto(),
                        p.getCustoMedio(),
                        p.getPrecoVenda(),
                        variacoesPorProduto.getOrDefault(p.getId(), Collections.emptySet())
                ))
                .toList();

        long total = countProdutos(parametro);

        return new PageImpl<>(resultados, page, total);
    }


    private List<Long> fetchProdutoIds(String parametro, Pageable page) {
        String baseQuery = "SELECT p.id FROM Produto p LEFT JOIN p.subCategoria s ";
        String condition = getCondition(parametro); // Removido desnecessário isNumeric, isEAN, isId
        String idQueryStr = baseQuery + condition;
        TypedQuery<Long> idQuery = entityManager.createQuery(idQueryStr, Long.class);
        setParameter(idQuery, parametro); // Removido desnecessário isNumeric, isEAN, isId
        return idQuery.getResultList(); // Removida a paginação aqui
    }


    private String getCondition(String parametro) {
        if (parametro != null && !parametro.isEmpty()) {
               return " WHERE lower(p.produtoNome) LIKE lower(:parametro) OR "
          	  		+ "lower(s.subcategoriaNome) LIKE lower(:parametro)";
        }
        return "";
    }

    private void setParameter(TypedQuery<?> query, String parametro) {
        if (parametro != null && !parametro.isEmpty()) {
                query.setParameter("parametro", "%" + parametro + "%");

        }
    }

    private long countProdutos(String parametro) {
        String countQuery = "SELECT COUNT(p) FROM Produto p LEFT JOIN p.subCategoria s LEFT JOIN p.marca m";
        String condition = getCondition(parametro);
        String countQueryStr = countQuery + condition;
        TypedQuery<Long> countTypedQuery = entityManager.createQuery(countQueryStr, Long.class);
        setParameter(countTypedQuery, parametro);
        return countTypedQuery.getSingleResult();
    }
}
