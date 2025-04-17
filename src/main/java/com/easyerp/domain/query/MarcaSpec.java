package com.easyerp.domain.query;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.easyerp.domain.entidade.Marca;

import org.springframework.util.StringUtils;
import jakarta.persistence.criteria.Predicate;

public class MarcaSpec {
	 private List <Predicate> predicates = new ArrayList <>();
	    public Specification <Marca> buscarId(Long id) {

	        return (root, query, builder) -> {
	            if (id != null) {
	                predicates.add(builder.equal(root.get("id"), id));

	            }
	            return builder.and(predicates.toArray(new Predicate[0]));
	        };

	    }
	    public Specification<Marca> buscar(String nome) {

	        return (root, query, builder) -> {

	            return builder.like(root.get("nomeMarca"),  nome + "%");

	        };

	    }

	    public Specification<Marca> buscarMarca(String nome) {

	        return (root, query, builder) -> {
	            if (StringUtils.hasText(nome)) {
	                System.out.println("passou aqui");
	                predicates.add(builder.like(root.get("nomeMarca"), nome + "%"));
	            }
	            return builder.and(predicates.toArray(new Predicate[0]));

	        };
	    }

}
