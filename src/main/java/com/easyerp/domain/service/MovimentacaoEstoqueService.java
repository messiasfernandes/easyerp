package com.easyerp.domain.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

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
		Integer variacaoEstoque = 0;
		Estoque estoque = new Estoque();
		estoque = atualizarQtdeTotal(produto, estoque);
		BigDecimal qteAnterior = estoque.getQuantidade();
		if (movimentacaoInput.qtdeProduto().signum() != 0) {
			estoque.setQuantidade(estoque.getQuantidade().add(movimentacaoInput.qtdeProduto()));
		}
		movimentacaoEstoque = processarItem(movimentacaoInput, movimentacaoEstoque, produto, qteAnterior);
		estoque.setDataAlteracao(LocalDateTime.now());
        produto.setEstoque(estoque);
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
	
	private MovimentacaoEstoque processarItem(MovimentacaoInput movimentacaoInput , MovimentacaoEstoque movimentacaoEstoque, Produto produto, BigDecimal saldoAterior) {
	    Set<ProdutoVariacao> variacoesDoProduto = produto.getVariacoes();
	    for(var item : movimentacaoInput.itens()) {
	    	Long idVariacao=item.variacoes().id();
	    	Optional<ProdutoVariacao> variacaoEncontrada = variacoesDoProduto.
	    			stream().filter(v -> v.getId().equals(idVariacao)).findFirst();
	    	if(variacaoEncontrada.isPresent()) {
	    		 ProdutoVariacao variacao = variacaoEncontrada.get();
	                // Exemplo: criar um ItemMovimentacao (classe de domínio) para registrar a movimentação
	                ItemMovimentacao itemMovimentacao = new ItemMovimentacao();
	                itemMovimentacao.setQuantidade(item.qtde());
	                itemMovimentacao.setProdutoVariacao(variacao);
	                itemMovimentacao.setSaldoanterior(saldoAterior);
	                itemMovimentacao.setMovimentacao(movimentacaoEstoque);
	               
	             //   if(variacao.getProduto().getTipoProduto().equals(TipoProduto.Kit)) {
	                System.out.println(variacao.getQtdeporPacote()+"qtde pacate");
	             
	                		System.out.println(item.qtde());
	                		System.out.println("pasou aqui");
	                		   var novaQteVaraicao=	variacao.calcularEstoqueKit(item.qtde().intValue());
	                		   System.out.println(novaQteVaraicao);
	                		  // variacao.setQtdeEstoque(   novaQteVaraicao);
	         
	               
	              
	             
	               
	                //	var novaPacote = variacao.calcualarEstoqueKit(item.qtde().intValue());
	                //	var novaQtde= variacao.getQtdeEstoque()+ novaPacote;
	                //	System.out.println("nova qtde "+ novaQtde);
	                //	System.out.println("varicao qtde "+ variacao.getQtdeEstoque());
	           
	              	System.out.println("Variacao atualizada "+ variacao.getQtdeEstoque());
	              	 movimentacaoEstoque.getItens().add(itemMovimentacao);
	           //    }
	    	}
	    }
		return movimentacaoEstoque;
	}

	private ItemMovimentacao criarItemMovimentacao(MovimentacaoEstoque movimentacaoEstoque, BigDecimal saldoAnterior,
			ProdutoVariacao variacao, BigDecimal qtde, TipoMovimentacao tipoMovimentacao) {
		System.out.println();
		ItemMovimentacao item = new ItemMovimentacao();
		if (qtde.signum() != 0) {

			item.setMovimentacao(movimentacaoEstoque);
			item.setProdutoVariacao(variacao);

			item.setQuantidade(qtde);
			System.out.println("saldo anterior " + saldoAnterior);
			item.setSaldoanterior(saldoAnterior);
		}

		return item;
	}
}
