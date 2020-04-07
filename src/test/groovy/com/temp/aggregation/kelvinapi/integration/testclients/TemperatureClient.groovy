package com.temp.aggregation.kelvinapi.integration.testclients

import com.temp.aggregation.kelvinapi.domain.ErrorResponse
import com.temp.aggregation.kelvinapi.domain.ListResponse
import com.temp.aggregation.kelvinapi.domain.Temperature
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

import static org.springframework.web.bind.annotation.RequestMethod.*

@FeignClient(
    name = 'temperatures',
    url = '${feign.app.url}',
    decode404 = true
)
interface TemperatureClient {
  @RequestMapping(method = GET, value = '/temperatures')
  ResponseEntity<ListResponse<Temperature>> getTemperatures(@RequestParam(value = 'organization_id') String organizationId)

  @RequestMapping(method = POST, value = '/temperatures')
  ResponseEntity<List<Temperature>> saveTemperatures(
      @RequestHeader('x-organization-pin') String organizationPin,
      @RequestBody List<Temperature> temperatures
  )

  @RequestMapping(method = GET, value = '/temperatures/{id}')
  ResponseEntity<Temperature> getTemperature(@PathVariable(value = 'id') String temperatureId)

  @RequestMapping(method = GET, value = '/temperatures/{id}')
  ResponseEntity<ErrorResponse> getTemperatureNotFound(@PathVariable(value = 'id') String temperatureId)

  @RequestMapping(method = DELETE, value = '/temperatures/{id}')
  ResponseEntity<Void> deleteTemperature(@PathVariable(value = 'id') String temperatureId)
}
