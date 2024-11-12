package com.easyerp.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

public class GeradordeCodigo {
	private static final Set<String> ean13sGerados = new HashSet<>();
	private static final Random random = new Random();

	public static String CriarEAN13() {
		String ean13;
		do {
			ean13 = gerarEAN13();
		} while (!ean13sGerados.add(ean13)); // Adiciona ao conjunto, se já existe, gera novamente

		return ean13;
	}

	public static String gerarEAN13() {

		LocalDateTime datahora = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		String formattedDateTime = datahora.format(formatter);
		String numeroAleatorio = String.format("%03d", random.nextInt(100));
		String stringConcatenada = "789" + formattedDateTime.substring(5, 11) + numeroAleatorio;

		String ean13 = CalcularDigitoEan.calcularEAN13(stringConcatenada);
		CodigoBarraEAN codigoBarra = new CodigoBarraEAN(ean13);
		Optional<CodigoBarraEAN> codigoBarraValidado = codigoBarra.validar();

		// Verifique se a validação foi bem-sucedida antes de imprimir
		if (codigoBarraValidado.isPresent()) {
			System.out.println("Código de barra: OK");
			System.out.println("Número do código de barras: " + codigoBarra.getCodigoBarra());
			return codigoBarra.getCodigoBarra();
		} else {
			System.out.println("Código de barra: Inválido");
			// Trate o erro - talvez gere um novo código ou lance uma exceção
			return null; // Ou lance uma exceção, dependendo da sua lógica
		}




		//return codigoBarra.getCodigoBarra();
	}

	public static String GerarCodigoFabricante() {
		LocalDateTime datahora = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		String dataFormatada = datahora.format(formatter);
		String codigofabricante = dataFormatada.substring(8, 14);
		System.out.println(codigofabricante);
		return codigofabricante;
	}
}
