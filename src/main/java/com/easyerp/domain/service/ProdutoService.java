
package com.easyerp.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.easyerp.config.ModelMapper;
import com.easyerp.domain.entidade.Produto;
import com.easyerp.domain.repository.ProdutoRepository;
import com.easyerp.domain.service.exeption.EntidadeEmUsoExeption;
import com.easyerp.domain.service.exeption.RegistroNaoEncontrado;
import com.easyerp.model.dto.ProdutoResponse;
import com.easyerp.model.input.ProdutoCadastroInput;
import com.easyerp.model.input.ProdutoEditarInput;
import com.easyerp.utils.ValidarProduto;

import jakarta.transaction.Transactional;

@Service
public class ProdutoService {
	@Autowired
	private ProdutoRepository produtoRepository;
	@Autowired
	private ModelMapper produtoMapper;

	public Page<ProdutoResponse> listar(String produtoNome, Pageable pageable) {
		return produtoMapper.convertPage(produtoRepository.buscarProdutos(produtoNome, pageable), ProdutoResponse::new);
	}

	@Transactional(rollbackOn = { Exception.class })
	public ProdutoResponse salvar(ProdutoCadastroInput produtoCadastroInput) {
		produtoCadastroInput.validar();
		var produto = produtoMapper.converter(produtoCadastroInput, Produto::new);
		
		if (!produto.getVariacoes().isEmpty()) {
			produto.getVariacoes().forEach(p -> p.setProduto(produto));

		}

		var produtoSalvo = produtoRepository.save(produto);
		return produtoMapper.converter(produtoSalvo, ProdutoResponse::new);

	}

	@Transactional(dontRollbackOn = { Exception.class })
	public ProdutoResponse atualizar(ProdutoEditarInput produtoEditarInput) {
		Produto produtoExistente = produtoRepository.getReferenceById(produtoEditarInput.id());
	       ValidarProduto validarProduto = new ValidarProduto();
	        validarProduto.validar(produtoExistente, produtoEditarInput);


	        produtoExistente.setCusto(produtoEditarInput.custo() != null ? produtoEditarInput.custo() : null);
	        produtoExistente.setCustoMedio(produtoEditarInput.custoMedio() != null ? produtoEditarInput.custoMedio() : null);
	        produtoExistente.setPrecoVenda(produtoEditarInput.precoVenda() != null ? produtoEditarInput.precoVenda() : null);

	      
	        var produtosalvo = produtoRepository.save(produtoExistente);
		
		return produtoMapper.converter(produtosalvo, ProdutoResponse::new);
	}

	public void excluir(Long id) {
		buscarPorId(id);
		try {
			produtoRepository.deleteById(id);
			produtoRepository.flush();
		} catch (DataIntegrityViolationException e) {
			throw new EntidadeEmUsoExeption(
					"Operação não permitida!! Este registro pode estar associado a outra tabela");
		}

	}

  

	public ProdutoResponse buscarPorId(Long id) {

		var produto = produtoRepository.findById(id)
				.orElseThrow(() -> new RegistroNaoEncontrado("Produto não encontrado"));
		return produtoMapper.converter(produto, ProdutoResponse::new);
	}
}
