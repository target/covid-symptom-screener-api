package com.temp.aggregation.kelvinapi.integration.testclients

import com.temp.aggregation.kelvinapi.domain.Enumeration
import com.temp.aggregation.kelvinapi.domain.ListResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

import static org.springframework.web.bind.annotation.RequestMethod.GET

@FeignClient(
    name = 'enumeration',
    url = '${feign.app.url}',
    decode404 = true
)
interface EnumerationClient {

  @RequestMapping(method = GET, value = '/enumerations/{name}')
  ResponseEntity<Enumeration> getEnumeration(@PathVariable(value = 'name') String name
  )

  @RequestMapping(method = GET, value = '/enumerations')
  ResponseEntity<ListResponse<Enumeration>> listEnumerations()

}
