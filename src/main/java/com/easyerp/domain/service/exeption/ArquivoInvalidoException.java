package com.easyerp.domain.service.exeption;

public class ArquivoInvalidoException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ArquivoInvalidoException(String mensagem) {
        super(mensagem);
    }

}
