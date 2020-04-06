package com.temp.aggregation.kelvinapi.controllers

import com.temp.aggregation.kelvinapi.domain.Enumeration
import com.temp.aggregation.kelvinapi.domain.ListResponse
import com.temp.aggregation.kelvinapi.exceptions.ServiceError
import com.temp.aggregation.kelvinapi.exceptions.ServiceException
import com.temp.aggregation.kelvinapi.services.EnumerationsService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Slf4j
@RestController
class EnumerationsController {

  @Autowired
  EnumerationsService enumerationsService

  @GetMapping('/enumerations')
  @ResponseStatus(HttpStatus.OK)
  ListResponse<Enumeration> listEnumerations() {
    log.info('Request to list enumerations')
    List<Enumeration> enumerations = enumerationsService.listEnumerations()

    return new ListResponse<Enumeration>(results: enumerations, total: enumerations.size())
  }

  @GetMapping('/enumerations/{name}')
  @ResponseStatus(HttpStatus.OK)
  Enumeration getEnumeration(@PathVariable(value = 'name') String name
  ) {
    log.info("Request to get an enumeration for name $name")
    Enumeration enumeration = enumerationsService.getEnumeration(name)
    if (enumeration) {
      return enumeration
    }

    throw new ServiceException(ServiceError.NOT_FOUND, 'enumeration')
  }

}
