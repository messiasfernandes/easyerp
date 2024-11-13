package com.easyerp.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.easyerp.config.ModelMapper;
import com.easyerp.domain.entidade.Produto;
import com.easyerp.domain.repository.ProdutoRepository;
import com.easyerp.model.dto.ProdutoResponse;
import com.easyerp.model.input.ProdutoCadastroInput;

@Service
public class ProdutoService {
	@Autowired
	private ProdutoRepository produtoRepository;
	@Autowired
	private ModelMapper produtoMapper;
	public Page<ProdutoResponse>listar(String produtoNome, Pageable pageable) {
		return produtoMapper.convertPage(     produtoRepository.buscarProdutos(produtoNome, pageable) ,ProdutoResponse::new );
	}
   public ProdutoResponse salvar( ProdutoCadastroInput produtoCadastroInput) {
	   var produto = produtoMapper.converter(produtoCadastroInput, Produto::new);
	   return produtoMapper.converter(produtoRepository.save(produto), ProdutoResponse::new );
	
}
}
