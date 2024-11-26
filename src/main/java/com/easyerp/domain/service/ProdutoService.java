package com.easyerp.domain.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.easyerp.config.ModelMapper;
import com.easyerp.domain.entidade.Componente;
import com.easyerp.domain.entidade.Produto;
import com.easyerp.domain.entidade.ProdutoVariacao;
import com.easyerp.domain.repository.ProdutoRepository;
import com.easyerp.domain.service.exeption.EntidadeEmUsoExeption;
import com.easyerp.domain.service.exeption.RegistroNaoEncontrado;
import com.easyerp.model.dto.ProdutoResponse;
import com.easyerp.model.input.ProdutoCadastroInput;
import com.easyerp.model.input.ProdutoEditarInput;
import com.easyerp.model.input.VariacaoCadastroInput;

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
		return produtoMapper.converter(produtoExistente, ProdutoResponse::new);
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

	private void calcularPrecos(Produto produto, VariacaoCadastroInput variacaoCadastroInput) {
		if (!variacaoCadastroInput.componentes().isEmpty()) {
			// Soma o custo de produção para cada componente
			BigDecimal custoTotalComponentes = variacaoCadastroInput.componentes().stream()
					.map(c -> c.custodeProducao().add(c.precoCusto().multiply(c.qtde())))
					.reduce(BigDecimal.ZERO, BigDecimal::add);

			// Calcular os preços e custos
			BigDecimal precoVenda = custoTotalComponentes.multiply(BigDecimal.valueOf(1.30)); // Exemplo: 30% de margem
			BigDecimal precoCusto = custoTotalComponentes; // Igual ao custo total dos componentes
			BigDecimal custoMedio = precoCusto.divide(BigDecimal.valueOf(variacaoCadastroInput.componentes().size()), 2,
					BigDecimal.ROUND_HALF_UP);

			// Setar os valores calculados no produto
			produto.setCusto(precoCusto);
			produto.setPrecoVenda(precoVenda);
			produto.setCustoMedio(custoMedio);
		} else {
			// Caso não haja componentes, configure valores padrões, se necessário
			produto.setCusto(null);
			produto.setPrecoVenda(BigDecimal.ZERO);
			produto.setCustoMedio(BigDecimal.ZERO);
		}
	}

	public ProdutoResponse buscarPorId(Long id) {

		var produto = produtoRepository.findById(id)
				.orElseThrow(() -> new RegistroNaoEncontrado("Produto não encontrado"));
		return produtoMapper.converter(produto, ProdutoResponse::new);
	}
}
