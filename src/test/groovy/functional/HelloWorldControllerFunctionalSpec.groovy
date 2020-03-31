package functional

import com.temp.aggregation.kelvinapi.KelvinApiApplication
import functional.testClients.HelloWorldClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import static org.springframework.http.HttpStatus.OK

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = KelvinApiApplication.class)
@ActiveProfiles('integration')
@EnableFeignClients
class HelloWorldControllerFunctionalSpec extends Specification {
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
