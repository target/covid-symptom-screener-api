package com.temp.aggregation.kelvinapi.integration

import com.temp.aggregation.kelvinapi.KelvinApiApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.ApplicationContext
import org.springframework.core.env.Environment
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = KelvinApiApplication)
@ActiveProfiles('integration')
@EnableFeignClients
class BaseIntegrationSpec extends Specification {
    @LocalServerPort
    int serverPort

    @Autowired
    Environment env

    @Autowired
    ApplicationContext applicationContext
}
