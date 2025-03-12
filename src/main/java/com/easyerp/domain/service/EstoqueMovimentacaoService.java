package com.easyerp.domain.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

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
import com.easyerp.domain.repository.ProdutoVariacaoRepository;
import com.easyerp.domain.service.exeption.NegocioException;
import com.easyerp.domain.service.exeption.RegistroNaoEncontrado;
import com.easyerp.model.dto.MovimentacaoResponse;
import com.easyerp.model.input.MovimentacaoInput;

import jakarta.transaction.Transactional;

@Service
public class EstoqueMovimentacaoService {

	@Autowired
	private MovimentoEstoqueRepository movimentoEstoqueRepository;
	@Autowired
	private ModelMapper movimentacaoEstoqueMapper;
	@Autowired
	private ProdutoRepository produtoRepository;
	@Autowired
	private ProdutoVariacaoRepository produtoVariacaoRepository;

	@Transactional
	public MovimentacaoResponse registroMovimentacao(MovimentacaoInput movimentacaoInput) {

		Produto produto = buscarProduto(movimentacaoInput.idProduto());

		MovimentacaoEstoque movimentacaoEstoque = movimentacaoEstoqueMapper.converter(movimentacaoInput,
				MovimentacaoEstoque::new);
		this.verificarMovimentacao(movimentacaoEstoque, produto, movimentacaoInput);
		MovimentacaoEstoque movimetacaoSalva = movimentoEstoqueRepository.save(movimentacaoEstoque);
		return movimentacaoEstoqueMapper.converter(movimetacaoSalva, MovimentacaoResponse::new);
	}

	private Produto buscarProduto(Long produtoId) {
		return produtoRepository.findById(produtoId)
				.orElseThrow(() -> new RegistroNaoEncontrado("Produto não encontrado para o ID: " + produtoId));
	}

	private void verificarMovimentacao(MovimentacaoEstoque movimentacaoEstoque, Produto produto,
			MovimentacaoInput movimentacaoInput) {

		if (movimentacaoEstoque.getTipoMovimentacao().equals(TipoMovimentacao.Entrada)) {
			entradaEstoque(movimentacaoEstoque, movimentacaoInput, produto);

		} else {

			saidaEstoque(movimentacaoEstoque, movimentacaoInput, produto);
		}
	}

	private void saidaEstoque(MovimentacaoEstoque movimentacaoEstoque, MovimentacaoInput movimentacaoInput,
			Produto produto) {
		// TODO Auto-generated method stub

	}

	private void entradaEstoque(MovimentacaoEstoque movimentacaoEstoque, MovimentacaoInput movimentacaoInput,
			Produto produto) {
		if (produto.getTipoProduto().equals(TipoProduto.Kit)) {
			processarItemKit(movimentacaoEstoque, movimentacaoInput, produto);
		} else {
			processaItem(movimentacaoEstoque, movimentacaoInput, produto);
		}

	}

	private ProdutoVariacao buscarVariacao(Produto produto, Long idVariacao) {
		return produto.getVariacoes().stream().filter(v -> v.getId().equals(idVariacao)).findFirst()
				.orElseThrow(() -> new NegocioException("Variação não encontrada para o ID: " + idVariacao));
	}

	private void processaItem(MovimentacaoEstoque movimentacaoEstoque, MovimentacaoInput movimentacaoInput,
			Produto produto) {
		Estoque estoque = new Estoque();
		estoque = atualizarQtdeTotal(produto, estoque);
		BigDecimal qteAnterior = estoque.getQuantidade();
		BigDecimal totalMovimentado = calcularTotalMovimentado(produto, movimentacaoInput);
		estoque.setQuantidade(estoque.getQuantidade().add(totalMovimentado));

		movimentacaoInput.itens().forEach(itemIp -> {

			var variacao = atualizaQtdeVariacao(produto, itemIp.qtde(), itemIp.variacoes().id(),
					movimentacaoEstoque.getTipoMovimentacao());
			ItemMovimentacao item = criarItemMovimentacao(movimentacaoEstoque, qteAnterior, variacao, itemIp.qtde(),
					movimentacaoInput.tipoMovimentacao(), totalMovimentado);

			movimentacaoEstoque.getItens().add(item);
		}

		);
		estoque.setDataAlteracao(LocalDateTime.now());
		produto.setEstoque(estoque);

	}

	private void processarItemKit(MovimentacaoEstoque movimentacaoEstoque, MovimentacaoInput movimentacaoInput,
			Produto produto) {
		Estoque estoque = new Estoque();
		estoque = atualizarQtdeTotal(produto, estoque);
		BigDecimal qteAnterior = estoque.getQuantidade();
		BigDecimal totalMovimentado = calcularTotalMovimentado(produto, movimentacaoInput);
		estoque.setQuantidade(estoque.getQuantidade().add(totalMovimentado));

		movimentacaoInput.itens().forEach(itemIp -> {

			var variacao = atualizaQtdeVariacao(produto, itemIp.qtde(), itemIp.variacoes().id(),
					movimentacaoEstoque.getTipoMovimentacao());

			ItemMovimentacao item = criarItemMovimentacao(movimentacaoEstoque, qteAnterior, variacao, itemIp.qtde(),
					movimentacaoInput.tipoMovimentacao(), totalMovimentado);

			movimentacaoEstoque.getItens().add(item);

			// AtualizarUnidadeKit(movimentacaoEstoque, qteAnterior, movimentacaoInput,
			// itemIp.qtde(), produto,
			// totalMovimentado);
			// }

		}

		);

		for (var variacao : produto.getVariacoes()) {
			if (variacao.getQtdeporPacote().compareTo(BigDecimal.ONE) == 0) {
				
				
				variacao.setQtdeEstoque(variacao.getQtdeEstoque() + totalMovimentado.intValue());
				ItemMovimentacao item = criarItemMovimentacao(movimentacaoEstoque, qteAnterior, variacao,
						totalMovimentado, movimentacaoInput.tipoMovimentacao(), totalMovimentado);
//
				movimentacaoEstoque.getItens().add(item);
			}

		}
		estoque.setDataAlteracao(LocalDateTime.now());
		produto.setEstoque(estoque);

	}

	private ProdutoVariacao AtualizaUnidadeKit(MovimentacaoEstoque movimentacaoEstoque, BigDecimal qteAnterior,
			MovimentacaoInput movimentacaoInput, BigDecimal qtde, ProdutoVariacao variacao,
			BigDecimal totalMovimentado) {

		movimentacaoInput.itens().stream().filter(item -> {
			System.out.println("Filtrando item com variação ID: " + item.variacoes().id());
			return item.variacoes().id().equals(variacao.getId());
		}).forEach(item -> {
			BigDecimal qtdeItem = Objects.requireNonNullElse(item.qtde(), BigDecimal.ZERO);
			System.out.println("Verificando variação: " + variacao.getId() + ", Qtde por Pacote: "
					+ variacao.getQtdeporPacote() + ", Qtde Item: " + qtdeItem);

			if (variacao.getQtdeporPacote().compareTo(BigDecimal.ONE) == 0) {
				if (qtdeItem.compareTo(BigDecimal.ZERO) == 0) {
					System.out.println("Caso 1: Pacote = 1 e Quantidade = 0");
					variacao.setQtdeEstoque(variacao.getQtdeEstoque() + totalMovimentado.intValue());
				} else {
					System.out.println("Caso 2: Pacote = 1 e Quantidade > 0");
					variacao.setQtdeEstoque(variacao.getQtdeEstoque() + qtdeItem.intValue());
				}
			} else if (variacao.getQtdeporPacote().compareTo(BigDecimal.ONE) > 0) {
				System.out.println("Caso 3: Pacote > 1");
				variacao.setQtdeEstoque(variacao.getQtdeEstoque() + qtdeItem.intValue());
			}
		});
		return variacao;
	}

	private void AtualizarUnidadeKit(MovimentacaoEstoque movimentacaoEstoque, BigDecimal qteAnterior,
			MovimentacaoInput movimentacaoInput, BigDecimal qtde, Produto produto, BigDecimal totalMovimentado) {

		for (var variacao : produto.getVariacoes()) {

			// Se qtde == 0 e pacote == 1
			if (qtde.compareTo(BigDecimal.ZERO) == 0 && variacao.getQtdeporPacote().compareTo(BigDecimal.ONE) == 0) {
				variacao.setQtdeEstoque(variacao.getQtdeEstoque() + totalMovimentado.intValue());
				System.out.println("Movimentação atualizada com 0: " + variacao.getQtdeEstoque());
			}
			// Se qtde > 0 e pacote == 1
			else if (qtde.compareTo(BigDecimal.ZERO) > 0
					&& variacao.getQtdeporPacote().compareTo(BigDecimal.ONE) == 0) {
				variacao.setQtdeEstoque(variacao.getQtdeEstoque() + qtde.intValue());
				System.out.println("Movimentação atualizada: " + variacao.getQtdeEstoque());
			}

			// Criar item da movimentação
			ItemMovimentacao item = criarItemMovimentacao(movimentacaoEstoque, qteAnterior, variacao, qtde,
					movimentacaoInput.tipoMovimentacao(), totalMovimentado);
			movimentacaoEstoque.getItens().add(item);
		}
	}

	private ProdutoVariacao atualizaQtdeVariacao(Produto produto, BigDecimal qtde, Long id, TipoMovimentacao tipo) {
		var varicacaoEncontrada = buscarVariacao(produto, id);
		if (tipo.equals(TipoMovimentacao.Entrada)) {
			varicacaoEncontrada.setQtdeEstoque(varicacaoEncontrada.getQtdeEstoque() + qtde.intValue());
		} else {
			varicacaoEncontrada.setQtdeEstoque(varicacaoEncontrada.getQtdeEstoque() - qtde.intValue());
		}

		return varicacaoEncontrada;
	}

	private BigDecimal calcularTotalMovimentado(Produto produto, MovimentacaoInput movimentacaoInput) {
		BigDecimal total = movimentacaoInput.itens().stream().map(itemImp -> {
			// Buscar a variação correta dentro do Set<ProdutoVariacao>
			ProdutoVariacao variacao = produto.getVariacoes().stream()
					.filter(v -> v.getId().equals(itemImp.variacoes().id())).findFirst()
					.orElseThrow(() -> new RuntimeException(
							"Variação não encontrada para o ID: " + itemImp.variacoes().id()));

			BigDecimal qtde = itemImp.qtde() != null ? itemImp.qtde() : BigDecimal.ZERO;
			BigDecimal qtdePorPacote = variacao.getQtdeporPacote() != null ? variacao.getQtdeporPacote()
					: BigDecimal.ONE;

			return qtde.multiply(qtdePorPacote); // Multiplica quantidade pelo tamanho do pacote
		}).reduce(BigDecimal.ZERO, BigDecimal::add);
		return total;
	}

	private Estoque atualizarQtdeTotal(Produto produto, Estoque estoque) {
		if (produto.getEstoque() == null) {

			estoque = inicializarEstoque(produto);
		} else {
			estoque = produto.getEstoque();
		}

		return estoque;

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

	private ItemMovimentacao criarItemMovimentacao(MovimentacaoEstoque movimentacaoEstoque, BigDecimal saldoAnterior,
			ProdutoVariacao variacao, BigDecimal qtde, TipoMovimentacao tipoMovimentacao, BigDecimal totalEstoque) {
		System.out.println();
		ItemMovimentacao item = new ItemMovimentacao();
		item.setSaldoanterior(saldoAnterior);
		item.setProdutoVariacao(variacao);
		item.setQuantidade(qtde);
		item.setMovimentacao(movimentacaoEstoque);

		return item;
	}

	private void atualizarVariacao(Produto produto, BigDecimal qtde, Long id, TipoMovimentacao tipo) {
		var varicacaoEncontrada = buscarVariacao(produto, id);
		if (tipo.equals(TipoMovimentacao.Entrada)) {
			
		}
	}
}
