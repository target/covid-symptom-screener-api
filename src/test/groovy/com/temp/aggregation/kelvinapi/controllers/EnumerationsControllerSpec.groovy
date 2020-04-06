package com.temp.aggregation.kelvinapi.controllers

import com.temp.aggregation.kelvinapi.domain.Enumeration
import com.temp.aggregation.kelvinapi.domain.ListResponse
import com.temp.aggregation.kelvinapi.services.EnumerationsService
import spock.lang.Specification

class EnumerationsControllerSpec extends Specification {

  EnumerationsController enumerationsController

  void setup() {
    enumerationsController = new EnumerationsController(
        enumerationsService: Mock(EnumerationsService)
    )
  }

  void 'ListEnumerations'() {

    given:
    List<Enumeration> expected = [new Enumeration(name: 'name', values: ['value'])]

    when:
    ListResponse<Enumeration> actual = enumerationsController.listEnumerations()

    then:
    1 * enumerationsController.enumerationsService.listEnumerations() >> expected
    0 * _

    actual.results == expected
    actual.total == 1
  }

  void 'GetEnumeration'() {
    given:
    String name = 'name'
    Enumeration expected = new Enumeration(name: 'name', values: ['value'])

    when:
    Enumeration actual = enumerationsController.getEnumeration(name)

    then:
    1 * enumerationsController.enumerationsService.getEnumeration(name) >> expected
    0 * _

    actual == expected
  }
}
