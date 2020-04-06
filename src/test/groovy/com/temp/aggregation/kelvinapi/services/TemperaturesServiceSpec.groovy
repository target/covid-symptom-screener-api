package com.temp.aggregation.kelvinapi.services

import com.temp.aggregation.kelvinapi.domain.Temperature
import com.temp.aggregation.kelvinapi.repositories.TemperatureRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import spock.lang.Specification

class TemperaturesServiceSpec extends Specification {
  TemperaturesService service = new TemperaturesService(
      temperatureRepository: Mock(TemperatureRepository)
  )

  void 'get temperatures by org id'() {
    setup:
    String orgId = 'o1'
    Pageable pageable = PageRequest.of(0, 20)
    Page<Temperature> expected = new PageImpl<>([])

    when:
    Page<Temperature> results = service.getTemperaturesFor(orgId, pageable)

    then:
    1 * service.temperatureRepository.findAllByOrganizationId(orgId, pageable) >> expected
    0 * _
    results == expected
  }

  void 'get temperatures'() {
    setup:
    Pageable pageable = PageRequest.of(0, 20)
    Page<Temperature> expected = new PageImpl<>([])

    when:
    Page<Temperature> results = service.getTemperaturesFor(null, pageable)

    then:
    1 * service.temperatureRepository.findAll(pageable) >> expected
    0 * _
    results == expected
  }
}
