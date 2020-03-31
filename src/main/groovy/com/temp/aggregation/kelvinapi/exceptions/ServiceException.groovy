package com.temp.aggregation.kelvinapi.exceptions

class ServiceException extends RuntimeException {
  private static final long serialVersionUID = -7940416327131181975L

  private ServiceError serviceError
  final List<Object[]> errorProperties = []

  ServiceException() {
    super()
  }

  ServiceException(ServiceError serviceError) {
    super()
    this.serviceError = serviceError
  }

  ServiceException(ServiceError serviceError, Object... errorProperties) {
    this(serviceError, [errorProperties])
  }

  ServiceException(ServiceError serviceError, Throwable throwable) {
    super(throwable)
    this.serviceError = serviceError
  }

  ServiceException(ServiceError serviceError, List<Object[]> errorProperties) {
    this.serviceError = serviceError
    this.errorProperties = errorProperties
  }

  ServiceError getServiceError() {
    if (serviceError == null) {
      serviceError = ServiceError.UNEXPECTED_ERROR
    }
    return this.serviceError
  }

  @Override
  String getMessage() {
    return getMessages().join('; ') ?: null
  }

  List<String> getMessages() {
    if (serviceError) {
      List<String> errors = errorProperties.collect { serviceError.getDescriptionWithProperties(it) }
      errors = errors ?: [serviceError.errorDescription]
      return errors
    }
    return [super.getMessage()]
  }
}
