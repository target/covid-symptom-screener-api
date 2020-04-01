package com.temp.aggregation.kelvinapi.integration.testclients

import com.temp.aggregation.kelvinapi.domain.Temperature
import com.temp.aggregation.kelvinapi.domain.TemperatureListResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

import static org.springframework.web.bind.annotation.RequestMethod.GET
import static org.springframework.web.bind.annotation.RequestMethod.POST

@FeignClient(name = 'temperature', url = '${feign.app.url}')
interface TemperatureClient {
    @RequestMapping(method = GET, value = '/temperatures')
    ResponseEntity<TemperatureListResponse> getTemperatures(@RequestParam(value = 'organization_id') String organizationId)

    @RequestMapping(method = POST, value = '/temperatures')
    ResponseEntity<TemperatureListResponse> saveTemperatures(@RequestBody List<Temperature> temperatures)
}
