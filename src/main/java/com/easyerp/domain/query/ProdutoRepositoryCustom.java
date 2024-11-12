package com.easyerp.domain.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.easyerp.domain.entidade.Produto;

public interface ProdutoRepositoryCustom {
	Page<Produto>buscarProdutos(String parametro, Pageable page);
}
