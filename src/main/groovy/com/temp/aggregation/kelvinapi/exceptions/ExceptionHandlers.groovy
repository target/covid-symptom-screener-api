package com.temp.aggregation.kelvinapi.exceptions

import com.temp.aggregation.kelvinapi.domain.ErrorResponse
import groovy.util.logging.Slf4j
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.util.StringUtils
import org.springframework.validation.BindException
import org.springframework.validation.FieldError
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.NoHandlerFoundException

import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException
import java.lang.reflect.ParameterizedType

@RestControllerAdvice
@Slf4j
class ExceptionHandlers {

  @ExceptionHandler([
      HttpMessageNotReadableException,
      IllegalArgumentException,
      MissingServletRequestParameterException
  ])
  ResponseEntity<ErrorResponse> handleBadRequestException(Exception e) {
    return generateResponse(HttpStatus.BAD_REQUEST, [e.message], e)
  }

  @ExceptionHandler([MethodArgumentNotValidException])
  ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
    List<String> errors = e.bindingResult.getAllErrors().collect {
      if (FieldError.isAssignableFrom(it.class)) {
        FieldError fieldError = it as FieldError
        return "${convertPathToSnakeCase(fieldError.field)} ${fieldError.defaultMessage}" as String
      }

      return it.defaultMessage
    }
    return generateResponse(HttpStatus.BAD_REQUEST, errors, e)
  }

  @ExceptionHandler([ConstraintViolationException])
  ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException e) {
    List<String> errors = e.constraintViolations?.collect { ConstraintViolation violation ->
      String propertyPath = convertPathToSnakeCase(violation.getPropertyPath()?.toString()) ?: 'null'
      String field = propertyPath == 'null' ? '' : "$propertyPath "
      return "${field}${violation.message}".toString()
    }
    return generateResponse(HttpStatus.BAD_REQUEST, errors, e)
  }

  @ExceptionHandler([NoHandlerFoundException, HttpRequestMethodNotSupportedException])
  ResponseEntity<ErrorResponse> handleResourceNotFoundException(Exception e) {
    return generateResponse(HttpStatus.NOT_FOUND, [e.message], e)
  }

  @ExceptionHandler([BindException])
  ResponseEntity<ErrorResponse> handleBindException(BindException e) {
    // BindException swallows the original exception and stores the original message (plus some junk) in the
    // included errors. So strip out the junk and create detail objects for each error.
    List<String> errors = e.getAllErrors().collect {
      it.defaultMessage.replaceFirst(/.*ServiceException: /, '')
    }

    return generateResponse(HttpStatus.BAD_REQUEST, errors, e)
  }

  @ExceptionHandler([ServiceException])
  ResponseEntity<ErrorResponse> handleServiceException(ServiceException e) {
    return generateResponse(e.serviceError.httpStatus, e.getMessages(), e)
  }

  @ExceptionHandler
  ResponseEntity<ErrorResponse> handleException(Exception e) {
    return generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, [e.message], e)
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException)
  ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
    String typeDescription
    if (List.isAssignableFrom(e.requiredType)) {
      ParameterizedType type = e.parameter.getGenericParameterType() as ParameterizedType
      Class<?> requiredType = type.getActualTypeArguments().first() as Class<?>
      typeDescription = "list of ${requiredType.getSimpleName()}"
    } else {
      typeDescription = e.requiredType.getSimpleName()
    }
    String message = "Unable to convert value of parameter ${e.name} to type $typeDescription"
    return generateResponse(HttpStatus.BAD_REQUEST, [message], e)
  }

  private ResponseEntity<ErrorResponse> generateResponse(HttpStatus status, List<String> errors, Exception e) {
    // only log stack traces for 500s
    if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
      log.error("${status.value()} error: ", e)
    } else {
      log.warn("${status.value()} error: ${errors.join(', ')} due to ${partialStackTrace(e)}")
    }

    return new ResponseEntity<ErrorResponse>(
        new ErrorResponse(
            message: status.reasonPhrase,
            errors: errors.collect { return escapeBrackets(it) }
        ),
        status
    )
  }

  static String convertPathToSnakeCase(String field) {
    String snakeCase = null
    if (field) {
      snakeCase = field.split(/\./).collect { convertToSnakeCase(it as String) }.join('.')
    }
    return snakeCase
  }

  static String convertToSnakeCase(String fieldName) {
    String snakeCase = null
    if (fieldName) {
      snakeCase = fieldName.replaceAll('(.)(\\p{Upper})', '$1_$2').toLowerCase()
    }
    return snakeCase
  }

  private static final Map<String, String> REPLACE_PATTERNS = [
      ('>'): '%3E',
      ('<'): '%3C'
  ]

  static String escapeBrackets(String text) {
    String escapedText = text
    REPLACE_PATTERNS.each { String pattern, String replacement ->
      escapedText = StringUtils.replace(escapedText, pattern, replacement)
    }

    return escapedText
  }

  private StackTraceElement partialStackTrace(Exception e) {
    return e.getStackTrace() ? e.getStackTrace()[0] : null
  }
}
