package com.easyerp.domain.query;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.easyerp.domain.entidade.Produto;
import com.easyerp.utils.ServiceFuncoes;
import com.easyerp.utils.TolowerCase;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

public class ProdutoRepositoryCustomImpl extends ServiceFuncoes  implements ProdutoRepositoryCustom{
	@PersistenceContext
	private EntityManager entityManager;
	@Override
	public Page<Produto> buscarProdutos(String parametro, Pageable page) {
		  boolean isNumeric = parametro != null && ehnumero(parametro);
          boolean isEAN = isNumeric && qtdecaraceteres(parametro) == 13;
          boolean isId = isNumeric && qtdecaraceteres(parametro) != 13;
          List<Long> produtoIds = fetchProdutoIds(parametro, page, isNumeric, isEAN, isId);
    System.out.println(produtoIds);
  		long total = countProdutos(parametro, isNumeric, isEAN, isId);

  		List<Produto> produtos = fetchProdutos(produtoIds, parametro, isEAN, isId);;

          return new PageImpl<>(produtos, page, total);
      }
      private List<Produto> fetchProdutos(List<Long> produtoIds, String parametro, boolean isEAN, boolean isId) {
          StringBuilder queryBuilder = new StringBuilder();
          queryBuilder.append("SELECT DISTINCT p FROM Produto p ");

          queryBuilder.append("LEFT JOIN FETCH p.marca m  ");
          queryBuilder.append("LEFT JOIN FETCH p.subCategoria s  ");
          queryBuilder.append("LEFT JOIN FETCH s.categoria c  ");
        queryBuilder.append("LEFT JOIN FETCH p.estoque e  ");
          queryBuilder.append("LEFT JOIN FETCH p.variacoes v ");
          queryBuilder.append("LEFT JOIN FETCH v.atributos a ");
          queryBuilder.append("LEFT JOIN FETCH v.componentes vc ");
          queryBuilder.append("LEFT JOIN FETCH v.unidadeMedida u ");
       queryBuilder.append("LEFT JOIN FETCH vc.variacoes cv ");
      //    queryBuilder.append("LEFT JOIN FETCH vc.variacao pv ");
      //   queryBuilder.append("LEFT JOIN FETCH pv.produto prov ");
   //   queryBuilder.append("LEFT JOIN FETCH prov.subCategoria sp ");
         // queryBuilder.append("LEFT JOIN FETCH sp.categoria cat ");
       // queryBuilder.append("LEFT JOIN FETCH pv.atributos av ");
       //   queryBuilder.append("LEFT JOIN FETCH pv.produto pr ");
          queryBuilder.append("WHERE p.id IN :ids ");

          if (isEAN) {
              queryBuilder.append("AND v.codigoEan13 = :parametro ");
          } else if (isId) {
              queryBuilder.append("AND p.id = :parametro ");
          }

          queryBuilder.append("ORDER BY p.produtoNome");

          TypedQuery<Produto> produtoQuery = entityManager.createQuery(queryBuilder.toString(), Produto.class);
          produtoQuery.setParameter("ids", produtoIds);
          if (isEAN || isId) {
              produtoQuery.setParameter("parametro", isEAN ? parametro : Long.valueOf(parametro));
          }
System.out.println(produtoIds);
          return produtoQuery.getResultList();
      }
      private List<Long> fetchProdutoIds(String parametro, Pageable page, boolean isNumeric, boolean isEAN, boolean isId) {

    	  String baseQuery = "SELECT p.id FROM Produto p  LEFT JOIN p.subCategoria s ";
    	  System.out.println("primeira consulta");
        parametro= TolowerCase.normalizarString(parametro);
          String condition = getCondition(parametro, isNumeric, isEAN, isId);
          String idQueryStr = baseQuery + condition;
          //   + " ORDER BY p.produtoNome";
          
        
          TypedQuery<Long> idQuery = entityManager.createQuery(idQueryStr, Long.class);

          setParameter(idQuery, parametro, isNumeric, isEAN, isId);

          idQuery.setFirstResult((int) page.getOffset());
          idQuery.setMaxResults(page.getPageSize());

          return idQuery.getResultList();
      }

      private String getCondition(String parametro, boolean isNumeric, boolean isEAN, boolean isId) {

          if (parametro != null && !parametro.isEmpty()) {
              if (!isNumeric && qtdecaraceteres(parametro) > 0) {
            	   return " WHERE p.produtoNome LIKE :parametro OR "
            	  		+ "p.subCategoria.subcategoriaNome LIKE :parametro OR p.marca.nomeMarca LIKE :parametro " ;

              }
              if (isEAN) {
            	  System.out.println("passou aqui ");
                  return " LEFT JOIN p.variacoes v WHERE v.codigoEan13 = :parametro";
              }
              if (isId) {
                  return " WHERE p.id = :id";
              }
          }
          return "";
      }
      private void setParameter(TypedQuery<?> query, String parametro, boolean isNumeric, boolean isEAN, boolean isId) {
          if (parametro != null && !parametro.isEmpty()) {
              if (isEAN || isId) {
                  query.setParameter(isEAN ? "parametro" : "id", isEAN ? parametro : Long.valueOf(parametro));
              } else {
            	  System.out.println(parametro +"paramentro");
                  query.setParameter("parametro", "%" + parametro + "%");
              }
          }
      }

      private long countProdutos(String parametro, boolean isNumeric, boolean isEAN, boolean isId) {
    	  String countQuery = "SELECT COUNT(p) FROM Produto p LEFT JOIN  p.subCategoria s";
          System.out.println("segunda consulta");
          String condition = getCondition(parametro, isNumeric, isEAN, isId);

          String countQueryStr = countQuery + condition;
          TypedQuery<Long> countTypedQuery = entityManager.createQuery(countQueryStr, Long.class);

          setParameter(countTypedQuery, parametro, isNumeric, isEAN, isId);

          return countTypedQuery.getSingleResult();
      }
  }


