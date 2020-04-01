package com.temp.aggregation.kelvinapi.exceptions

import com.temp.aggregation.kelvinapi.domain.ErrorResponse
import groovy.util.logging.Slf4j
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestControllerAdvice
@Slf4j
class ExceptionHandlers {
  @SuppressWarnings('UnusedMethodParameter')
  @ExceptionHandler(ServiceException)
  ResponseEntity<ErrorResponse> handleServiceException(HttpServletRequest req,
                                                       ServiceException ex,
                                                       HttpServletResponse res) {
    log.error("Service exception in ${req.getMethod()} ${req.getRequestURI()}: ${ex.getMessage()}", ex)
    ErrorResponse errorResponse = new ErrorResponse(
        message: ex.serviceError.getDescriptionWithProperties(req.requestURI),
        errors: []
    )
    return new ResponseEntity<ErrorResponse>(errorResponse, ex.serviceError.httpStatus)
  }
}
