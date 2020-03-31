package com.temp.aggregation.kelvinapi.controllers

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloWorldController {

    @GetMapping('/hello')
    @ResponseStatus(HttpStatus.OK)
    String sayHello(@RequestParam(value = 'name', required = false, defaultValue = 'fella') String name) {
        return "Hello, ${name}!"
    }
}
