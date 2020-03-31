package com.temp.aggregation.kelvinapi.integration

import com.temp.aggregation.kelvinapi.KelvinApiApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.ApplicationContext
import spock.lang.Specification

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = KelvinApiApplication)
class ApplicationIntegrationSpec extends Specification {
    @LocalServerPort
    int serverPort

    @Autowired
    ApplicationContext applicationContext

    void 'got application context'() {
        expect:
        serverPort != null
        applicationContext.id == 'blah'
    }
}
