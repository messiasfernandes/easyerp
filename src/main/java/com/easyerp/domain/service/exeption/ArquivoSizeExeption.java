package com.easyerp.domain.service.exeption;

public class ArquivoSizeExeption extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ArquivoSizeExeption(String mensagem) {
        super(mensagem);
    }
	
	
}
