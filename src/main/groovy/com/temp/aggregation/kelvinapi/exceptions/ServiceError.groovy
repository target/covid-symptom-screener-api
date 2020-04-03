package com.temp.aggregation.kelvinapi.exceptions

import org.springframework.http.HttpStatus

enum ServiceError {
  ORGANIZATION_CONFLICT(HttpStatus.CONFLICT, 'Organization with tax id %s exists already'),
  NOT_FOUND(HttpStatus.NOT_FOUND, '%s not found'),
  ORGANIZATION_NOT_APPROVED(HttpStatus.FORBIDDEN, 'No approved organization found for provided auth code.'),
  UNAUTHORIZED(HttpStatus.FORBIDDEN, 'Required role not present for user.'),
  UNEXPECTED_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 'Unexpected error has occurred. %s')

  final String errorDescription
  final HttpStatus httpStatus

  String getDescriptionWithProperties(Object... properties) throws RuntimeException {
    String message = String.format(errorDescription, properties)
    return message
  }

  private ServiceError(HttpStatus httpStatus, String errorDescription) {
    this.errorDescription = errorDescription
    this.httpStatus = httpStatus
  }

  @Override
  String toString() {
    return "${this.name()}:${this.errorDescription}"
  }
}
