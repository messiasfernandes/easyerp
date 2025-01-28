package com.easyerp;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EasyErpApplicationTests {

	@Test
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
	

}
