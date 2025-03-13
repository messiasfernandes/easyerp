package com.easyerp.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easyerp.config.ModelMapper;
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

		Produto produto = buscarProduto(movimentacaoInput.idProduto());

		MovimentacaoEstoque movimentacaoEstoque = movimentacaoEstoqueMapper.converter(movimentacaoInput,
				MovimentacaoEstoque::new);

		BigDecimal estoqueTotal = produto.getEstoque().getQuantidade();
		produto.getEstoque().setQuantidade(estoqueTotal.add(calcularTotalMovimentado(produto, movimentacaoInput)));
		this.verificarMovimentacao(movimentacaoEstoque, movimentacaoInput, produto, estoqueTotal);
		MovimentacaoEstoque movimetacaoSalva = movimentoEstoqueRepository.save(movimentacaoEstoque);
		return movimentacaoEstoqueMapper.converter(movimetacaoSalva, MovimentacaoResponse::new);
	}

	private void verificarMovimentacao(MovimentacaoEstoque movimentacaoEstoque, MovimentacaoInput movimentacaoInput,
			Produto produto, BigDecimal estoquTotal) {

		if (movimentacaoEstoque.getTipoMovimentacao().equals(TipoMovimentacao.Entrada)) {
			entradaEstoque(movimentacaoEstoque, movimentacaoInput, produto, estoquTotal);

		} else {

			saidaEstoque(movimentacaoEstoque, movimentacaoInput,produto, estoquTotal);
		}
	}

	private void entradaEstoque(MovimentacaoEstoque movimentacaoEstoque, MovimentacaoInput movimentacaoInput,
			Produto produto, BigDecimal estoquetotal) {
		for (var itemInput : movimentacaoInput.itens()) {
			ProdutoVariacao variacao = produtoVariacaoRepository.findById(itemInput.variacoes().id())
					.orElseThrow(() -> new NegocioException("Variação não encontrada"));
		

			variacao.setQtdeEstoque(variacao.getQtdeEstoque() + itemInput.qtde().intValue());
	
			var item = criarItemMovimentacao(movimentacaoEstoque, estoquetotal, variacao, itemInput.qtde(),
					movimentacaoInput.tipoMovimentacao(), estoquetotal);
			movimentacaoEstoque.getItens().add(item);
		}
		produto.getEstoque().setDataAlteracao(LocalDateTime.now());
		produtoRepository.save(produto);

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

	private void saidaEstoque(MovimentacaoEstoque movimentacaoEstoque, MovimentacaoInput movimentacaoInput,
	        Produto produto, BigDecimal estoqueTotal) {
	    BigDecimal totalMovimentado = calcularTotalMovimentado(produto, movimentacaoInput).abs();
	    if (estoqueTotal.compareTo(totalMovimentado) < 0) {
	        throw new NegocioException("Estoque insuficiente no produto");
	    }

	    for (var itemInput : movimentacaoInput.itens()) {
	        ProdutoVariacao variacao = produtoVariacaoRepository.findById(itemInput.variacoes().id())
	                .orElseThrow(() -> new NegocioException("Variação não encontrada"));
	        BigDecimal unidadesMovimentadas = itemInput.qtde().multiply(variacao.getQtdeporPacote());
	        BigDecimal estoqueVariacaoUnidades = BigDecimal.valueOf(variacao.getQtdeEstoque()).multiply(variacao.getQtdeporPacote());

	        // Validação rigorosa do estoque da variação
	        if (estoqueVariacaoUnidades.compareTo(unidadesMovimentadas) < 0) {
	            if (variacao.getQtdeporPacote().compareTo(BigDecimal.ONE) == 0) {
	                BigDecimal unidadesFaltantes = unidadesMovimentadas.subtract(estoqueVariacaoUnidades);
	                if (estoqueTotal.compareTo(unidadesFaltantes.add(estoqueVariacaoUnidades)) >= 0) {
	                    converterCaixasParaAvulso(produto, unidadesFaltantes, variacao);
	                } else {
	                    throw new NegocioException("Estoque insuficiente para a variação " + variacao.getDescricao());
	                }
	            } else {
	                throw new NegocioException("Estoque insuficiente para a variação " + variacao.getDescricao());
	            }
	        }

	        // Processa a subtração
	        if (variacao.getQtdeporPacote().compareTo(BigDecimal.ONE) == 0) {
	            variacao.setQtdeEstoque(variacao.getQtdeEstoque() - itemInput.qtde().intValue());
	            // Opcional: variacao.setQtdeEstoque(0); // Zera o estoque avulso após a venda
	        } else {
	            variacao.setQtdeEstoque(variacao.getQtdeEstoque() - itemInput.qtde().intValue());
	        }

	        var item = criarItemMovimentacao(movimentacaoEstoque, estoqueTotal, variacao, itemInput.qtde(),
	                movimentacaoInput.tipoMovimentacao(), estoqueTotal.subtract(totalMovimentado));
	        movimentacaoEstoque.getItens().add(item);
	    }
	    produto.getEstoque().setQuantidade(estoqueTotal.subtract(totalMovimentado));
	    produto.getEstoque().setDataAlteracao(LocalDateTime.now());
	    produtoRepository.save(produto);
	}

	private Produto buscarProduto(Long produtoId) {
		return produtoRepository.findById(produtoId)
				.orElseThrow(() -> new RegistroNaoEncontrado("Produto não encontrado para o ID: " + produtoId));
	}


	
		private ItemMovimentacao criarItemMovimentacao(MovimentacaoEstoque movimentacaoEstoque, BigDecimal saldoAnterior,
			ProdutoVariacao variacao, BigDecimal qtde, TipoMovimentacao tipoMovimentacao, BigDecimal totalEstoque) {
	
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

		private void converterCaixasParaAvulso(Produto produto, BigDecimal unidadesFaltantes, ProdutoVariacao variacaoAvulsa) {
		    System.out.println("Unidades faltantes iniciais: " + unidadesFaltantes);
		    AtomicBoolean conversaoRealizada = new AtomicBoolean(false);
		    for (ProdutoVariacao variacao : produto.getVariacoes()) {
		        System.out.println("Variação ID: " + variacao.getId() + ", qtdePorPacote: " + variacao.getQtdeporPacote() + ", qtdeEstoque: " + variacao.getQtdeEstoque());
		        if (variacao.getQtdeporPacote().compareTo(BigDecimal.ONE) > 0 && unidadesFaltantes.compareTo(BigDecimal.ZERO) > 0) {
		            BigDecimal unidadesPorCaixa = variacao.getQtdeporPacote();
		            BigDecimal divisao = unidadesFaltantes.divide(unidadesPorCaixa, 4, RoundingMode.CEILING);
		            int caixasNecessarias = divisao.setScale(0, RoundingMode.CEILING).intValue();
		            BigDecimal unidadesConvertidas = unidadesPorCaixa.multiply(BigDecimal.valueOf(caixasNecessarias));
		            System.out.println("Caixas necessárias: " + caixasNecessarias + ", Unidades convertidas: " + unidadesConvertidas);

		            if (variacao.getQtdeEstoque() >= caixasNecessarias) {
		                variacao.setQtdeEstoque(variacao.getQtdeEstoque() - caixasNecessarias);
		                variacaoAvulsa.setQtdeEstoque(variacaoAvulsa.getQtdeEstoque() + unidadesConvertidas.intValue());
		                unidadesFaltantes = unidadesFaltantes.subtract(unidadesConvertidas);
		                System.out.println("Convertido: Nova qtdeEstoque da variação " + variacao.getId() + ": " + variacao.getQtdeEstoque() + ", Avulsa: " + variacaoAvulsa.getQtdeEstoque() + ", Faltantes: " + unidadesFaltantes);
		                conversaoRealizada.set(true);
		            } else {
		                System.out.println("Estoque insuficiente na variação " + variacao.getId());
		            }
		        } else {
		            System.out.println("Variação " + variacao.getId() + " ignorada: qtdePorPacote <= 1 ou unidadesFaltantes <= 0");
		        }
		    }
		    if (unidadesFaltantes.compareTo(BigDecimal.ZERO) > 0) {
		        if (!conversaoRealizada.get()) {
		            throw new NegocioException("Estoque insuficiente para a variação " + variacaoAvulsa.getDescricao());
		        } else {
		            throw new NegocioException("Não há caixas suficientes para converter " + unidadesFaltantes + " unidades avulsas");
		        }
		    }
		}
}
