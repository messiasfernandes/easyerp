package com.easyerp.domain.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Iterator;

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
public class EstoqueMovimentacaoService {

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
			processaItem();
		}

	}

	private ProdutoVariacao buscarVariacao(Produto produto, Long idVariacao) {
		return produto.getVariacoes().stream().filter(v -> v.getId().equals(idVariacao)).findFirst()
				.orElseThrow(() -> new NegocioException("Variação não encontrada para o ID: " + idVariacao));
	}



	private void processaItem() {
		// TODO Auto-generated method stub

	}

	private void processarItemKit(MovimentacaoEstoque movimentacaoEstoque, MovimentacaoInput movimentacaoInput,
			Produto produto) {
		Estoque estoque = new Estoque();
		estoque = atualizarQtdeTotal(produto, estoque);
		BigDecimal qteAnterior = estoque.getQuantidade();
		estoque.setQuantidade(estoque.getQuantidade().add(movimentacaoInput.qtdeProduto()));
		movimentacaoInput.itens().forEach(itemIp -> {
			ProdutoVariacao variacao=	buscarVariacao(produto, itemIp.variacoes().id());
			atualizarQuantidadeVariacao(variacao, itemIp.qtde(), movimentacaoEstoque.getTipoMovimentacao());
		    ItemMovimentacao item =criarItemMovimentacao(movimentacaoEstoque, qteAnterior, variacao, qteAnterior, 
		    		movimentacaoInput.tipoMovimentacao(), movimentacaoInput.qtdeProduto());
		    System.out.println("Produto: " + variacao.getProduto().getProdutoNome() +
	                   ", EAN: " + variacao.getCodigoEan13() +
	                   ", qtdeMovimentada: " + movimentacaoInput.qtdeProduto() +
	                   ", qtdeporVariacao: " + variacao.getQtdeEstoque());
		    movimentacaoEstoque.getItens().add(item);
		});
		var item = new ItemMovimentacao();
		item.setMovimentacao(movimentacaoEstoque);
		item.setQuantidade(movimentacaoInput.qtdeProduto());
		item.setSaldoanterior(qteAnterior);
		item.setProdutoVariacao(atualizarQuantidadeUnidadeVariacao(produto, 
				movimentacaoInput.qtdeProduto(), movimentacaoEstoque.getTipoMovimentacao()));
		
		System.out.println("Produto unitario: " +item.getProdutoVariacao() .getProduto().getProdutoNome() +
                ", EAN: " + item.getProdutoVariacao().getCodigoEan13() +
                ", qtdeMovimentada: " + movimentacaoInput.qtdeProduto() +
                ", qtdeporVariacao: " + item.getProdutoVariacao().getQtdeEstoque());
		movimentacaoEstoque.getItens().add(item);
	
	}

	private ProdutoVariacao atualizarQuantidadeUnidadeVariacao(Produto produto, BigDecimal qtde, TipoMovimentacao tipoMovimentacao) {
		 System.out.println("variacao unitaria "+ qtde);
		
		for (var variacao : produto.getVariacoes()) {
            	   
		        if(  variacao.getQtdeporPacote().compareTo(BigDecimal.ONE)==0) {
		        	if(variacao.getQtdeEstoque()==0) {
		        		variacao.setQtdeEstoque(variacao.getQtdeEstoque().intValue()+qtde.intValue());
		        	}
		        	variacao.setQtdeEstoque(variacao.getQtdeEstoque().intValue()+qtde.intValue());
		        }
	        else {
	        	variacao.setQtdeEstoque(variacao.getQtdeEstoque().intValue()+variacao.getProduto().getEstoque().getQuantidade().intValue() +qtde.intValue());
		        }
		        System.out.println("variacao unitaria "+ variacao.getQtdeEstoque());
		        return variacao;
			}
         return null;
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
	
	private void atualizarQuantidadeVariacao(ProdutoVariacao variacao, BigDecimal qtde,
			TipoMovimentacao tipoMovimentacao) {

		if (tipoMovimentacao == TipoMovimentacao.Entrada) {

			variacao.setQtdeEstoque(qtde.intValue());
			
			System.out.println(variacao.getQtdeEstoque() +"variacao atualizada");

		} else {
			if (variacao.getQtdeEstoque() < qtde.intValue()) {
				throw new NegocioException("Quantidade insuficiente no estoque da variação: " + variacao.getId());
			}

			variacao.setQtdeEstoque((variacao.getQtdeEstoque() - qtde.intValue()));
		}

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
		} else if (qtde.compareTo(BigDecimal.ZERO)==0 ) {
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
