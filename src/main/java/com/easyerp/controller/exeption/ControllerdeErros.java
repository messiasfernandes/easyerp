package com.easyerp.controller.exeption;

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
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.easyerp.domain.service.exeption.EntidadeEmUsoExeption;
import com.easyerp.domain.service.exeption.NegocioException;
import com.easyerp.domain.service.exeption.RegistroNaoEncontrado;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class ControllerdeErros {
	@ExceptionHandler({ ConstraintViolationException.class })
	public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
		var status = HttpStatus.BAD_REQUEST;

		// Criar um mapa para capturar os erros detalhados
		Map<String, String> errors = new HashMap<>();
		for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
			String fieldName = violation.getPropertyPath().toString(); // Nome do campo
			String errorMessage = violation.getMessage();              // Mensagem de erro
			errors.put(fieldName, errorMessage);
		}

		// Criar o objeto 'Problema' com detalhes dos campos violados
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
	@ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Object> handleNotFound(NoResourceFoundException ex) {
		var status = HttpStatus.NOT_FOUND;
		var problema = Problema.builder().status(status.value()).titulo("url não encontrada ou com erro! Confira ar url e tente novamente").dataHora(OffsetDateTime.now())
				.build();
		;
        return ResponseEntity.status(status).body(problema);
    }


//	@ExceptionHandler(HttpMessageNotReadableException.class)
//    public ResponseEntity<Object> jsonErro (HttpMessageNotReadableException ex) {
//		var status = HttpStatus.BAD_REQUEST;
//		var problema = Problema.builder().status(status.value()).titulo("Houve um erro no preenchimento dos dados. Por favor, verifique se os dados estão corretos e tente novamente."+ ex.getMessage()).dataHora(OffsetDateTime.now())
//				.build();
//		;
//        return ResponseEntity.status(status).body(problema);
//    }
}
