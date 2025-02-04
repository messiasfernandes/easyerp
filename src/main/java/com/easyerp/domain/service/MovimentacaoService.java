package com.easyerp.domain.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easyerp.config.ModelMapper;
import com.easyerp.domain.entidade.Estoque;
import com.easyerp.domain.entidade.ItemMovimentacao;
import com.easyerp.domain.entidade.MovimentacaoEstoque;
import com.easyerp.domain.entidade.Produto;
import com.easyerp.domain.entidade.ProdutoVariacao;
import com.easyerp.domain.enumerados.TipoMovimentacao;
import com.easyerp.domain.enumerados.TipoProduto;
import com.easyerp.domain.repository.MovimentoEstoqueRepository;
import com.easyerp.domain.repository.ProdutoRepository;
import com.easyerp.domain.service.exeption.NegocioException;
import com.easyerp.domain.service.exeption.RegistroNaoEncontrado;
import com.easyerp.model.dto.MovimentacaoResponse;
import com.easyerp.model.input.MovimentacaoInput;

import jakarta.transaction.Transactional;

@Service
public class MovimentacaoService {
	@Autowired
	private MovimentoEstoqueRepository movimentoEstoqueRepository;
	@Autowired
	private ModelMapper movimentacaoEstoqueMapper;
	@Autowired
	private ProdutoRepository produtoRepository;

	@Transactional
	public MovimentacaoResponse registrarMovimentacao(MovimentacaoInput movimentacaoInput) {
		validarMovimentacaoInput(movimentacaoInput);
		MovimentacaoEstoque movimentacaoEstoque = movimentacaoEstoqueMapper.converter(movimentacaoInput,
				MovimentacaoEstoque::new);
		Produto produto = buscarProduto(movimentacaoInput.idProduto());

		verificarMovimentacao(movimentacaoEstoque, produto, movimentacaoInput);

		MovimentacaoEstoque movimetacaoSalva = movimentoEstoqueRepository.save(movimentacaoEstoque);
		return movimentacaoEstoqueMapper.converter(movimetacaoSalva, MovimentacaoResponse::new);
	}

	private void verificarMovimentacao(MovimentacaoEstoque movimentacaoEstoque, Produto produto,
			MovimentacaoInput movimentacaoInput) {

		if (movimentacaoEstoque.getTipoMovimentacao().equals(TipoMovimentacao.Entrada)) {
			entradaEstoque(movimentacaoEstoque, movimentacaoInput, produto);

		} else {

			saidaEstoque(movimentacaoEstoque, movimentacaoInput, produto);
		}
	}

	private ProdutoVariacao buscarVariacao(Produto produto, Long idVariacao) {
		return produto.getVariacoes().stream().filter(v -> v.getId().equals(idVariacao)).findFirst()
				.orElseThrow(() -> new NegocioException("Variação não encontrada para o ID: " + idVariacao));
	}

	private ItemMovimentacao criarItemMovimentacao(MovimentacaoEstoque movimentacaoEstoque, BigDecimal saldoAnterior,
			ProdutoVariacao variacao, BigDecimal qtde, TipoMovimentacao tipoMovimentacao) {
		ItemMovimentacao item = new ItemMovimentacao();
		if (qtde.signum() != 0) {

			item.setMovimentacao(movimentacaoEstoque);
			item.setProdutoVariacao(variacao);

			item.setQuantidade((qtde));
			item.setSaldoanterior(saldoAnterior);
		}

		return item;
	}

	private void atualizarQuantidadeVariacao(ProdutoVariacao variacao, BigDecimal qtde,
			TipoMovimentacao tipoMovimentacao) {
		if (tipoMovimentacao == TipoMovimentacao.Entrada) {

			variacao.setQtdeEstoque(
					variacao.getQtdeEstoque() + qtde.intValue() * variacao.getQtdeporPacote().intValue());

		} else {
			if (variacao.getQtdeEstoque() < qtde.intValue()) {
				throw new NegocioException("Quantidade insuficiente no estoque da variação: " + variacao.getId());
			}

			variacao.setQtdeEstoque(variacao.calcularEstoque(   variacao.getQtdeEstoque() - qtde.intValue()));
		}

	}

	private Produto buscarProduto(Long produtoId) {
		return produtoRepository.findById(produtoId)
				.orElseThrow(() -> new RegistroNaoEncontrado("Produto não encontrado para o ID: " + produtoId));
	}

	private void validarMovimentacaoInput(MovimentacaoInput movimentacaoInput) {
		if (movimentacaoInput == null || movimentacaoInput.itens().isEmpty()) {
			throw new NegocioException("A movimentação deve conter ao menos um item.");
		}

		if (movimentacaoInput.idProduto() == null) {
			throw new NegocioException("ID do produto não pode ser nulo.");
		}
	}

	private Estoque inicializarEstoque(Produto produto) {
		Estoque estoque = new Estoque();
		estoque.setQuantidade(BigDecimal.ZERO);
		estoque.setDataAlteracao(LocalDateTime.now());
		estoque.setDataCadastro(LocalDateTime.now());
		estoque.setProduto(produto);
		produto.setEstoque(estoque);
		return estoque;
	}

	private void atualizarEstoqueProdutoEvariacao(Estoque estoque, BigDecimal quantidade, ItemMovimentacao item) {
   System.out.println(quantidade+ "qtde vinda");
  
		estoque.setQuantidade(
				estoque.getQuantidade().subtract(quantidade.multiply(item.getProdutoVariacao().getQtdeporPacote())));


		if (item.getProdutoVariacao().getProduto().getTipoProduto().equals(TipoProduto.Kit)) {
			
			item.getProdutoVariacao()
					.setQtdeEstoque(item.getProdutoVariacao().calcularEstoque(estoque.getQuantidade().intValue()));
			
			 System.out.println(item.getProdutoVariacao().getQtdeEstoque()+ "qtde saida");
		}

		if (estoque.getQuantidade().compareTo(quantidade) < 0) {
			throw new NegocioException("Quantidade insuficiente no estoque.");
		}

	}

	private void entradaEstoque(MovimentacaoEstoque movimentacaoEstoque, MovimentacaoInput movimentacaoInput,
			Produto produto) {
		Integer variacaoEstoque = 0;
		Estoque estoque = new Estoque();
		if (produto.getEstoque() == null) {
		
			estoque = inicializarEstoque(produto);
		} else {
			estoque = produto.getEstoque();
		}

		BigDecimal qteAnterior = estoque.getQuantidade();

		if (movimentacaoInput.qtdeProduto().signum() != 0) {

			estoque.setQuantidade(estoque.getQuantidade().add(movimentacaoInput.qtdeProduto()));

		}

		if (produto.getTipoProduto().equals(TipoProduto.Kit)) {
			for (var varicao : produto.getVariacoes()) {

				if (varicao.getQtdeEstoque() == 0) {
					variacaoEstoque = estoque.getQuantidade().intValue();
				} else {
					variacaoEstoque = varicao.calcularEstoque(estoque.getQuantidade().intValue());

				}
				varicao.setQtdeEstoque(varicao.getQtdeEstoque() + varicao.calcularEstoque(variacaoEstoque));
				var item = new ItemMovimentacao();
			//	item= criarItemMovimentacao(movimentacaoEstoque,new BigDecimal( variacaoEstoque), varicao, qteAnterior, movimentacaoEstoque.getTipoMovimentacao());
				
				item.setQuantidade(new BigDecimal(variacaoEstoque));
				item.setMovimentacao(movimentacaoEstoque);
				item.setSaldoanterior(qteAnterior);
				item.setProdutoVariacao(varicao);

			
				movimentacaoEstoque.getItens().add(item);

			}
		}

		else

		{

			movimentacaoInput.itens().forEach(itemIp -> {
				ProdutoVariacao variacao = buscarVariacao(produto, itemIp.variacoes().id());
				ItemMovimentacao item = criarItemMovimentacao(movimentacaoEstoque, qteAnterior, variacao, itemIp.qtde(),

						movimentacaoInput.tipoMovimentacao());

				movimentacaoEstoque.getItens().add(item);
				atualizarQuantidadeVariacao(variacao, itemIp.qtde(), movimentacaoInput.tipoMovimentacao());
			});

			BigDecimal somaVariacoes = movimentacaoInput.itens().stream().map(varicao -> varicao.qtde())
					.reduce(BigDecimal.ZERO, BigDecimal::add);

			if (somaVariacoes.compareTo(movimentacaoInput.qtdeProduto()) != 0) {
				throw new NegocioException(
						"A soma das quantidades das variações excede a quantidade total em estoque.");
			}

		}
		produto.setEstoque(estoque);
	//	estoque.setProduto(produto);
		estoque.setDataAlteracao(LocalDateTime.now());

		produtoRepository.save(produto);
	}

	private void saidaEstoque(MovimentacaoEstoque movimentacaoEstoque, MovimentacaoInput movimentacaoInput,
			Produto produto) {
		Estoque estoque = new Estoque();
		if (produto.getEstoque() == null) {
			throw new NegocioException("Produto não possui estoque.");
		}

		else {
			estoque = produto.getEstoque();
		}
		BigDecimal qteAnterior = estoque.getQuantidade();
	
		movimentacaoInput.itens().forEach(itemIp -> {
			ProdutoVariacao variacao = buscarVariacao(produto, itemIp.variacoes().id());
			ItemMovimentacao item = criarItemMovimentacao(movimentacaoEstoque, qteAnterior, variacao, itemIp.qtde(),

					movimentacaoInput.tipoMovimentacao());

			movimentacaoEstoque.getItens().add(item);
			atualizarQuantidadeVariacao(variacao, itemIp.qtde(), movimentacaoInput.tipoMovimentacao());
			System.out.println(produto.getEstoque().getQuantidade()+ "quatidade no estoque total");
			
	  	produto.getEstoque().setQuantidade(	somaQuatidadeVariacao(variacao,  produto.getEstoque()));
			atualizarEstoqueProdutoEvariacao(produto.getEstoque(), item.getQuantidade(), item);
			System.out.println(produto.getEstoque().getQuantidade()+ "quatidade atualizada na variacao");
		});

		estoque.setDataAlteracao(LocalDateTime.now());
		produtoRepository.save(produto);

	}

	private BigDecimal somaQuatidadeVariacao(ProdutoVariacao variacao,Estoque estoque) {
	     estoque.setQuantidade(estoque.getQuantidade().subtract(new BigDecimal(variacao.getQtdeEstoque())));
	     System.out.println(estoque.getQuantidade()+"soma quantidade");
		return  estoque.getQuantidade();
		
	}
}