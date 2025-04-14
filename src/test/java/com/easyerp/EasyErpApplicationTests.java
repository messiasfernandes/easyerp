package com.easyerp;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.easyerp.domain.repository.ProdutoVariacaoRepository;

@SpringBootTest
class EasyErpApplicationTests {
@Autowired
	ProdutoVariacaoRepository produtoVariacaoRepository;
	void contextLoads() {
		String[] cores = {"azul", "preto", "vermelho"};
        String[] tamanhos = {"pequeno", "medio", "gigante", "extra grande"};

        List<String> combinacoes = new ArrayList<>();

        for (String cor : cores) {
            for (String tamanho : tamanhos) {
                combinacoes.add(cor + " " + tamanho);
            }
        }

        // Imprime as combinações
        for (String combinacao : combinacoes) {
            System.out.println(combinacao);
        }
    }
	
  void somar() {
		int saldoatual=30;
		int qtde=90;
		int multiplicador=15;
		int estoque=0;
		estoque= (saldoatual+qtde)/multiplicador;
		System.out.println("estoque : "+estoque);
	  
  }
  @Test
  void buscar(){


	  
  }
}
