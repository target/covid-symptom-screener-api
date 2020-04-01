package com.temp.aggregation.kelvinapi.controllers

import com.temp.aggregation.kelvinapi.domain.ListResponse
import com.temp.aggregation.kelvinapi.domain.Temperature
import com.temp.aggregation.kelvinapi.services.OrganizationService
import com.temp.aggregation.kelvinapi.services.TemperaturesService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
class TemperaturesController {
  @Autowired
  TemperaturesService temperaturesService

  @Autowired
  OrganizationService organizationService

  @GetMapping('/temperatures/{id}')
  @ResponseStatus(HttpStatus.OK)
  Temperature getTemperature(@PathVariable(value = 'id') String temperatureId) {
    return temperaturesService.findById(temperatureId)
  }

  @GetMapping('/temperatures')
  @ResponseStatus(HttpStatus.OK)
  ListResponse<Temperature> getTemperatures(@RequestParam('organization_id') String organizationId,
                                            Pageable pageable) {
    Page<Temperature> temperatures = temperaturesService.getTemperaturesFor(organizationId, pageable)
    return new ListResponse<Temperature>(results: temperatures.content, total: temperatures.totalElements)
  }

  @PostMapping('/temperatures')
  @ResponseStatus(HttpStatus.CREATED)
  List<Temperature> saveTemperatures(
      @RequestHeader('x-authorization-code') String organizationAuthCode,
      @RequestBody List<Temperature> temperatures
  ) {
    return temperaturesService.saveAll(temperatures, organizationAuthCode)
  }

  @DeleteMapping('/temperatures/{id}')
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void deleteTemperature(@PathVariable(value = 'id') String temperatureId) {
    temperaturesService.deleteById(temperatureId)
  }
}
