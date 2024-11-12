package com.easyerp.domain.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.easyerp.model.dto.ProdutoResponse;

public interface ProdutoRepositoryProjectionCustom {
	Page<ProdutoResponse>buscarProdutos(String parametro, Pageable page);
}
