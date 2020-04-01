package com.temp.aggregation.kelvinapi.integration.controllers

import com.temp.aggregation.kelvinapi.domain.Temperature
import com.temp.aggregation.kelvinapi.domain.TemperatureListResponse
import com.temp.aggregation.kelvinapi.integration.BaseIntegrationSpec
import com.temp.aggregation.kelvinapi.integration.testclients.TemperatureClient
import com.temp.aggregation.kelvinapi.repositories.TemperatureRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

import static org.springframework.data.domain.Sort.Direction.ASC

class TemperatureControllerFunctionalSpec extends BaseIntegrationSpec {
    @Autowired
    TemperatureClient client

    @Autowired
    TemperatureRepository repository

    void cleanup() {
        repository.deleteAll()
    }

    void 'can get temperatures by org id'() {
        given:
        String organizationId = 'testOrgA'
        List<Temperature> temperatures = [
                new Temperature(
                        organizationId: organizationId,
                        temperature: 98.6,
                        userId: 'test-user-a',
                        latitude: 44.934940,
                        longitude: -93.158660
                ),
                new Temperature(
                        organizationId: organizationId,
                        temperature: 100.5,
                        userId: 'test-user-b',
                        latitude: 44.934941,
                        longitude: -93.158661
                )
                ,
                new Temperature(
                        organizationId: 'aDifferentOrg',
                        temperature: 100.5,
                        userId: 'test-user-b',
                        latitude: 44.934941,
                        longitude: -93.158661
                )
        ]
        repository.saveAll(temperatures)

        when:
        ResponseEntity<TemperatureListResponse> response = client.getTemperatures(organizationId)

        then:
        response.statusCode == HttpStatus.OK
        response.body.results*.temperature.containsAll([98.6f, 100.5f])
        response.body.results*.userId.containsAll(['test-user-a', 'test-user-b'])
        response.body.results*.organizationId.unique() == [organizationId]
        response.body.results*.latitude.containsAll([44.934940f, 44.934941f])
        response.body.results*.longitude.containsAll([-93.158660f, -93.158661f])
    }

    void 'can save temperatures via the API'() {
        String organizationId = 'testOrgB'
        List<Temperature> temperatures = [
                new Temperature(
                        organizationId: organizationId,
                        temperature: 98.6,
                        userId: 'test-user-a',
                        latitude: 44.934940,
                        longitude: -93.158660
                ),
                new Temperature(
                        organizationId: organizationId,
                        temperature: 100.5,
                        userId: 'test-user-b',
                        latitude: 44.934941,
                        longitude: -93.158661
                )
        ]

        when:
        ResponseEntity<TemperatureListResponse> response = client.saveTemperatures(temperatures)

        then:
        response.statusCode == HttpStatus.CREATED
        response.body.results.size() == 2

        when: 'confirm they are in the db'
        Page<Temperature> retrieved = repository.findAllByOrganizationId(organizationId, new PageRequest(0, 100, new Sort(ASC, ['id'])))

        then:
        retrieved.content.size() == 2
        retrieved.content*.id.containsAll(response.body.results*.id)
    }
}
