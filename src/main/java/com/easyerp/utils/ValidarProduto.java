package com.easyerp.utils;

import java.math.BigDecimal;

import com.easyerp.domain.entidade.Categoria;
import com.easyerp.domain.entidade.Marca;
import com.easyerp.domain.entidade.Produto;
import com.easyerp.domain.entidade.SubCategoria;
import com.easyerp.domain.enumerados.TipoProduto;
import com.easyerp.domain.service.exeption.NegocioException;
import com.easyerp.model.input.ProdutoEditarInput;
import com.easyerp.model.input.ProdutoVAlterar;



public class ValidarProduto {

    public void validar(Produto produtoExistente, ProdutoEditarInput produtoEditarInput){
        if(produtoEditarInput.produto()!=null){
            produtoExistente.setProdutoNome(produtoEditarInput.produto());
        }
       

        if (produtoEditarInput.marca() != null) {
            var marca = new Marca();
            marca.setId(produtoEditarInput.marca().id());
            produtoExistente.setMarca(marca);
        }
        if(produtoEditarInput.subcategoria()!=null){
            var subCategoria = new SubCategoria();
            subCategoria.setId(produtoEditarInput.subcategoria().id());
          produtoExistente.setSubCategoria(subCategoria);
        }
        if (produtoEditarInput.tipoProduto() != null) {
            produtoExistente.setTipoProduto(produtoEditarInput.tipoProduto());
        }
        if (produtoExistente == null) {
            throw new NegocioException("Estoque não encontrado para o produto.");
        }
        if (produtoExistente.getEstoque() != null && produtoExistente.getEstoque().getQuantidade().signum() > 0 
        		&& produtoExistente.getTipoProduto()!= TipoProduto.Kit) {
           validarQuantidadeTotal(produtoExistente.getEstoque().getQuantidade(), produtoEditarInput);
    
        	 produtoEditarInput.variacoes().forEach(variacao -> {
                 produtoExistente.getVariacoes().stream()
                         .filter(v -> v.getId().equals(variacao.id()))
                         .findFirst()
                         .ifPresent(v -> v.setQtdeEstoque(variacao.qtdeEstoque().intValue()));
             });


        }
    }
    private void validarQuantidadeTotal(BigDecimal quantidadeTotal, ProdutoEditarInput produtoEditarInput) {
    	  BigDecimal somaVariacoes =produtoEditarInput.variacoes().stream()
                  .map(ProdutoVAlterar::qtdeEstoque)
                  .reduce(BigDecimal.ZERO, BigDecimal::add);

          if (somaVariacoes.compareTo(quantidadeTotal) > 0) {
              throw new NegocioException("A soma das quantidades das variações excede a quantidade total em estoque.");
          }
    }
}


