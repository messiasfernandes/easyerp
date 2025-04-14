package com.easyerp.domain.service.exeption;

import org.springframework.web.multipart.MaxUploadSizeExceededException;

public class ArquivoSizeExeption extends MaxUploadSizeExceededException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ArquivoSizeExeption(long maxUploadSize, Throwable ex) {
		super(maxUploadSize, ex);
	
	}





	
}
