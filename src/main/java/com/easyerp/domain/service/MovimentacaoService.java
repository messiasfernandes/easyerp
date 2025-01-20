package com.easyerp.domain.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ValueRange;
import java.util.List;
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
import com.easyerp.model.input.ProdutoMovimentacaoInput;
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
	        validarQuantidadeTotal(item.getQuantidade(), movimentacaoInput.itens(), item.getProduto().getTipoProduto());
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
	            // Lógica para KIT (se necessário)
	        	produto.getVariacoes().forEach(variacao -> variacao.setQtdeEstoque(variacao.calcularEstoque(variacao.getQtdeEstoque())));
	        } else {
	        	atualizarEstoqueVariacoes(produto, movimentacaoInput.itens(),isEntrada);
	        }
	        if(isEntrada) {
	        	estoque.setQuantidade(estoque.getQuantidade().add(quantidadeMovimentacao));
	        }else {
	            if(quantidadeMovimentacao.signum() == 0) {
	                estoque.setQuantidade(novoEstoque(produto, isEntrada));
	            } else {
	                estoque.setQuantidade(estoque.getQuantidade().subtract(quantidadeMovimentacao));
	            }
	        }


	        estoque.setDataAlteracao(LocalDateTime.now());
	        produto.setEstoque(estoque);
	        produtoRepository.save(produto);
	    }
	    
	    private void atualizarEstoqueVariacoes(Produto produto, Set<ItemMovimentacoaInput> itens, boolean isEntrada) {
			// TODO Auto-generated method stub
			
		}

		private void atualizarEstoqueVariacoes(Produto produto, Set<ProdutoMovimentacaoInput> itensMovimentacao, boolean isEntrada) {
	    	 itensMovimentacao.forEach(inputItem -> {
	            inputItem.variacoes().forEach(inputVariacao -> {
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
	    private void validarQuantidadeTotal(BigDecimal quantidadeTotal,  Set<ProdutoMovimentacaoInput> itensMovimentacao, TipoProduto tipoProduto) {
	        if (tipoProduto.equals(TipoProduto.Kit)) {
	            return;
	        }
	    	 BigDecimal somaVariacoes = itensMovimentacao.stream()
	                 .flatMap(item -> item.variacoes().stream())
	                 .map(VariacaoMovimentacaoInput::qtde)
	                 .reduce(BigDecimal.ZERO, BigDecimal::add);


	            if (somaVariacoes.compareTo(quantidadeTotal) != 0) {
	                throw new NegocioException("A soma das quantidades das variações deve ser igual a quantidade total da movimentação.");
	            }
	    }

	    private BigDecimal novoEstoque(Produto produto,boolean isEntrada) {
	   	 BigDecimal total = BigDecimal.ZERO;
			for(var pVIten: produto.getVariacoes()){
				if(isEntrada) {
					total =total.add(new BigDecimal(pVIten.getQtdeEstoque()));
				}else {
					total =total.subtract(new BigDecimal(pVIten.getQtdeEstoque()));
				}
			
			 }
			 return total;
	   }
	    private Produto buscarProduto(Long produtoId) {
	        return produtoRepository.findById(produtoId)
	                .orElseThrow(() -> new RegistroNaoEncontrado("Produto não encontrado para o ID: " + produtoId));
	    }
	}