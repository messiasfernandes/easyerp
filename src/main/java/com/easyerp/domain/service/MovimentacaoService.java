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
import lombok.experimental.var;

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
		MovimentacaoEstoque movimentacaoEstoque = new MovimentacaoEstoque();
		movimentacaoEstoque.setDataMovimentacao(LocalDateTime.now());
		movimentacaoEstoque.setTipoMovimentacao(movimentacaoInput.tipoMovimentacao());
		Produto produto = buscarProduto(movimentacaoInput.idProduto());

		movimentacaoInput.itens().forEach(itemIp -> {
			ProdutoVariacao variacao = buscarVariacao(produto, itemIp.variacoes().id());
			ItemMovimentacao item = criarItemMovimentacao(movimentacaoEstoque, variacao, itemIp.qtde(),
					movimentacaoInput.tipoMovimentacao());
			movimentacaoEstoque.getItens().add(item);
			atualizarQuantidadeVariacao(variacao, itemIp.qtde(), movimentacaoInput.tipoMovimentacao());
		});

		verificarMovimentacao(movimentacaoEstoque, movimentacaoInput);

		MovimentacaoEstoque movimetacaoSalva = movimentoEstoqueRepository.save(movimentacaoEstoque);
		return movimentacaoEstoqueMapper.converter(movimetacaoSalva, MovimentacaoResponse::new);
	}

	private void verificarMovimentacao(MovimentacaoEstoque movimentacaoEstoque, MovimentacaoInput movimentacaoInput) {
		if (movimentacaoEstoque.getTipoMovimentacao().equals(TipoMovimentacao.Entrada)) {
			movimentacaoEstoque.getItens()
					.forEach(item -> processarEntradaEstoque(item, item.getQuantidade()));

		} else {
			movimentacaoEstoque.getItens()
					.forEach(item -> processarSaidaEstoque(item, item.getQuantidade()));
		}
	}


	private void atualizarEstoque(ItemMovimentacao item, BigDecimal qtdeTotal, boolean isEntrada) {
	    Produto produto = item.getProdutoVariacao().getProduto();
	    Estoque estoque = produto.getEstoque();
	    item.setSaldoanterior(estoque.getQuantidade());

	    // Garantir que o estoque não seja nulo
	    if (estoque == null) {
	        estoque = inicializarEstoque(produto);
	    }

	    if (produto.getTipoProduto().equals(TipoProduto.Kit)) {
	        if (isEntrada) {
	            atualizarEstoqueEntradaKit(produto, qtdeTotal);
	        } else {
	            atualizarEstoqueSaidaKit(produto, qtdeTotal);
	        }
	    } else {
	        atualizarEstoqueProdutoSimples(estoque, qtdeTotal, isEntrada);
	    }
        estoque.setProduto(produto);
	    estoque.setDataAlteracao(LocalDateTime.now());
	    produtoRepository.save(produto);
	}


	private ProdutoVariacao buscarVariacao(Produto produto, Long idVariacao) {
		return produto.getVariacoes().stream().filter(v -> v.getId().equals(idVariacao)).findFirst()
				.orElseThrow(() -> new NegocioException("Variação não encontrada para o ID: " + idVariacao));
	}

	private ItemMovimentacao criarItemMovimentacao(MovimentacaoEstoque movimentacaoEstoque, ProdutoVariacao variacao,
			BigDecimal qtde, TipoMovimentacao tipoMovimentacao) {
		ItemMovimentacao item = new ItemMovimentacao();
		item.setMovimentacao(movimentacaoEstoque);
		item.setProdutoVariacao(variacao);
		item.setQuantidade((qtde));
		return item;
	}

	private void validarQuantidadeTotal(BigDecimal quantidadeTotal, MovimentacaoInput movimentacaoInput,
			TipoProduto tipoProduto) {
		System.out.println("pasou aqui");
		if (tipoProduto.equals(TipoProduto.Kit)) {

			System.out.println(movimentacaoInput.qtdeProduto());
			return;
		}
		BigDecimal somaVariacoes = movimentacaoInput.itens().stream().map(v -> v.qtde()) // Obtém o valor de cada
																							// variação
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		System.out.println(somaVariacoes + "soma variacoes");
		if (somaVariacoes.compareTo(quantidadeTotal) != 0) {
			throw new NegocioException(
					"A soma das quantidades das variações deve ser igual a quantidade total da movimentação.");
		}
	}

	private void atualizarQuantidadeVariacao(ProdutoVariacao variacao, BigDecimal qtde,
			TipoMovimentacao tipoMovimentacao) {
		if (tipoMovimentacao == TipoMovimentacao.Entrada) {
			variacao.setQtdeEstoque(variacao.getQtdeEstoque() + qtde.intValue());

		} else {
			if (variacao.getQtdeEstoque() < qtde.intValue()) {
				throw new NegocioException("Quantidade insuficiente no estoque da variação: " + variacao.getId());
			}

			variacao.setQtdeEstoque(variacao.getQtdeEstoque() - qtde.intValue());
		}
	}

	private void processarEntradaEstoque(ItemMovimentacao item, BigDecimal qtde) {
		atualizarEstoque(item, qtde, true);
	}

	private void processarSaidaEstoque(ItemMovimentacao item, BigDecimal qtde) {
		atualizarEstoque(item, qtde, false);
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
	    estoque.setProduto(produto);
	    produto.setEstoque(estoque);
	    return estoque;
	}
	
	private void atualizarEstoqueProdutoSimples(Estoque estoque, BigDecimal quantidade, boolean isEntrada) {
		System.out.println("quantidade"+ quantidade);
	
	    if (isEntrada) {
	        estoque.setQuantidade(estoque.getQuantidade().add(quantidade));
	    } else {
	        if (estoque.getQuantidade().compareTo(quantidade) < 0) {
	            throw new NegocioException("Quantidade insuficiente no estoque.");
	        }
	        estoque.setQuantidade(estoque.getQuantidade().subtract(quantidade));
	    }
	}
	
	private void atualizarEstoqueEntradaKit(Produto kit, BigDecimal quantidadeTotal) {
    	kit.getEstoque().setQuantidade(kit.getEstoque().getQuantidade().add(quantidadeTotal));
	    for (ProdutoVariacao variacao : kit.getVariacoes()) {
	        BigDecimal qtdePorVez = quantidadeTotal.multiply((variacao.getQtdeporPacote()));
	        
	        variacao.setQtdeEstoque(variacao.calcularEstoque(kit.getEstoque().getQuantidade().intValue()));
	    }
	}

	private void atualizarEstoqueSaidaKit(Produto kit, BigDecimal quantidadeTotal) {
	    System.out.println("Atualização Kit: " + quantidadeTotal);

	    // Loop pelas variações do kit
	    for (ProdutoVariacao variacao : kit.getVariacoes()) {
	        // Calcula a quantidade necessária baseada na variação
	        BigDecimal qtdeNecessaria = quantidadeTotal.multiply(variacao.getQtdeporPacote());
	        // Validação de estoque suficiente na variação
	        if (variacao.getProduto().getEstoque().getQuantidade().compareTo(qtdeNecessaria) < 0) {
	            throw new NegocioException("Quantidade insuficiente no estoque para a variação: " + variacao.getId());
	        }

	        // Atualiza o estoque da variação
	        BigDecimal estoqueAtualVariacao = variacao.getProduto().getEstoque().getQuantidade();
	        variacao.getProduto().getEstoque().setQuantidade(estoqueAtualVariacao.subtract(qtdeNecessaria));

	        // Atualiza o estoque total do kit
	        BigDecimal estoqueAtualKit = kit.getEstoque().getQuantidade();
	        kit.getEstoque().setQuantidade(estoqueAtualKit.subtract(qtdeNecessaria.divide(variacao.getQtdeporPacote())));

	        // Log para depuração
	        System.out.println("Variação ID " + variacao.getId() + 
	            " - Qtde Necessária: " + qtdeNecessaria + 
	            ", Estoque Atual: " + estoqueAtualVariacao + 
	            ", Estoque Atualizado: " + variacao.getProduto().getEstoque().getQuantidade());
	    }

	    // Log final para o estoque do kit
	    System.out.println("Estoque Total do Kit Atualizado: " + kit.getEstoque().getQuantidade());
	}



}