package com.temp.aggregation.kelvinapi.integration.controllers

import com.temp.aggregation.kelvinapi.domain.Enumeration
import com.temp.aggregation.kelvinapi.domain.ListResponse
import com.temp.aggregation.kelvinapi.integration.BaseIntegrationSpec
import com.temp.aggregation.kelvinapi.integration.testclients.EnumerationClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class EnumerationsControllerFunctionalSpec extends BaseIntegrationSpec {

  @Autowired
  EnumerationClient client

  void 'get enumeration'() {

    when:
    ResponseEntity<Enumeration> response = client.getEnumeration('ApprovalStatus')

    then:
    response.statusCode == HttpStatus.OK
    response.body.name == 'ApprovalStatus'
  }

  void 'list organizations'() {

    when:
    ResponseEntity<ListResponse<Enumeration>> response = client.listEnumerations()

    then:
    response.statusCode == HttpStatus.OK
    response.body.results.size() == 3
  }

}
