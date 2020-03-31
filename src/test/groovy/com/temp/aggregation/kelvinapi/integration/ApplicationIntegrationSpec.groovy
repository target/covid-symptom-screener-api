package com.temp.aggregation.kelvinapi.integration

import com.temp.aggregation.kelvinapi.KelvinApiApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.ApplicationContext
import org.springframework.core.env.Environment
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = KelvinApiApplication.class)
@ActiveProfiles('integration')
class ApplicationIntegrationSpec extends Specification {
    @LocalServerPort
    int serverPort

    @Autowired
    Environment env

    @Autowired
    ApplicationContext applicationContext

    void 'got application context'() {
        expect:
        serverPort == 8080
        env.activeProfiles.toList() == ['integration']
        applicationContext.id == 'kelvin-api'
    }
}
