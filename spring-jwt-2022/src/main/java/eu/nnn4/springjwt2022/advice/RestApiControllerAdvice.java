package eu.nnn4.springjwt2022.advice;

import eu.nnn4.springjwt2022.exception.ResourceNotFoundException;
import eu.nnn4.springjwt2022.exception.TokenBaseException;
import eu.nnn4.springjwt2022.exception.UserApiNotAllowedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

@Slf4j
@RestControllerAdvice
public class RestApiControllerAdvice {

  @ExceptionHandler(value = {TokenBaseException.class, UserApiNotAllowedException.class})
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ErrorMessage handleTokenRefreshException(RuntimeException ex, WebRequest request) {
    return new ErrorMessage(
        HttpStatus.FORBIDDEN.value(),
        new Date(),
        ex.getMessage(),
        request.getDescription(false));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorMessage> handleException(HttpMessageNotReadableException e){
    log.info("error class: "+e.getClass());
    ErrorMessage message= new ErrorMessage(HttpStatus.BAD_REQUEST.value(), new Date(), e.getMessage(),"Payload not correctly defined");
    return new ResponseEntity<>(message,HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorMessage> handleException(MethodArgumentNotValidException e){
    BindingResult result = e.getBindingResult();
    FieldError error = result.getFieldError();
    String errorString="Field: "+ error.getField()+" has not valid value: "+error.getRejectedValue();
    if(!error.getDefaultMessage().isBlank())
      errorString=errorString+", "+error.getDefaultMessage();
    ErrorMessage message= new ErrorMessage(HttpStatus.BAD_REQUEST.value(), new Date(),errorString ,"Payload not valid");
    return new ResponseEntity<>(message,HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler({ResourceNotFoundException.class, IllegalArgumentException.class})
  public ResponseEntity<ErrorMessage> handleException(RuntimeException e){
    log.info("error class: "+e.getClass());
    ErrorMessage message= new ErrorMessage(HttpStatus.BAD_REQUEST.value(), new Date(), e.getMessage(),"Payload not correctly defined");
    return new ResponseEntity<>(message,HttpStatus.BAD_REQUEST);
  }
}
