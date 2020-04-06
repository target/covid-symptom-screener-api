package com.temp.aggregation.kelvinapi.controllers

import com.temp.aggregation.kelvinapi.domain.ListResponse
import com.temp.aggregation.kelvinapi.domain.Temperature
import com.temp.aggregation.kelvinapi.domain.TemperatureUpdate
import com.temp.aggregation.kelvinapi.security.UserRoleService
import com.temp.aggregation.kelvinapi.services.OrganizationService
import com.temp.aggregation.kelvinapi.services.TemperaturesService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Slf4j
@RestController
class TemperaturesController {
  @Autowired
  TemperaturesService temperaturesService

  @Autowired
  OrganizationService organizationService

  @Autowired
  UserRoleService userRoleService

  @GetMapping('/temperatures/{id}')
  @ResponseStatus(HttpStatus.OK)
  Temperature getTemperature(@PathVariable(value = 'id') String temperatureId) {
    log.info("Request to get temperature with id $temperatureId")
    return temperaturesService.findById(temperatureId)
  }

  @GetMapping('/temperatures')
  @ResponseStatus(HttpStatus.OK)
  ListResponse<Temperature> getTemperatures(
      @RequestParam(name = 'organization_id', required = false) String organizationId,
      Pageable pageable
  ) {
    log.info("Request to list temperatures with organization $organizationId")
    userRoleService.requireAdmin()

    Page<Temperature> temperatures = temperaturesService.getTemperaturesFor(organizationId, pageable)
    return new ListResponse<Temperature>(results: temperatures.content, total: temperatures.totalElements)
  }

  @PostMapping('/temperatures')
  @ResponseStatus(HttpStatus.CREATED)
  List<Temperature> saveTemperatures(
      @RequestHeader('x-authorization-code') String organizationAuthCode,
      @RequestBody List<TemperatureUpdate> temperatures
  ) {
    log.info("Request to create temperature with auth code $organizationAuthCode")

    return temperaturesService.saveAll(temperatures, organizationAuthCode)
  }

  @DeleteMapping('/temperatures/{id}')
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void deleteTemperature(@PathVariable(value = 'id') String temperatureId) {
    log.info("Request to delete temperature with id $temperatureId")

    temperaturesService.deleteById(temperatureId)
  }
}
