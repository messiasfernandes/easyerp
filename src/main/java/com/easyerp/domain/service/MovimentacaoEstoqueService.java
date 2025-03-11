package com.easyerp.domain.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easyerp.config.ModelMapper;
import com.easyerp.domain.entidade.Estoque;
import com.easyerp.domain.entidade.ItemMovimentacao;
import com.easyerp.domain.entidade.MovimentacaoEstoque;
import com.easyerp.domain.entidade.Produto;
import com.easyerp.domain.entidade.ProdutoVariacao;
import com.easyerp.domain.enumerados.TipoMovimentacao;
import com.easyerp.domain.repository.MovimentoEstoqueRepository;
import com.easyerp.domain.repository.ProdutoRepository;
import com.easyerp.domain.repository.ProdutoVariacaoRepository;
import com.easyerp.domain.service.exeption.NegocioException;
import com.easyerp.domain.service.exeption.RegistroNaoEncontrado;
import com.easyerp.model.dto.MovimentacaoResponse;
import com.easyerp.model.input.MovimentacaoInput;

import jakarta.transaction.Transactional;

@Service
public class MovimentacaoEstoqueService {
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

		// Produto produto = buscarProduto(movimentacaoInput.idProduto());

		MovimentacaoEstoque movimentacaoEstoque = movimentacaoEstoqueMapper.converter(movimentacaoInput,
				MovimentacaoEstoque::new);
		 this.verificarMovimentacao(movimentacaoEstoque, movimentacaoInput);
		MovimentacaoEstoque movimetacaoSalva = movimentoEstoqueRepository.save(movimentacaoEstoque);
		return movimentacaoEstoqueMapper.converter(movimetacaoSalva, MovimentacaoResponse::new);
	}

	private void verificarMovimentacao(MovimentacaoEstoque movimentacaoEstoque, MovimentacaoInput movimentacaoInput) {

		if (movimentacaoEstoque.getTipoMovimentacao().equals(TipoMovimentacao.Entrada)) {
			entradaEstoque(movimentacaoEstoque, movimentacaoInput);

		} else {

			saidaEstoque(movimentacaoEstoque, movimentacaoInput);
		}
	}

	private void entradaEstoque(MovimentacaoEstoque movimentacaoEstoque, MovimentacaoInput movimentacaoInput) {
		Estoque estoque = new Estoque();
		
	
		
		movimentacaoInput.itens().forEach(
				
				itemP-> {
					ProdutoVariacao variacao = new ProdutoVariacao();
					 variacao = produtoVariacaoRepository.getReferenceById(itemP.variacoes().id());
					 BigDecimal qteAnterior =variacao.getProduto().getEstoque().getQuantidade();
						BigDecimal totalMovimentado = calcularTotalMovimentado(variacao.getProduto(), movimentacaoInput);
						variacao.getProduto().getEstoque().setQuantidade(variacao.getProduto().getEstoque().getQuantidade()
								.add(itemP.qtde()));
					if(variacao.getQtdeporPacote().compareTo(BigDecimal.ONE)>1) {
						System.out.println("if 1 ");
						variacao.setQtdeEstoque(variacao.getQtdeEstoque()+itemP.qtde().intValue());
					}
					if(variacao.getQtdeporPacote().compareTo(BigDecimal.ONE)==0) {
						System.out.println("if 2 ");
						variacao.setQtdeEstoque(variacao.getQtdeEstoque()+itemP.qtde().intValue());
					}
				
					var item = criarItemMovimentacao(movimentacaoEstoque, qteAnterior, variacao, qteAnterior,
							movimentacaoEstoque.getTipoMovimentacao(), totalMovimentado);
					movimentacaoEstoque.getItens().add(item);
					
					variacao.getProduto().getEstoque().setDataAlteracao(LocalDateTime.now());
					produtoVariacaoRepository.save(variacao);
					produtoRepository.save(variacao.getProduto());
				});
		
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

	private void saidaEstoque(MovimentacaoEstoque movimentacaoEstoque, MovimentacaoInput movimentacaoInput) {
	}

	private Estoque atualizarQtdeTotal(Produto produto, Estoque estoque) {
		if (produto.getEstoque() == null) {

			estoque = inicializarEstoque(produto);
		} else {
			estoque = produto.getEstoque();
		}

		return estoque;

	}

	private void atualizarQuantidadeVariacao(ProdutoVariacao variacao, BigDecimal qtde,
			TipoMovimentacao tipoMovimentacao) {

		if (tipoMovimentacao == TipoMovimentacao.Entrada) {

			variacao.setQtdeEstoque(qtde.intValue());

		} else {
			if (variacao.getQtdeEstoque() < qtde.intValue()) {
				throw new NegocioException("Quantidade insuficiente no estoque da variação: " + variacao.getId());
			}

			variacao.setQtdeEstoque(variacao.calcularEstoque(variacao.getQtdeEstoque() - qtde.intValue()));
		}

	}

	public boolean verificarKit(Produto produto) {
		AtomicBoolean kit = new AtomicBoolean(false); // Objeto mutável

		produto.getVariacoes().forEach(v -> {
			if (v.getQtdeporPacote().compareTo(BigDecimal.ONE) == 0) {
				kit.set(true); // Modifica o valor
			}
		});

		return kit.get(); // Retorna o valor
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

	private ProdutoVariacao buscarVariacao(Produto produto, Long idVariacao) {
		return produto.getVariacoes().stream().filter(v -> v.getId().equals(idVariacao)).findFirst()
				.orElseThrow(() -> new NegocioException("Variação não encontrada para o ID: " + idVariacao));
	}

	private Produto buscarProduto(Long produtoId) {
		return produtoRepository.findById(produtoId)
				.orElseThrow(() -> new RegistroNaoEncontrado("Produto não encontrado para o ID: " + produtoId));
	}

	private ProdutoVariacao ValidarUnidade(Produto produto, int Qtde) {
		ProdutoVariacao[] variacao = new ProdutoVariacao[1]; // Array de um único elemento

		produto.getVariacoes().forEach(v -> {
			if (v.getQtdeporPacote().compareTo(BigDecimal.ONE) == 0) {
				v.setQtdeEstoque(v.getQtdeEstoque() + Qtde);
				variacao[0] = v; // Armazena o valor no array
			}
		});
		System.out.println("Variação modificada: " + variacao[0].getQtdeEstoque());
		return variacao[0];
		// Agora você pode usar variacao[0] fora do lambda

	}

	private MovimentacaoEstoque processarItemKit(MovimentacaoInput movimentacaoInput,
			MovimentacaoEstoque movimentacaoEstoque, Produto produto, BigDecimal saldoAterior) {
		/// Integer variacaoEstoque =produto. getEstoque().getQuantidade().intValue();
		for (var varicao : produto.getVariacoes()) {

			for (var iemP : movimentacaoInput.itens()) {
				if ((iemP.variacoes().id().equals(varicao.getId())
						&& (varicao.getQtdeporPacote().compareTo(BigDecimal.ONE) == 0))) {
					varicao.setQtdeEstoque(varicao.getQtdeEstoque() + movimentacaoInput.qtdeProduto().intValue());
				}
				if (iemP.qtde().compareTo(BigDecimal.ZERO) == 0) {
					varicao.setQtdeEstoque(saldoAterior.intValue() + iemP.qtde().intValue());
				} else {
					varicao.setQtdeEstoque(
							varicao.getQtdeEstoque() + (produto.getEstoque().getQuantidade().intValue()));
				}

				var item = new ItemMovimentacao();
				item = criarItemMovimentacao(movimentacaoEstoque, saldoAterior, varicao, iemP.qtde(),
						movimentacaoEstoque.getTipoMovimentacao(), null);
				System.out.println("saldo anterior " + item.getSaldoanterior());
				movimentacaoEstoque.getItens().add(item);
			}

		}

		return movimentacaoEstoque;
	}

	private MovimentacaoEstoque processarItem(MovimentacaoInput movimentacaoInput,
			MovimentacaoEstoque movimentacaoEstoque, Produto produto, BigDecimal saldoAterior) {

		// Obtém as variações do produto
		Set<ProdutoVariacao> variacoesDoProduto = produto.getVariacoes();
		int i = 0;
		int y = 0;
		// Processa as variações que possuem qtdePorPacote igual a 1
		for (ProdutoVariacao variacao : variacoesDoProduto) {
			for (var item : movimentacaoInput.itens()) {
				// Verifica se o id da variação do item confere com a variação atual
				// e se a quantidade por pacote é 1
				if (item.variacoes().id().equals(variacao.getId())
						|| variacao.getQtdeporPacote().compareTo(BigDecimal.ONE) == 1 && y == 0) {
					y++;
					System.out.println(y + "passou unitario");
					// Cria um ItemMovimentacao para registrar a movimentação
					ItemMovimentacao itemMovimentacao = new ItemMovimentacao();
					itemMovimentacao.setQuantidade(item.qtde());

					// Atualiza o estoque: soma o saldo anterior com o estoque já registrado na
					// variação
					// variacao.setQtdeEstoque(saldoAterior.intValue() +
					// variacao.getQtdeEstoque().intValue());
					atualizarQuantidadeVariacao(variacao, itemMovimentacao.getQuantidade(),
							movimentacaoInput.tipoMovimentacao());
					// Se necessário, calcula o estoque em kit (caso essa operação seja diferente)
					// variacao.calcularEstoqueKit(movimentacaoInput.qtdeProduto().intValue());

					// Registra os demais valores
					itemMovimentacao.setProdutoVariacao(variacao);
					itemMovimentacao.setSaldoanterior(saldoAterior);
					itemMovimentacao.setMovimentacao(movimentacaoEstoque);

					movimentacaoEstoque.getItens().add(itemMovimentacao);
				}
			}
		}

		// Processa as variações que possuem qtdePorPacote diferente de 1
		for (var item : movimentacaoInput.itens()) {
			Long idVariacao = item.variacoes().id();
			Optional<ProdutoVariacao> variacaoEncontrada = variacoesDoProduto.stream()
					.filter(v -> v.getId().equals(idVariacao) && v.getQtdeporPacote().intValue() != 1).findFirst();

			if (variacaoEncontrada.isPresent()) {
				i++;
				System.out.println(i + "passou kit");
				ProdutoVariacao variacao = variacaoEncontrada.get();
				ItemMovimentacao itemMovimentacao = new ItemMovimentacao();
				itemMovimentacao.setQuantidade(item.qtde());
				itemMovimentacao.setProdutoVariacao(variacao);
				itemMovimentacao.setSaldoanterior(saldoAterior);
				itemMovimentacao.setMovimentacao(movimentacaoEstoque);
				variacao.calcularEstoque(item.qtde().intValue());
				movimentacaoEstoque.getItens().add(itemMovimentacao);
			}
		}
		return movimentacaoEstoque;
	}

	private ItemMovimentacao criarItemMovimentacao(MovimentacaoEstoque movimentacaoEstoque, BigDecimal saldoAnterior,
			ProdutoVariacao variacao, BigDecimal qtde, TipoMovimentacao tipoMovimentacao, BigDecimal totalEstoque) {
		System.out.println();
		ItemMovimentacao item = new ItemMovimentacao();

		item.setMovimentacao(movimentacaoEstoque); // ✅ Sempre definir primeiro!
		item.setProdutoVariacao(variacao);
		item.setSaldoanterior(saldoAnterior);

		if (qtde.signum() != 0) {
			// Se qtde for diferente de zero, criar normalmente
			item.setQuantidade(qtde);
		} else if (qtde.compareTo(BigDecimal.ZERO) == 0 && variacao.getQtdeporPacote().compareTo(BigDecimal.ONE) == 0) {
			// Só criar o item se realmente houver necessidade
			if (totalEstoque.signum() != 0) {
				item.setQuantidade(totalEstoque);
			} else {
				return null; // ❌ Não cria um item vazio
			}
		}
		return item;
	}
}
