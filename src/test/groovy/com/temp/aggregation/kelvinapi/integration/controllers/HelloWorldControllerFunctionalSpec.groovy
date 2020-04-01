package com.temp.aggregation.kelvinapi.integration.controllers

import com.temp.aggregation.kelvinapi.integration.BaseIntegrationSpec
import com.temp.aggregation.kelvinapi.integration.testclients.HelloWorldClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity

import static org.springframework.http.HttpStatus.OK

class HelloWorldControllerFunctionalSpec extends BaseIntegrationSpec {
    @Autowired
    HelloWorldClient client

    void 'say hello'() {
        when:
        ResponseEntity<String> response = client.sayHello(name)

        then:
        response.statusCode == OK
        response.body == expected

        where:
        name      | expected
        'Douglas' | 'Hello, Douglas!'
        'Connie'  | 'Hello, Connie!'
        'Jorge'   | 'Hello, Jorge!'
        null      | 'Hello, fella!'
    }
}
