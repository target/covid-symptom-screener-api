package com.temp.aggregation.kelvinapi.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloWorld {

    @GetMapping('/hello')
    @ResponseStatus
    String sayHello(@RequestParam(value = 'name', required = false, defaultValue = 'fella') String name) {
        return "Hello, ${name}"
    }
}
