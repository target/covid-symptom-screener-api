package com.temp.aggregation.kelvinapi.controllers

import com.temp.aggregation.kelvinapi.domain.ListResponse
import com.temp.aggregation.kelvinapi.domain.TemperatureDTO
import com.temp.aggregation.kelvinapi.security.UserRoleService
import com.temp.aggregation.kelvinapi.services.OrganizationsService
import com.temp.aggregation.kelvinapi.services.TemperaturesService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Slf4j
@RestController
class TemperaturesController {
  @Autowired
  TemperaturesService temperaturesService

  @Autowired
  OrganizationsService organizationService

  @Autowired
  UserRoleService userRoleService

  @GetMapping('/temperatures/{id}')
  @ResponseStatus(HttpStatus.OK)
  TemperatureDTO getTemperature(@PathVariable(value = 'id') String temperatureId) {
    log.info("Request to get temperature with id $temperatureId")
    return temperaturesService.findById(temperatureId)
  }

  @GetMapping('/temperatures')
  @ResponseStatus(HttpStatus.OK)
  ListResponse<TemperatureDTO> getTemperatures(
      @RequestParam(name = 'organization_id', required = false) String organizationId,
      Pageable pageable
  ) {
    log.info("Request to list temperatures with organization $organizationId")
    userRoleService.requireAdmin()

    Page<TemperatureDTO> temperatures = temperaturesService.getTemperaturesFor(organizationId, pageable)
    ListResponse<TemperatureDTO> temperaturesListResponse =
        new ListResponse<TemperatureDTO>(results: temperatures.content, total: temperatures.totalElements)
    return temperaturesListResponse
  }

  @PostMapping('/temperatures')
  @ResponseStatus(HttpStatus.CREATED)
  List<TemperatureDTO> saveTemperatures(
      @RequestHeader('x-organization-pin') String organizationPin,
      @RequestBody List<TemperatureDTO> temperatures
  ) {
    log.info("Request to create temperature with auth code $organizationPin")

    return temperaturesService.saveAll(temperatures, organizationPin)
  }

  @DeleteMapping('/temperatures/{id}')
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void deleteTemperature(@PathVariable(value = 'id') String temperatureId) {
    log.info("Request to delete temperature with id $temperatureId")

    temperaturesService.deleteById(temperatureId)
  }
}
