package com.temp.aggregation.kelvinapi.services

import com.temp.aggregation.kelvinapi.domain.Enumeration
import spock.lang.Specification

class EnumerationsServiceSpec extends Specification {

  EnumerationsService enumerationsService = new EnumerationsService()

  void 'ListEnumerations'() {

    when:
    List<Enumeration> enumerations = enumerationsService.listEnumerations()

    then:
    enumerations.size() == 3
    enumerations.find { it.name == 'ApprovalStatus' }.values.size() == 4
  }

  void 'GetEnumeration'() {
    when:
    Enumeration enumeration = enumerationsService.getEnumeration('ApprovalStatus')

    then:
    enumeration.name == 'ApprovalStatus'
    enumeration.values.size() == 4

    where:
    name << [
        'approvalstatus',
        'ApprovalStatus'
    ]
  }
}
