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
	        
	        
	        Set<ItemMovimentacao> itensMovimentacao = movimentacaoInput.itens().stream()
	                .map(inputItem -> {
	                    ItemMovimentacao item = new ItemMovimentacao();
	                    item.setProduto(produto);
	                    item.setQuantidade(inputItem.qtde());
	                    item.setMovimentacao(movimentacaoEstoque);
	                    return item;
	                }).collect(Collectors.toSet());

	        movimentacaoEstoque.getItens().addAll(itensMovimentacao);
	        movimentacaoEstoque.getItens().forEach(m->m.setMovimentacao(movimentacaoEstoque));
	        verificarMovimentacao(movimentacaoEstoque, movimentacaoInput);

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
	        validarQuantidadeTotal(item.getQuantidade(), movimentacaoInput.itens(), item.getProduto().getTipoProduto());
	        atualizarEstoque(item, movimentacaoInput, true);
	    }
	    
	   
	    private void atualizarEstoque(ItemMovimentacao item, MovimentacaoInput movimentacaoInput, boolean isEntrada) {
	        Produto produto = item.getProduto();
	        Estoque estoque = produto.getEstoque();
	        
	        if (estoque == null) {
	            estoque = new Estoque();
	            estoque.setDataCadastro(LocalDateTime.now());
	            estoque.setProduto(produto);
	            estoque.setQuantidade(BigDecimal.ZERO);
	        }

	        BigDecimal quantidadeMovimentacao = item.getQuantidade();

	        if (produto.getTipoProduto().equals(TipoProduto.Kit)) {
	        	
	        //	atualizarEstoqueVariacoes(produto, movimentacaoInput.itens(), isEntrada);
	            // Lógica para KIT (se necessário)
	      	produto.getVariacoes().forEach(variacao -> variacao.setQtdeEstoque(variacao.calcularEstoque(variacao.getQtdeEstoque())));
	        } else {
	        	System.out.println("veio aqui ");
	        	atualizarEstoqueVariacoes(produto, movimentacaoInput.itens(), isEntrada);
	        
	        }
	        if(isEntrada) {
	        	estoque.setQuantidade(estoque.getQuantidade().add(quantidadeMovimentacao));
	        }else {
	            if(quantidadeMovimentacao.signum() == 0) {
	            	System.out.println("PASOU AQUI ");
	            ///	atualizarEstoqueVariacoes(produto, movimentacaoInput.itens(), isEntrada);
	                estoque.setQuantidade(novoEstoque(produto,movimentacaoInput.itens(), isEntrada));
	            } else {
	                estoque.setQuantidade(estoque.getQuantidade().subtract(quantidadeMovimentacao));
	            }
	        }


	        estoque.setDataAlteracao(LocalDateTime.now());
	        produto.setEstoque(estoque);
	        System.out.println("estoque");
	        produtoRepository.save(produto);
	    }
	    
	  
		private void atualizarEstoqueVariacoes(Produto produto, Set<ItemMovimentacoaInput> itensMovimentacao, boolean isEntrada) {
	    	 itensMovimentacao.forEach(inputItem -> {
	            inputItem.produtoMovimetacao().variacoes().forEach(inputVariacao -> {
	            	produto.getVariacoes().forEach(variacao -> {
	                    if (variacao.getId().equals(inputVariacao.id())) {
	                        Integer novaQuantidade = variacao.getQtdeEstoque();
	                        if (isEntrada) {
	                           novaQuantidade += inputVariacao.qtde().intValue();
	                        }else{
	                            novaQuantidade -= inputVariacao.qtde().intValue();
	                        }
	                         variacao.setQtdeEstoque(novaQuantidade);
	                     }
	                 });
	             });
	         });
	    }
	    private void validarQuantidadeTotal(BigDecimal quantidadeTotal,  Set<ItemMovimentacoaInput> itensMovimentacao, TipoProduto tipoProduto) {
	        if (tipoProduto.equals(TipoProduto.Kit)) {
	            return;
	        }
	        BigDecimal somaVariacoes = itensMovimentacao.stream()
	                .flatMap(item -> item.produtoMovimetacao().variacoes().stream())
	                .map(VariacaoMovimentacaoInput::qtde)
	                .reduce(BigDecimal.ZERO, BigDecimal::add);

	            // Verificando se a soma das variações é igual à quantidade total
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