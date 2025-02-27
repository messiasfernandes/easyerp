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
		
		Produto produto = buscarProduto(movimentacaoInput.idProduto());
		validarMovimentacaoInput(movimentacaoInput);
		MovimentacaoEstoque movimentacaoEstoque = movimentacaoEstoqueMapper.converter(movimentacaoInput,
				MovimentacaoEstoque::new);
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
		System.out.println();
		ItemMovimentacao item = new ItemMovimentacao();
		if (qtde.signum() != 0) {

			item.setMovimentacao(movimentacaoEstoque);
			item.setProdutoVariacao(variacao);

			item.setQuantidade(qtde);
           System.out.println("saldo anterior "+ saldoAnterior);
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
		
			variacao.setQtdeEstoque(variacao.calcularEstoque(variacao.getQtdeEstoque() - qtde.intValue()));
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

	private void atualizarEstoqueProdutoEvariacaoKit(Estoque estoque, BigDecimal quantidade, ItemMovimentacao item) {

		estoque.setQuantidade(
				estoque.getQuantidade().subtract(quantidade.multiply(item.getProdutoVariacao().getQtdeporPacote())));

		for (var v : item.getProdutoVariacao().getProduto().getVariacoes()) {
			v.setQtdeEstoque(v.calcularEstoque(estoque.getQuantidade().intValue()));

		}

		if (estoque.getQuantidade().intValue() < (item.getProdutoVariacao().getQtdeEstoque())) {
			throw new NegocioException("Quantidade insuficiente no estoque." + estoque.getQuantidade() + "variacao "
					+ item.getProdutoVariacao().getQtdeEstoque());
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
		System.out.println("saldo anterio" + qteAnterior);
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
				System.out.println("Varaiação Estoque" + variacaoEstoque);
				varicao.setQtdeEstoque(varicao.getQtdeEstoque() + varicao.calcularEstoque(variacaoEstoque));
				var item = new ItemMovimentacao();
				item = criarItemMovimentacao(movimentacaoEstoque, qteAnterior, varicao, new BigDecimal(variacaoEstoque),
						movimentacaoEstoque.getTipoMovimentacao());
				System.out.println("saldo anterior " + item.getSaldoanterior());
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
			produto.getEstoque().setQuantidade(produto.getEstoque().getQuantidade().subtract(itemIp.qtde()));
			if (produto.getTipoProduto().equals(TipoProduto.Kit)) {
				atualizarEstoqueProdutoEvariacaoKit(produto.getEstoque(), item.getQuantidade(), item);
			}

		});
		produto.setEstoque(estoque);
		estoque.setDataAlteracao(LocalDateTime.now());
		produtoRepository.save(produto);

	}

}