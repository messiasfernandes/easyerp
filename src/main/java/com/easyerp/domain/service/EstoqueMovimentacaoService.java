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
		BigDecimal totalMovimentado = calcularTotalMovimentado(produto, movimentacaoInput);
		estoque.setQuantidade(estoque.getQuantidade().add(totalMovimentado));
		produto.getVariacoes().forEach(var -> {
		    movimentacaoInput.itens().forEach(itemImp -> {
		    	
		        ProdutoVariacao variacaoEncontrada = buscarVariacao(produto, itemImp.variacoes().id());
		        if (itemImp.variacoes().id().equals(variacaoEncontrada.getId())){
		        	variacaoEncontrada.setQtdeEstoque(variacaoEncontrada.getQtdeEstoque() + itemImp.qtde().intValue());
		        }
		        
		        System.out.println(variacaoEncontrada.getQtdeEstoque() +"variaao atualizado" );
		    ItemMovimentacao    item  = criarItemMovimentacao(movimentacaoEstoque,
             qteAnterior, variacaoEncontrada, itemImp.qtde(), movimentacaoInput.tipoMovimentacao(), totalMovimentado);
		    
		 
		    movimentacaoEstoque.getItens().add(item);
		    
		    if(variacaoEncontrada.getQtdeporPacote().compareTo(BigDecimal.ONE)==0) {
		    	System.out.println("pasou aqui ");
		        var itemM = new ItemMovimentacao();
		        variacaoEncontrada.setQtdeEstoque(variacaoEncontrada.getQtdeEstoque() + totalMovimentado.intValue());
		        itemM.setProdutoVariacao(variacaoEncontrada);
		        itemM.setMovimentacao(movimentacaoEstoque);
		        itemM.setSaldoanterior(qteAnterior);
		        itemM.setQuantidade ( totalMovimentado);
		        movimentacaoEstoque.getItens().add(itemM);
		    }
		    });
		    
		    
		});
      
     
	  
	   
	}
	private BigDecimal calcularTotalMovimentado(Produto produto,  MovimentacaoInput movimentacaoInput) {
		BigDecimal total= movimentacaoInput.itens().stream()
			    .map(itemImp -> {
			        // Buscar a variação correta dentro do Set<ProdutoVariacao>
			        ProdutoVariacao variacao = produto.getVariacoes().stream()
			            .filter(v -> v.getId().equals(itemImp.variacoes().id()))
			            .findFirst()
			            .orElseThrow(() -> new RuntimeException("Variação não encontrada para o ID: " + itemImp.variacoes().id()));

			        BigDecimal qtde = itemImp.qtde() != null ? itemImp.qtde() : BigDecimal.ZERO;
			        BigDecimal qtdePorPacote = variacao.getQtdeporPacote() != null ? variacao.getQtdeporPacote() : BigDecimal.ONE;

			        return qtde.multiply(qtdePorPacote); // Multiplica quantidade pelo tamanho do pacote
			    })
			    .reduce(BigDecimal.ZERO, BigDecimal::add);
		return total;
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
		item.setSaldoanterior(saldoAnterior);
		item.setProdutoVariacao(variacao);
		item.setQuantidade(qtde);
        item.setMovimentacao(movimentacaoEstoque);
		
		return item;
	}
}
