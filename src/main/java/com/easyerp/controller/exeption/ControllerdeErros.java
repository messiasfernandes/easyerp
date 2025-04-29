package com.easyerp.controller.exeption;

import java.io.FileNotFoundException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.easyerp.domain.service.exeption.ArquivoInvalidoException;
import com.easyerp.domain.service.exeption.ArquivoSizeExeption;
import com.easyerp.domain.service.exeption.EntidadeEmUsoExeption;
import com.easyerp.domain.service.exeption.NegocioException;
import com.easyerp.domain.service.exeption.RegistroNaoEncontrado;
import com.easyerp.domain.service.exeption.StorageException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class ControllerdeErros  {
	@ExceptionHandler({ ConstraintViolationException.class })
	public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
		var status = HttpStatus.BAD_REQUEST;

		
		Map<String, String> errors = new HashMap<>();
		for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
			String fieldName = violation.getPropertyPath().toString(); 
			String errorMessage = violation.getMessage();              
			errors.put(fieldName, errorMessage);
		}

	
		var problema = Problema.builder()
				.status(status.value())
				.titulo("Erro de validação: um ou mais campos estão inválidos.")
				.dataHora(OffsetDateTime.now())
				.campos(errors.entrySet().stream()
						.map(entry -> new Problema.Campo(entry.getKey(), entry.getValue()))
						.collect(Collectors.toList()))
				.build();

		return new ResponseEntity<>(problema, status);
	}

//	@ExceptionHandler({ ConstraintViolationException.class })
//	public ResponseEntity<Object> cpfoCnpjviolation(ConstraintViolationException ex, WebRequest request) {
//		var status = HttpStatus.BAD_REQUEST;
//		var problema = Problema.builder().status(status.value()).titulo(ex.getMessage()).dataHora(OffsetDateTime.now())
//				.build();
//		;
//
//		return new ResponseEntity<>(problema, status);
//	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	protected ResponseEntity<Object> validarCampos(MethodArgumentNotValidException ex, WebRequest request) {
		List<Problema.Campo> campos = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> new Problema.Campo(error.getField(), error.getDefaultMessage()))
				.collect(Collectors.toList());

		Problema problema = Problema.builder()
				.titulo("Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente.")
				.status(HttpStatus.BAD_REQUEST.value()).dataHora(OffsetDateTime.now()).campos(campos).build();

		return ResponseEntity.badRequest().body(problema);
	}

	@ExceptionHandler(RegistroNaoEncontrado.class)
	public ResponseEntity<Object> EntidadeNaoEncontrada(RegistroNaoEncontrado ex, WebRequest request) {
		var status = HttpStatus.NOT_FOUND;

		var problema = Problema.builder().status(status.value()).titulo(ex.getMessage()).dataHora(OffsetDateTime.now())
				.build();

		return new ResponseEntity<>(problema, status);
	}

	@ExceptionHandler({ NegocioException.class })
	public ResponseEntity<Object> IlegalExeption(NegocioException ex, WebRequest request) {
		var status = HttpStatus.BAD_REQUEST;
		var problema = Problema.builder().status(status.value()).titulo(ex.getMessage()).dataHora(OffsetDateTime.now())
				.build();
		System.out.println(problema.getTitulo());
		return new ResponseEntity<>(problema, status);
	}

	@ExceptionHandler({ EntidadeEmUsoExeption.class })
	public ResponseEntity<Object> ViolacaoIntegriadade(EntidadeEmUsoExeption ex, WebRequest request) {
		var status = HttpStatus.CONFLICT;
		var problema = Problema.builder().status(status.value()).titulo(ex.getMessage()).dataHora(OffsetDateTime.now())
				.build();
		;

		return new ResponseEntity<>(problema, status);
	}
	@ExceptionHandler({NoResourceFoundException.class})
    public ResponseEntity<Object> handleNotFound(NoResourceFoundException ex) {
		var status = HttpStatus.NOT_FOUND;
		var problema = Problema.builder().status(status.value()).titulo("url não encontrada ou com erro! Confira ar url e tente novamente").dataHora(OffsetDateTime.now())
				.build();
		;
        return ResponseEntity.status(status).body(problema);
    }
	  @ExceptionHandler({ArquivoInvalidoException.class})
	    public ResponseEntity<Problema> handleIllegalArgument(ArquivoInvalidoException ex, WebRequest request) {
			var status = HttpStatus.UNSUPPORTED_MEDIA_TYPE;
	        Problema problema = Problema.builder()
	            .status(status.value())
	            .dataHora(OffsetDateTime.now())
	            .titulo(ex.getMessage())
	            .build();
	        System.out.println("Problema criado: " + problema);
	        return ResponseEntity.status(status).body(problema);
	    }

	    @ExceptionHandler({FileNotFoundException.class})
	    public ResponseEntity<Object> handleFileNotFound(FileNotFoundException ex) {
	        Problema problema = criarProblema(HttpStatus.NOT_FOUND, ex.getMessage());
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problema);
	    }

	    @ExceptionHandler({StorageException.class})
	    public ResponseEntity<Object> handleStorage(StorageException ex) {
	        Problema problema = criarProblema(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problema);
	    }
	  private Problema criarProblema(HttpStatus status, String mensagem) {
	        return Problema.builder()
	                .status(status.value())
	                .dataHora(OffsetDateTime.now())
	                .titulo(mensagem)
	                .build();
	    }

	   
   @ExceptionHandler(ArquivoSizeExeption.class)
    public ResponseEntity<Object> handleMaxSizeException(ArquivoSizeExeption ex,WebRequest request) {
	   var status = HttpStatus.PAYLOAD_TOO_LARGE;
       Problema problema = Problema.builder()
           .status(status.value())
           .dataHora(OffsetDateTime.now())
           .titulo(ex.getMessage())
           .build();
       return new ResponseEntity<>(problema, status);
}
}