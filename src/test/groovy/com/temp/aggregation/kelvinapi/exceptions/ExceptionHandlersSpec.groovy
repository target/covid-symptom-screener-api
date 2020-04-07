package com.temp.aggregation.kelvinapi.exceptions

import com.temp.aggregation.kelvinapi.domain.ErrorResponse
import com.temp.aggregation.kelvinapi.domain.OrganizationDTO
import org.hibernate.validator.internal.engine.ConstraintViolationImpl
import org.hibernate.validator.internal.engine.path.PathImpl
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import spock.lang.Specification
import spock.lang.Unroll

import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException
import java.lang.annotation.ElementType
import java.lang.reflect.ParameterizedType

class ExceptionHandlersSpec extends Specification {

  ExceptionHandlers exceptionHandlers = new ExceptionHandlers()

  void 'handleBadRequestException'() {

    given:
    Exception exception = new Exception('message')

    when:
    ResponseEntity<ErrorResponse> responseEntity = exceptionHandlers.handleBadRequestException(exception)

    then:
    responseEntity.statusCode == HttpStatus.BAD_REQUEST
    responseEntity.body.message == HttpStatus.BAD_REQUEST.reasonPhrase
    responseEntity.body.errors == [exception.message]
  }

  void 'handleBadRequestException escapes brackets'() {

    given:
    Exception exception = new Exception('<script>this is a message</script>')

    when:
    ResponseEntity<ErrorResponse> responseEntity = exceptionHandlers.handleBadRequestException(exception)

    then:
    responseEntity.statusCode == HttpStatus.BAD_REQUEST
    responseEntity.body.message == HttpStatus.BAD_REQUEST.reasonPhrase
    responseEntity.body.errors == ['%3Cscript%3Ethis is a message%3C/script%3E']
  }

  void 'handleValidationException'() {

    given:
    MethodParameter parameter = Mock(MethodParameter)
    BindingResult bindingResult = Mock(BindingResult)
    MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult)

    when:
    ResponseEntity<ErrorResponse> responseEntity = exceptionHandlers.handleValidationException(exception)

    then:
    1 * bindingResult.getAllErrors() >> [
        new ObjectError('obj', 'message'),
        new FieldError('obj', 'field', 'the message')
    ]
    responseEntity.statusCode == HttpStatus.BAD_REQUEST
    responseEntity.body.message == HttpStatus.BAD_REQUEST.reasonPhrase
    responseEntity.body.errors == ['message', 'field the message']
  }

  void 'handleResourceNotFoundException'() {

    given:
    HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException('test')

    when:
    ResponseEntity<ErrorResponse> responseEntity = exceptionHandlers.handleResourceNotFoundException(exception)

    then:
    responseEntity.statusCode == HttpStatus.NOT_FOUND
    responseEntity.body.message == HttpStatus.NOT_FOUND.reasonPhrase
    responseEntity.body.errors == [exception.message]
  }

  void 'handleBindException'() {

    given:
    BindingResult bindingResult = Mock(BindingResult)
    BindException exception = new BindException(bindingResult)

    when:
    ResponseEntity<ErrorResponse> responseEntity = exceptionHandlers.handleBindException(exception)

    then:
    1 * bindingResult.getAllErrors() >> [new ObjectError('obj', 'com.ServiceException: message')]
    responseEntity.statusCode == HttpStatus.BAD_REQUEST
    responseEntity.body.message == HttpStatus.BAD_REQUEST.reasonPhrase
    responseEntity.body.errors == ['message']
  }

  void 'handleServiceException'() {

    given:
    ServiceException exception = new ServiceException(ServiceError.NOT_FOUND, 'test')

    when:
    ResponseEntity<ErrorResponse> responseEntity = exceptionHandlers.handleServiceException(exception)

    then:
    responseEntity.statusCode == HttpStatus.NOT_FOUND
    responseEntity.body.message == HttpStatus.NOT_FOUND.reasonPhrase
    responseEntity.body.errors == [exception.message]
  }

  void 'handleException'() {

    given:
    Exception exception = new Exception('message')

    when:
    ResponseEntity<ErrorResponse> responseEntity = exceptionHandlers.handleException(exception)

    then:
    responseEntity.statusCode == HttpStatus.INTERNAL_SERVER_ERROR
    responseEntity.body.message == HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase
    responseEntity.body.errors == [exception.message]
  }

  @Unroll
  void 'handleConstraintViolationException'() {
    given:
    ConstraintViolation<OrganizationDTO> violation = ConstraintViolationImpl.forBeanValidation(
        'not null',
        [:],
        [:],
        'may not be null',
        OrganizationDTO,
        new OrganizationDTO(),
        new OrganizationDTO(),
        null,
        propertyPath,
        null,
        ElementType.FIELD,
        null)
    ConstraintViolationException exception = new ConstraintViolationException('Invalid', [violation].toSet())

    when:
    ResponseEntity<ErrorResponse> responseEntity = exceptionHandlers.handleConstraintViolationException(exception)

    then:
    responseEntity.statusCode == HttpStatus.BAD_REQUEST
    responseEntity.body.message == HttpStatus.BAD_REQUEST.reasonPhrase
    responseEntity.body.errors == [error]

    where:
    propertyPath                                       | error
    PathImpl.createPathFromString('legalBusinessName') | 'legal_business_name may not be null'
    PathImpl.createPathFromString('null')              | 'may not be null'
    PathImpl.createPathFromString('')                  | 'may not be null'
    PathImpl.createRootPath()                          | 'may not be null'
    null                                               | 'may not be null'
  }

  void 'handle MethodArgumentTypeMismatchException for object'() {

    given:
    MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
        'parameter_value',
        Integer,
        'parameter_name',
        null,
        new Exception()
    )

    when:
    ResponseEntity<ErrorResponse> response = exceptionHandlers.handleMethodArgumentTypeMismatchException(exception)

    then:
    response.statusCode == HttpStatus.BAD_REQUEST
    response.body.errors == ['Unable to convert value of parameter parameter_name to type Integer']
  }

  void 'handle MethodArgumentTypeMismatchException for list'() {

    given:
    MethodParameter mockMethodParameter = Mock(MethodParameter)
    ParameterizedType mockParameterizedType = Mock(ParameterizedType)
    MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
        'parameter_value',
        List,
        'parameter_name',
        mockMethodParameter,
        new Exception()
    )

    when:
    ResponseEntity<ErrorResponse> response = exceptionHandlers.handleMethodArgumentTypeMismatchException(exception)

    then:
    1 * mockMethodParameter.getGenericParameterType() >> mockParameterizedType
    1 * mockParameterizedType.getActualTypeArguments() >> [Integer]
    0 * _

    response.statusCode == HttpStatus.BAD_REQUEST
    response.body.errors == ['Unable to convert value of parameter parameter_name to type list of Integer']
  }
}
