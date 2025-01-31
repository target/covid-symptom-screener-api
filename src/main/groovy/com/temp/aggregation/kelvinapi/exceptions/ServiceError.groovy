package com.temp.aggregation.kelvinapi.exceptions

import org.springframework.http.HttpStatus

enum ServiceError {
  UNAUTHORIZED(HttpStatus.FORBIDDEN, 'Required role not present for user.'),
  AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, 'Could not authenticate the user.'),
  ORGANIZATION_CONFLICT(HttpStatus.CONFLICT, 'Organization with tax id %s exists already.'),
  INVALID_ORGANIZATION_STATE_CHANGE(HttpStatus.BAD_REQUEST, 'Invalid organization state change.'),
  NOT_FOUND(HttpStatus.NOT_FOUND, '%s not found'),
  ORGANIZATION_NOT_APPROVED(HttpStatus.FORBIDDEN, 'No approved organization found for provided auth code.'),
  ASSESSMENT_QUESTION_CONFLICT(HttpStatus.CONFLICT, 'An AssessmentQuestion with the provided display_value exists already.'),
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
