package com.easyerp.domain.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easyerp.config.ModelMapper;
import com.easyerp.domain.entidade.Estoque;
import com.easyerp.domain.entidade.ItemMovimentacao;
import com.easyerp.domain.entidade.MovimentacaoEstoque;
import com.easyerp.domain.entidade.MovimentacaoEstoque.TipoMovimentacao;
import com.easyerp.domain.entidade.Produto;
import com.easyerp.domain.enumerados.TipoProduto;
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
    private MovimentacaoInput movimento;
	@Transactional
	public MovimentacaoResponse registrarMovimentacao(MovimentacaoInput movimentacaoInput) {
	
		MovimentacaoEstoque movimentacaoEstoque = new MovimentacaoEstoque();
		movimentacaoEstoque.setDataMovimentacao(LocalDateTime.now());
      
		movimentacaoEstoque.setTipoMovimentacao(movimentacaoInput.tipoMovimentacao());
         this.movimento = movimentacaoInput;
		Produto produto = new Produto();
		
		produto = buscarProduto(movimentacaoInput.idProduto());
         
		ItemMovimentacao item = new ItemMovimentacao();
		item.setProduto(produto);
		movimentacaoEstoque.getItens().add(item);
		movimentacaoEstoque.getItens().forEach(m -> m.setMovimentacao(movimentacaoEstoque));

		for (var ItemM : movimentacaoEstoque.getItens()) {
			for (var itemIp : movimentacaoInput.itens()) {
				ItemM.setQuantidade(itemIp.qtde());

			}
			verificarMovimentacao(movimentacaoEstoque,  movimentacaoInput);
		}
		var movimetacaoSalva = movimentoEstoqueRepository.save(movimentacaoEstoque);
		return movimentacaoEstoqueMapper.converter(movimetacaoSalva, MovimentacaoResponse::new);
	}

	private void processarSaidaEstoque(ItemMovimentacao item) {
	 
		Estoque estoque = item.getProduto().getEstoque();
		if (estoque == null) {

			estoque = new Estoque();
			estoque.setDataAlteracao(LocalDateTime.now());
			estoque.setDataCadastro(LocalDateTime.now());

			estoque.setProduto(item.getProduto());

			estoque.setQuantidade(BigDecimal.ZERO);

		}
     
		/// item.setSaldoAnterior(estoque.getQuantidade());
		if(item.getQuantidade().signum()==0) {
			  System.out.println("estoque saida");
			 movimento.itens().forEach(inputItem -> {
		            inputItem.produtoMovimetacao().variacoes().forEach(inputVariacao -> {
		                item.getProduto().getVariacoes().forEach(variacao -> {
		                    if (variacao.getId().equals(inputVariacao.id())) {
		                     Integer     novaQuantidade = variacao.getQtdeEstoque() - inputVariacao.qtde().intValue();
		                     System.out.println("nova quantidade"+ novaQuantidade);
		                        variacao.setQtdeEstoque(novaQuantidade);
		              
		                    }
		                });
		            });
		            
		        });
			
			estoque.setQuantidade(novoEstoque(item));
		}else{
			System.out.println(item.getQuantidade());
			estoque.setQuantidade(estoque.getQuantidade().add(item.getQuantidade()));
		}
		estoque.setDataAlteracao(LocalDateTime.now());
		
		item.getProduto().setEstoque(estoque);
		if (item.getProduto().getTipoProduto().equals(TipoProduto.Kit)) {
			for (var produtoVariacao : item.getProduto().getVariacoes()) {
				produtoVariacao.setQtdeEstoque(produtoVariacao.calcularEstoque(produtoVariacao.getQtdeEstoque()));
			}
		}else {
			System.out.println("pasou aqui tabem ");
//			//validarQuantidadeTotal(item.getProduto().getEstoque().getQuantidade(), movimento);
			
			 movimento.itens().forEach(inputItem -> {
		            inputItem.produtoMovimetacao().variacoes().forEach(inputVariacao -> {
		                item.getProduto().getVariacoes().forEach(variacao -> {
		                    if (variacao.getId().equals(inputVariacao.id())) {
		                        Integer novaQuantidade = variacao.getQtdeEstoque() - inputVariacao.qtde().intValue();
		                        variacao.setQtdeEstoque(novaQuantidade);
		                    }
		                });
		            });
		        });
   }
		
		produtoRepository.save(item.getProduto());
	}

	private void processarEntradaEstoque(ItemMovimentacao item) {
		Estoque estoque = item.getProduto().getEstoque();
		if (estoque == null) {

			estoque = new Estoque();
			estoque.setDataAlteracao(LocalDateTime.now());
			estoque.setDataCadastro(LocalDateTime.now());

			estoque.setProduto(item.getProduto());

			estoque.setQuantidade(BigDecimal.ZERO);

		}

		estoque.setDataAlteracao(LocalDateTime.now());
		estoque.setQuantidade(estoque.getQuantidade().add(item.getQuantidade()));
		item.getProduto().setEstoque(estoque);
		if (item.getProduto().getTipoProduto().equals(TipoProduto.Kit)) {
			for (var produtoVariacao : item.getProduto().getVariacoes()) {
				produtoVariacao.setQtdeEstoque(produtoVariacao.calcularEstoque(produtoVariacao.getQtdeEstoque()));
			}
		}else {
			 System.out.println("passou aqui ");
			validarQuantidadeTotal(item.getProduto().getEstoque().getQuantidade(), movimento);
			
			 movimento.itens().forEach(inputItem -> {
		            inputItem.produtoMovimetacao().variacoes().forEach(inputVariacao -> {
		                item.getProduto().getVariacoes().forEach(variacao -> {
		                    if (variacao.getId().equals(inputVariacao.id())) {
		                        Integer novaQuantidade = variacao.getQtdeEstoque() + inputVariacao.qtde().intValue();
		                        variacao.setQtdeEstoque(novaQuantidade);
		                    }
		                });
		            });
		        });
		    }
		
	
		produtoRepository.save(item.getProduto());
	}

	private void verificarMovimentacao(MovimentacaoEstoque movimentacaoEstoque, MovimentacaoInput movimentacaoInput) {
		if (movimentacaoEstoque.getTipoMovimentacao().equals(TipoMovimentacao.Entrada)) {
			movimentacaoEstoque.getItens().forEach(this::processarEntradaEstoque);
		} else {
			movimentacaoEstoque.getItens().forEach(this::processarSaidaEstoque);
		}

	}
	 private void validarQuantidadeTotal(BigDecimal quantidadeTotal, MovimentacaoInput  movimentacao) {
		    BigDecimal somaVariacoes = movimentacao.itens().stream()
		            .flatMap(item -> item.produtoMovimetacao().variacoes().stream())
		            .map(variacao -> variacao.qtde() != null ? variacao.qtde() : BigDecimal.ZERO) // Qtde já é BigDecimal, então use diretamente
		            .reduce(BigDecimal.ZERO, BigDecimal::add);

		        if (somaVariacoes.compareTo(quantidadeTotal) > 0) {
		            throw new NegocioException("A soma das quantidades das variações excede a quantidade total em estoque.");
		        }
		        
		        System.out.println("Quantidade total estoque: " + quantidadeTotal);
		        System.out.println("Soma variações no movimento: " + somaVariacoes);
	 }
	 private BigDecimal novoEstoque(ItemMovimentacao item) {
		 BigDecimal total = BigDecimal.ZERO;
		for(var pVIten: item.getProduto().getVariacoes()){
			total =total.add(new BigDecimal(pVIten.getQtdeEstoque()));
			 System.out.println( "Estoque"+pVIten.getQtdeEstoque());
			 System.out.println( "total"+total);
		 }
		 return total;
	 }
	 
	 private Produto buscarProduto(Long produtoId) {
	        return produtoRepository.findById(produtoId)
	                .orElseThrow(() -> new RegistroNaoEncontrado("Produto não encontrado para o ID: " + produtoId));
	    }
	 
	
}
