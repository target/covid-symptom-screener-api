package com.temp.aggregation.kelvinapi.integration.controllers

import com.temp.aggregation.kelvinapi.domain.ErrorResponse
import com.temp.aggregation.kelvinapi.domain.ListResponse
import com.temp.aggregation.kelvinapi.domain.Organization
import com.temp.aggregation.kelvinapi.domain.Temperature
import com.temp.aggregation.kelvinapi.exceptions.ServiceError
import com.temp.aggregation.kelvinapi.integration.BaseIntegrationSpec
import com.temp.aggregation.kelvinapi.integration.testclients.TemperatureClient
import com.temp.aggregation.kelvinapi.repositories.OrganizationRepository
import com.temp.aggregation.kelvinapi.repositories.TemperatureRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

import static com.temp.aggregation.kelvinapi.domain.ApprovalStatus.APPROVED
import static org.springframework.data.domain.Sort.Direction.ASC

class TemperatureControllerFunctionalSpec extends BaseIntegrationSpec {
  @Autowired
  TemperatureClient client

  @Autowired
  TemperatureRepository temperatureRepository

  @Autowired
  OrganizationRepository organizationRepository

  void cleanup() {
    temperatureRepository.deleteAll()
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
    temperatureRepository.saveAll(temperatures)

    when:
    ResponseEntity<ListResponse<Temperature>> response = client.getTemperatures(organizationId)

    then:
    response.statusCode == HttpStatus.OK
    response.body.total == 2
    response.body.results*.temperature.containsAll([98.6f, 100.5f])
    response.body.results*.userId.containsAll(['test-user-a', 'test-user-b'])
    response.body.results*.organizationId.unique() == [organizationId]
    response.body.results*.latitude.containsAll([44.934940f, 44.934941f])
    response.body.results*.longitude.containsAll([-93.158660f, -93.158661f])
    response.body.results*.created
    response.body.results*.createdBy
    response.body.results*.lastModified
    response.body.results*.lastModifiedBy
  }

  void 'can save temperatures via the API'() {
    String orgAuthCode = 'auth1'
    Organization savedOrg = organizationRepository.save(
        new Organization(
            authorizationCode: orgAuthCode,
            taxId: '11111',
            orgName: 'testOrg',
            approvalStatus: APPROVED
        )
    )

    List<Temperature> temperatures = [
        new Temperature(
            temperature: 98.6,
            userId: 'test-user-a',
            latitude: 44.934940,
            longitude: -93.158660
        ),
        new Temperature(
            temperature: 100.5,
            userId: 'test-user-b',
            latitude: 44.934941,
            longitude: -93.158661
        )
    ]

    when:
    ResponseEntity<List<Temperature>> response = client.saveTemperatures(orgAuthCode, temperatures)

    then:
    response.statusCode == HttpStatus.CREATED
    response.body.size() == 2

    when: 'confirm they are in the db'
    Page<Temperature> retrieved = temperatureRepository.findAllByOrganizationId(savedOrg.id, PageRequest.of(0, 100, ASC, 'id'))

    then:
    retrieved.content.size() == 2
    retrieved.content*.id.containsAll(response.body*.id)
  }

  void 'can get individual temperature by id'() {
    given:
    String organizationId = 'testOrgC'
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
    List<Temperature> saved = temperatureRepository.saveAll(temperatures)

    when:
    ResponseEntity<Temperature> gotFirstResponse = client.getTemperature(saved.first().id)

    then:
    gotFirstResponse.statusCode == HttpStatus.OK
    gotFirstResponse.body == saved.first()

    when:
    ResponseEntity<Temperature> gotSecondResponse = client.getTemperature(saved.last().id)

    then:
    gotSecondResponse.statusCode == HttpStatus.OK
    gotSecondResponse.body == saved.last()
  }

  void '404 on get by id is handled'() {
    when:
    ResponseEntity<ErrorResponse> response = client.getTemperatureNotFound('bad-id')

    then:
    response.statusCode == HttpStatus.NOT_FOUND
    response.body.message == ServiceError.NOT_FOUND.getDescriptionWithProperties('/temperatures/bad-id')
  }

  void 'can delete individual temperature by id'() {
    given:
    String organizationId = 'testOrgD'
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
    List<Temperature> saved = temperatureRepository.saveAll(temperatures)

    when:
    ResponseEntity<Void> deletedFirst = client.deleteTemperature(saved.first().id)

    then:
    deletedFirst.statusCode == HttpStatus.NO_CONTENT
    deletedFirst.body == null

    and:
    temperatureRepository.findById(saved.first().id).orElse(null) == null

    when:
    ResponseEntity<Void> deletedSecond = client.deleteTemperature(saved.last().id)

    then:
    deletedSecond.statusCode == HttpStatus.NO_CONTENT
    deletedSecond.body == null

    and:
    temperatureRepository.findById(saved.last().id).orElse(null) == null
  }
}
