package com.easyerp.domain.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

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
import com.easyerp.model.input.ItemMovimentacoaInput;
import com.easyerp.model.input.MovimentacaoInput;
import com.easyerp.model.input.VariacaoMovimentacaoInput;

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
	    	 MovimentacaoEstoque movimentacaoEstoque = new MovimentacaoEstoque();
		        movimentacaoEstoque.setDataMovimentacao(LocalDateTime.now());
		        movimentacaoEstoque.setTipoMovimentacao(movimentacaoInput.tipoMovimentacao());
	    	Produto produto = buscarProduto(movimentacaoInput.idProduto());
	    	var valor= produto.getEstoque().getQuantidade().add(movimentacaoInput.qtdeProduto());
	        produto.getEstoque().setQuantidade(valor);  
	      

	    	for (var itemIp : movimentacaoInput.itens()) {
	    	    ProdutoVariacao variacao = produto.getVariacoes()
	    	        .stream()
	    	        .filter(v -> v.getId().equals(itemIp.variacoes().id()))
	    	        .findFirst()
	    	        .orElseThrow(() -> new NegocioException("Variação não encontrada para o item."));
	    	    
	    	    ItemMovimentacao item = new ItemMovimentacao();
	    	    item.setMovimentacao(movimentacaoEstoque);
	    	   
	    	    item.setQuantidade(itemIp.qtde());
	        	  var nvqute=    item.getQuantidade().intValue();
	    	    variacao.setQtdeEstoque(nvqute);
	    	    item.setProdutoVariacao(variacao);
	    	    movimentacaoEstoque.getItens().add(item);
	    	    verificarMovimentacao(movimentacaoEstoque, movimentacaoInput);
     
	    	    System.out.println("Nome do produto: " + item.getProdutoVariacao().getProduto().getProdutoNome());
	    	    System.out.println("Qtde estoque: " + item.getProdutoVariacao().getQtdeEstoque());
	    	    System.out.println("Qtde: " + item.getQuantidade());
	    	}
	    	produtoRepository.save(produto);
	    	   var movimetacaoSalva = movimentoEstoqueRepository.save(movimentacaoEstoque);
		        return movimentacaoEstoqueMapper.converter(movimetacaoSalva, MovimentacaoResponse::new);
	    }
	    
	    private void verificarMovimentacao(MovimentacaoEstoque movimentacaoEstoque, MovimentacaoInput movimentacaoInput) {
	        if (movimentacaoEstoque.getTipoMovimentacao().equals(TipoMovimentacao.Entrada)) {
	         movimentacaoEstoque.getItens().forEach(item -> processarEntradaEstoque(item, movimentacaoInput));
	        	
	        } else {
	            movimentacaoEstoque.getItens().forEach(item -> processarSaidaEstoque(item, movimentacaoInput));
	        }
	    }

	    private void processarSaidaEstoque(ItemMovimentacao item, MovimentacaoInput movimentacaoInput) {
	      //  validarQuantidadeTotal(item.getQuantidade(), movimentacaoInput.itens(), item.getProduto().getTipoProduto());
	        atualizarEstoque(item, movimentacaoInput, false);
	    }

	    private void processarEntradaEstoque(ItemMovimentacao item, MovimentacaoInput movimentacaoInput) {
	       validarQuantidadeTotal(movimentacaoInput.qtdeProduto(), movimentacaoInput, item.getProdutoVariacao().getProduto().getTipoProduto());
	 ///  atualizarEstoque(item, movimentacaoInput, true);
	       
	       
	    }
	    
	   
	    private void atualizarEstoque(ItemMovimentacao item, MovimentacaoInput movimentacaoInput, boolean isEntrada) {
	        Produto produto = item.getProdutoVariacao().getProduto();
       Estoque estoque = produto.getEstoque();
//
        if (estoque == null) {
	            estoque = new Estoque(); // Inicializa o estoque se for nulo
	            estoque.setQuantidade(BigDecimal.ZERO); // Define quantidade inicial como zero
	            estoque.setProduto(produto); // Associa o estoque ao produto
            produto.setEstoque(estoque);
	        }
//
//	        BigDecimal quantidadeMovimentacao = item.getQuantidade(); // Usa a quantidade específica do item
//
//	        if (isEntrada) {
//	            estoque.setQuantidade(estoque.getQuantidade().add(quantidadeMovimentacao));
//	        } else {
//	            if (estoque.getQuantidade().compareTo(quantidadeMovimentacao) < 0) {
//	                throw new NegocioException("Quantidade insuficiente no estoque para a movimentação.");
//	            }
//	            estoque.setQuantidade(estoque.getQuantidade().subtract(quantidadeMovimentacao));
//	        }
             
	        estoque.setDataAlteracao(LocalDateTime.now());
	        estoque.setProduto(produto);
	        produtoRepository.save(produto); // Salva o produto com o estoque atualizado
	    }
	    
	   private void atuliazacaoEstoque(MovimentacaoEstoque movimentacaoEstoque, MovimentacaoInput movimentacaoInput) {
		   Produto produto = movimentacaoEstoque.getItens().stream()
				   .map(i-> i.getProdutoVariacao().getProduto()).findFirst().orElse(null);
		   Estoque estoque =new Estoque();
	       
	        estoque = produto.getEstoque();
			   System.out.println("estouqe qtde"+ estoque.getQuantidade());
	        	
	        BigDecimal quantidadeMovimentacao = movimentacaoInput.qtdeProduto().
	        		add(produto.getEstoque().getQuantidade());
	        System.out.println("estouqe TOTAL "+ quantidadeMovimentacao);
	        estoque.setDataAlteracao(LocalDateTime.now());
	        estoque.setQuantidade(quantidadeMovimentacao);
	        estoque.setProduto(produto);
	        produtoRepository.save(produto);
	   }
		private void atualizarEstoqueVariacoes(Produto produto, Set<ItemMovimentacoaInput> itensMovimentacao, boolean isEntrada) {
	    	
	    }
	    private void validarQuantidadeTotal(BigDecimal quantidadeTotal,  MovimentacaoInput movimentacaoInput, TipoProduto tipoProduto) {
	    	System.out.println("pasou aqui");
	    	if (tipoProduto.equals(TipoProduto.Kit)) {
		            return;
		        }
	    	  BigDecimal somaVariacoes = movimentacaoInput.itens().stream()
	    			    .map(v -> v.qtde()) // Obtém o valor de cada variação
	    			    .reduce(BigDecimal.ZERO, BigDecimal::add); 
	    	  System.out.println(somaVariacoes + "soma variacoes");
	    	  if (somaVariacoes.compareTo(quantidadeTotal) != 0) {
	                throw new NegocioException("A soma das quantidades das variações deve ser igual a quantidade total da movimentação.");
	            }
	    }

	    private BigDecimal novoEstoque(Produto produto , Set<ItemMovimentacoaInput> itensMovimentacao, boolean isEntrada) {
	    	System.out.println(produto.getEstoque().getQuantidade());
	   	 BigDecimal total =produto.getEstoque().getQuantidade();
	   	 
	  
			for(var pVIten: produto.getVariacoes()){
			
				if(isEntrada) {
					total =total.add(new BigDecimal(pVIten.getQtdeEstoque()));
				}else {
					System.out.println(pVIten.getQtdeEstoque());
					System.out.println(pVIten.getQtdeporPacote());
					BigDecimal totalmultiplicado= pVIten.getQtdeporPacote().multiply(new BigDecimal(  pVIten.getQtdeEstoque()));
					System.out.println(totalmultiplicado+"multiplicado");
					
					//total =total.subtract(new BigDecimal(pVIten.getQtdeEstoque()));
					BigDecimal finaltotal= totalmultiplicado.multiply(new BigDecimal(  pVIten.getQtdeEstoque()));
					System.out.println(total.subtract( finaltotal)+"total final o final");
				}
			}
			 
			System.out.println(total+"total final");
			produto.getEstoque().setQuantidade(total);
			 return produto.getEstoque().getQuantidade();
	   }
	    private Produto buscarProduto(Long produtoId) {
	        return produtoRepository.findById(produtoId)
	                .orElseThrow(() -> new RegistroNaoEncontrado("Produto não encontrado para o ID: " + produtoId));
	    }
	}