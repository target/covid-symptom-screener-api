package com.temp.aggregation.kelvinapi.services

import com.temp.aggregation.kelvinapi.domain.Organization
import com.temp.aggregation.kelvinapi.domain.Temperature
import com.temp.aggregation.kelvinapi.domain.TemperatureUpdate
import com.temp.aggregation.kelvinapi.exceptions.ServiceError
import com.temp.aggregation.kelvinapi.exceptions.ServiceException
import com.temp.aggregation.kelvinapi.repositories.TemperatureRepository
import org.codehaus.groovy.runtime.InvokerHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class TemperaturesService {

  @Autowired
  TemperatureRepository temperatureRepository

  @Autowired
  OrganizationService organizationService

  Page<Temperature> getTemperaturesFor(String organizationId, Pageable pageable) {
    return temperatureRepository.findAllByOrganizationId(organizationId, pageable)
  }

  List<Temperature> saveAll(List<TemperatureUpdate> temperatures, String organizationAuthCode) {
    Organization organization = organizationService.getApprovedOrganizationByAuthCode(organizationAuthCode)
    if (!organization) {
      throw new ServiceException(ServiceError.ORGANIZATION_NOT_APPROVED)
    }
    return temperatureRepository.saveAll(
        temperatures.collect { temperatureUpdate ->
          Temperature temperature = new Temperature()
          InvokerHelper.setProperties(temperature, temperatureUpdate.properties)
          temperature.organizationId = organization.id
          return temperature
        }
    )
  }

  Temperature findById(String temperatureId) {
    return temperatureRepository.findById(temperatureId)
        .orElseThrow { new ServiceException(ServiceError.NOT_FOUND) }
  }

  void deleteById(String temperatureId) {
    temperatureRepository.deleteById(temperatureId)
  }
}
