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

	@Transactional
	public MovimentacaoResponse registroMovimentacao(MovimentacaoInput movimentacaoInput) {

		Produto produto = buscarProduto(movimentacaoInput.idProduto());

		MovimentacaoEstoque movimentacaoEstoque = movimentacaoEstoqueMapper.converter(movimentacaoInput,
				MovimentacaoEstoque::new);
		this.verificarMovimentacao(movimentacaoEstoque, produto, movimentacaoInput);
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

	private void entradaEstoque(MovimentacaoEstoque movimentacaoEstoque, MovimentacaoInput movimentacaoInput,
			Produto produto) {
		Estoque estoque = new Estoque();
		estoque = atualizarQtdeTotal(produto, estoque);
		BigDecimal qteAnterior = estoque.getQuantidade();
		estoque.setQuantidade(estoque.getQuantidade().add(movimentacaoInput.qtdeProduto()));
		movimentacaoInput.itens().forEach(itemIp -> {
			ProdutoVariacao variacao = buscarVariacao(produto, itemIp.variacoes().id());
			if(itemIp.qtde().signum()!=0) {
				ItemMovimentacao item = criarItemMovimentacao(movimentacaoEstoque, qteAnterior, variacao, itemIp.qtde(),
					    
						movimentacaoInput.tipoMovimentacao(), movimentacaoInput.qtdeProduto());
				movimentacaoEstoque.getItens().add(item);
			}
			
			atualizarQuantidadeVariacao(variacao, itemIp.qtde(), movimentacaoInput.tipoMovimentacao());
			

		});

		var variacao = ValidarUnidade(produto, movimentacaoInput.qtdeProduto().intValue());
		
		ItemMovimentacao item = new ItemMovimentacao();

		item.setQuantidade(movimentacaoInput.qtdeProduto());
		item.setSaldoanterior(qteAnterior);
		item.setMovimentacao(movimentacaoEstoque);
		item.setProdutoVariacao(variacao);

		movimentacaoEstoque.getItens().add(item);

		estoque.setDataAlteracao(LocalDateTime.now());
		produto.setEstoque(estoque);
		BigDecimal somaVariacoes = movimentacaoInput.itens().stream().map(varicao -> varicao.qtde())
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		if (somaVariacoes.compareTo(movimentacaoInput.qtdeProduto()) != 0) {
			throw new NegocioException(
					"A soma das quantidades das variações excede a quantidade total em estoque.");
		}

		produtoRepository.save(produto);
	}

	private void saidaEstoque(MovimentacaoEstoque movimentacaoEstoque, MovimentacaoInput movimentacaoInput,
			Produto produto) {
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
					varicao.setQtdeEstoque(varicao.getQtdeEstoque()
							+ varicao.calcularEstoque(produto.getEstoque().getQuantidade().intValue()));
				}

				var item = new ItemMovimentacao();
				item = criarItemMovimentacao(movimentacaoEstoque, saldoAterior, varicao, iemP.qtde(),
						movimentacaoEstoque.getTipoMovimentacao(),null);
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

		// Processa as variações que possuem qtdePorPacote igual a 1
		for (ProdutoVariacao variacao : variacoesDoProduto) {
			for (var item : movimentacaoInput.itens()) {
				// Verifica se o id da variação do item confere com a variação atual
				// e se a quantidade por pacote é 1
				if (item.variacoes().id().equals(variacao.getId())
						|| variacao.getQtdeporPacote().compareTo(BigDecimal.ONE) == 0) {

					// Cria um ItemMovimentacao para registrar a movimentação
					ItemMovimentacao itemMovimentacao = new ItemMovimentacao();
					itemMovimentacao.setQuantidade(item.qtde());

					// Atualiza o estoque: soma o saldo anterior com o estoque já registrado na
					// variação
					variacao.setQtdeEstoque(saldoAterior.intValue() + variacao.getQtdeEstoque().intValue());

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
	
	    item.setMovimentacao(movimentacaoEstoque);  // ✅ Sempre definir primeiro!
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
	            return null;  // ❌ Não cria um item vazio
	        }
	    }
		return item;
	}
}
