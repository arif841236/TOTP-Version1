package com.indusnet.exception;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.indusnet.model.common.LocalDateTimeConverter;
import com.indusnet.model.common.LoggingResponseMessage;
import com.indusnet.model.common.MessageTypeConst;
import lombok.extern.slf4j.Slf4j;

// This class for handle all exception
@ControllerAdvice
@Slf4j
public class ExceptionHandlerGlobal extends ResponseEntityExceptionHandler {

	GsonBuilder builder = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeConverter()); 
	Gson gson = builder.create();

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(OtpException.class)
	public ResponseEntity<ErrorResponce> userExceptionHandler(OtpException ue, WebRequest wb, Exception e) {
		ErrorResponce error = ErrorResponce.builder()
				.errorCode(1)
				.status(HttpStatus.BAD_REQUEST.value())
				.errorMessage(HttpStatus.BAD_REQUEST.name())
				.path(wb.getDescription(false).substring(4))
				.timestamp(Timestamp.valueOf(LocalDateTime.now()))
				.traceID(Instant.now().toEpochMilli())
				.errorDetails(ue.getMessage())
				.build();

		LoggingResponseMessage loggingResponseMessage2 = LoggingResponseMessage.builder()
				.data(error)
				.message("Some internal errror.")
				.messageTypeId(MessageTypeConst.INTERNAL_ERROR)
				.statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
				.build();

		log.error(gson.toJson(loggingResponseMessage2));

		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	// @param nValid :its argument of validate.
	// @return : its return response entity of Map.
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		List<FieldError> error = ex.getBindingResult().getFieldErrors();
		OtpErrorResponce otpVException = new OtpErrorResponce(HttpStatus.UNPROCESSABLE_ENTITY.value(),
				error.get(0).getDefaultMessage());

		LoggingResponseMessage loggingResponseMessage2 = LoggingResponseMessage.builder()
				.data(error)
				.message(error.get(0).getDefaultMessage())
				.messageTypeId(MessageTypeConst.INTERNAL_ERROR)
				.statusCode(HttpStatus.UNPROCESSABLE_ENTITY)
				.build();

		log.error(gson.toJson(loggingResponseMessage2));

		return new ResponseEntity<>(otpVException, HttpStatus.UNPROCESSABLE_ENTITY);
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<OtpErrorResponce> dateReponseGenerate(ValidationException excp) {

		OtpErrorResponce resp = new OtpErrorResponce(HttpStatus.UNPROCESSABLE_ENTITY.value(), excp.getMessage());

		LoggingResponseMessage loggingResponseMessage2 = LoggingResponseMessage.builder()
				.data(resp)
				.message(excp.getMessage())
				.messageTypeId(MessageTypeConst.INTERNAL_ERROR)
				.statusCode(HttpStatus.UNPROCESSABLE_ENTITY)
				.build();

		log.error(gson.toJson(loggingResponseMessage2));

		return new ResponseEntity<>(resp, HttpStatus.UNPROCESSABLE_ENTITY);
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(SQLException.class)
	public final ResponseEntity<Object> sqlExceptions(SQLException ex, WebRequest request) {
		ErrorResponce error = ErrorResponce.builder()
				.errorCode(1)
				.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.errorMessage(HttpStatus.INTERNAL_SERVER_ERROR.name())
				.path(request.getDescription(false).substring(4))
				.timestamp(Timestamp.valueOf(LocalDateTime.now()))
				.traceID(Instant.now().toEpochMilli())
				.errorDetails(ex.getMessage())
				.build();

		LoggingResponseMessage msgStart = LoggingResponseMessage.builder()
				.message(ex.getMessage())
				.messageTypeId(MessageTypeConst.ERROR)
				.data(error)
				.build();

		log.error(gson.toJson(msgStart));

		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Override
	protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {

		OtpErrorResponce resp = new OtpErrorResponce(HttpStatus.UNPROCESSABLE_ENTITY.value(), ex.getMessage());

		LoggingResponseMessage loggingResponseMessage2 = LoggingResponseMessage.builder()
				.data(resp)
				.message(ex.getMessage())
				.messageTypeId(MessageTypeConst.INTERNAL_ERROR)
				.statusCode(HttpStatus.UNPROCESSABLE_ENTITY)
				.build();

		log.error(gson.toJson(loggingResponseMessage2));

		return new ResponseEntity<>(resp, HttpStatus.UNPROCESSABLE_ENTITY);
	}

	@Override
	protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		ErrorResponce error = ErrorResponce.builder()
				.errorCode(1)
				.status(HttpStatus.NOT_FOUND.value())
				.errorMessage(HttpStatus.NOT_FOUND.name())
				.path(request.getDescription(false).substring(4))
				.timestamp(Timestamp.valueOf(LocalDateTime.now()))
				.traceID(Instant.now().toEpochMilli())
				.errorDetails(ex.getMessage())
				.build();

		LoggingResponseMessage loggingResponseMessage2 = LoggingResponseMessage.builder()
				.data(error)
				.message(ex.getMessage())
				.messageTypeId(MessageTypeConst.ERROR)
				.statusCode(HttpStatus.NOT_FOUND)
				.build();

		log.error(gson.toJson(loggingResponseMessage2));

		return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
	}

	@Override
	protected ResponseEntity<Object> handleMissingPathVariable(MissingPathVariableException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {

		ErrorResponce error = ErrorResponce.builder()
				.errorCode(1)
				.status(HttpStatus.METHOD_NOT_ALLOWED.value())
				.errorMessage(HttpStatus.METHOD_NOT_ALLOWED.name())
				.path(request.getDescription(false).substring(4))
				.timestamp(Timestamp.valueOf(LocalDateTime.now()))
				.traceID(Instant.now().toEpochMilli())
				.errorDetails(ex.getMessage())
				.build();

		LoggingResponseMessage loggingResponseMessage2 = LoggingResponseMessage.builder()
				.data(error)
				.message(ex.getMessage())
				.messageTypeId(MessageTypeConst.ERROR)
				.statusCode(HttpStatus.METHOD_NOT_ALLOWED)
				.build();

		log.error(gson.toJson(loggingResponseMessage2));

		return new ResponseEntity<>(error, HttpStatus.METHOD_NOT_ALLOWED);
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(TransactionSystemException.class)
	public ResponseEntity<Object> handleTransactionException(TransactionSystemException ex ,HttpStatus status, WebRequest request) {

		ErrorResponce error = ErrorResponce.builder()
				.errorCode(1)
				.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.errorMessage(HttpStatus.INTERNAL_SERVER_ERROR.name())
				.path(request.getDescription(false).substring(4))
				.timestamp(Timestamp.valueOf(LocalDateTime.now()))
				.traceID(Instant.now().toEpochMilli())
				.errorDetails(ex.getMessage())
				.build();

		LoggingResponseMessage loggingResponseMessage2 = LoggingResponseMessage.builder()
				.data(error)
				.message(ex.getMessage())
				.messageTypeId(MessageTypeConst.INTERNAL_ERROR)
				.statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
				.build();

		log.error(gson.toJson(loggingResponseMessage2));

		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(InvalidDataAccessApiUsageException.class)
	public ResponseEntity<Object> handleInvalidDataAccessApiUsageException(InvalidDataAccessApiUsageException ex , HttpStatus status, WebRequest request) {

		ErrorResponce error = ErrorResponce.builder()
				.errorCode(1)
				.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.errorMessage(HttpStatus.INTERNAL_SERVER_ERROR.name())
				.path(request.getDescription(false).substring(4))
				.timestamp(Timestamp.valueOf(LocalDateTime.now()))
				.traceID(Instant.now().toEpochMilli())
				.errorDetails(ex.getMessage())
				.build();

		LoggingResponseMessage loggingResponseMessage2 = LoggingResponseMessage.builder()
				.data(error)
				.message(ex.getMessage())
				.messageTypeId(MessageTypeConst.INTERNAL_ERROR)
				.statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
				.build();

		log.error(gson.toJson(loggingResponseMessage2));

		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(ClassCastException.class)
	public ResponseEntity<Object> handleClassCastException(ClassCastException ex, HttpStatus status, WebRequest request) {

		ErrorResponce error = ErrorResponce.builder()
				.errorCode(1)
				.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.errorMessage(HttpStatus.INTERNAL_SERVER_ERROR.name())
				.path(request.getDescription(false).substring(4))
				.timestamp(Timestamp.valueOf(LocalDateTime.now()))
				.traceID(Instant.now().toEpochMilli())
				.errorDetails(ex.getMessage())
				.build();

		LoggingResponseMessage loggingResponseMessage2 = LoggingResponseMessage.builder()
				.data(error)
				.message(ex.getMessage())
				.messageTypeId(MessageTypeConst.INTERNAL_ERROR)
				.statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
				.build();

		log.error(gson.toJson(loggingResponseMessage2));

		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(JpaSystemException.class)
	public ResponseEntity<Object> handleJpaSystemException(JpaSystemException ex, HttpStatus status, WebRequest request) {

		ErrorResponce error = ErrorResponce.builder()
				.errorCode(1)
				.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.errorMessage(HttpStatus.INTERNAL_SERVER_ERROR.name())
				.path(request.getDescription(false).substring(4))
				.timestamp(Timestamp.valueOf(LocalDateTime.now()))
				.traceID(Instant.now().toEpochMilli())
				.errorDetails(ex.getMessage())
				.build();

		LoggingResponseMessage loggingResponseMessage2 = LoggingResponseMessage.builder()
				.data(error)
				.message(ex.getMessage())
				.messageTypeId(MessageTypeConst.INTERNAL_ERROR)
				.statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
				.build();

		log.error(gson.toJson(loggingResponseMessage2));

		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(JsonIOException.class)
	public ResponseEntity<Object> handleJsonIOException(JsonIOException ex, HttpStatus status, WebRequest request) {

		ErrorResponce error = ErrorResponce.builder()
				.errorCode(1)
				.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.errorMessage(HttpStatus.INTERNAL_SERVER_ERROR.name())
				.path(request.getDescription(false).substring(4))
				.timestamp(Timestamp.valueOf(LocalDateTime.now()))
				.traceID(Instant.now().toEpochMilli())
				.errorDetails(ex.getMessage())
				.build();

		LoggingResponseMessage loggingResponseMessage2 = LoggingResponseMessage.builder()
				.data(error)
				.message(ex.getMessage())
				.messageTypeId(MessageTypeConst.INTERNAL_ERROR)
				.statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
				.build();

		log.error(gson.toJson(loggingResponseMessage2));

		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(NullPointerException.class)
	public final ResponseEntity<Object> nullpointerExceptions(NullPointerException ex, WebRequest request) {

		ErrorResponce error = ErrorResponce.builder()
				.errorCode(1)
				.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.errorMessage(HttpStatus.INTERNAL_SERVER_ERROR.name())
				.path(request.getDescription(false).substring(4))
				.timestamp(Timestamp.valueOf(LocalDateTime.now()))
				.traceID(Instant.now().toEpochMilli())
				.errorDetails(ex.getMessage())
				.build();

		LoggingResponseMessage loggingResponseMessage2 = LoggingResponseMessage.builder()
				.data(error)
				.message(ex.getMessage())
				.messageTypeId(MessageTypeConst.INTERNAL_ERROR)
				.statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
				.build();

		log.error(gson.toJson(loggingResponseMessage2));

		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(Exception.class)
	public final ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {

		ErrorResponce error = ErrorResponce.builder()
				.errorCode(1)
				.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.errorMessage(HttpStatus.INTERNAL_SERVER_ERROR.name())
				.path(request.getDescription(false).substring(4))
				.timestamp(Timestamp.valueOf(LocalDateTime.now()))
				.traceID(Instant.now().toEpochMilli())
				.errorDetails(ex.getMessage())
				.build();

		LoggingResponseMessage loggingResponseMessage2 = LoggingResponseMessage.builder()
				.data(error)
				.message(ex.getMessage())
				.messageTypeId(MessageTypeConst.INTERNAL_ERROR)
				.statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
				.build();

		log.error(gson.toJson(loggingResponseMessage2));

		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Override
	protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {

		ErrorResponce error = ErrorResponce.builder()
				.errorCode(1).status(HttpStatus.NOT_FOUND.value())
				.errorMessage(HttpStatus.NOT_FOUND.name())
				.path(request.getDescription(false).substring(4))
				.timestamp(Timestamp.valueOf(LocalDateTime.now()))
				.traceID(Instant.now().toEpochMilli())
				.errorDetails(ex.getMessage())
				.build();

		LoggingResponseMessage loggingResponseMessage2 = LoggingResponseMessage.builder()
				.data(error)
				.message(ex.getMessage())
				.messageTypeId(MessageTypeConst.ERROR)
				.statusCode(HttpStatus.NOT_FOUND)
				.build();

		log.error(gson.toJson(loggingResponseMessage2));

		return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
	}
}
