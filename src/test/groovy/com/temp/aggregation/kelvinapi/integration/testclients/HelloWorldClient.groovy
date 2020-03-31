package com.temp.aggregation.kelvinapi.integration.testclients

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

import static org.springframework.web.bind.annotation.RequestMethod.GET

@FeignClient(name = 'hello', url = '${feign.app.url}')
interface HelloWorldClient {
    @RequestMapping(method = GET, value = '/hello')
    ResponseEntity<String> sayHello(@RequestParam(value = 'name', required = false) String name)
}
