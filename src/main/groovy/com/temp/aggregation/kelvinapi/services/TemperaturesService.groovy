package com.temp.aggregation.kelvinapi.services

import com.temp.aggregation.kelvinapi.domain.*
import com.temp.aggregation.kelvinapi.exceptions.ServiceError
import com.temp.aggregation.kelvinapi.exceptions.ServiceException
import com.temp.aggregation.kelvinapi.repositories.AssessmentQuestionAnswerRepository
import com.temp.aggregation.kelvinapi.repositories.OrganizationRepository
import com.temp.aggregation.kelvinapi.repositories.TemperatureRepository
import org.codehaus.groovy.runtime.InvokerHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import javax.persistence.EntityManager

@Service
class TemperaturesService {

  @Autowired
  TemperatureRepository temperatureRepository

  @Autowired
  OrganizationsService organizationService

  @Autowired
  OrganizationRepository organizationRepository

  @Autowired
  AssessmentQuestionService questionService

  @Autowired
  AssessmentQuestionAnswerRepository assessmentQuestionAnswerRepository

  @Autowired
  EntityManager entityManager

  @Transactional
  Page<Temperature> getTemperaturesFor(String organizationId, Pageable pageable) {
    Page<TemperatureDTO> page
    if (organizationId) {
      page = temperatureRepository.findAllByOrganizationId(organizationId, pageable)
    } else {
      page = temperatureRepository.findAll(pageable)
    }
    List<Temperature> temperatures = page.content.collect { temperatureDTO ->
      Temperature temperature = buildTemperatureFrom(temperatureDTO)
      return temperature
    }
    return new PageImpl<Temperature>(temperatures, page.pageable, page.totalElements)
  }

  @Transactional
  List<Temperature> saveAll(List<Temperature> toSave, String organizationAuthCode) {
    Organization organization = organizationService.getApprovedOrganizationByAuthCode(organizationAuthCode)
    if (!organization) {
      throw new ServiceException(ServiceError.ORGANIZATION_NOT_APPROVED)
    }
    List<TemperatureDTO> temperatureDTOs = toSave.collect { temperature ->
      TemperatureDTO temperatureDTO = new TemperatureDTO()
      InvokerHelper.setProperties(temperatureDTO, temperature.properties)
      temperatureDTO.organizationId = organization.id
      populateDTOQuestionAnswersFromTemperature(temperature, temperatureDTO)
      return temperatureDTO
    }
    List<TemperatureDTO> saved = temperatureRepository.saveAll(
        temperatureDTOs
    )
    return saved.collect { buildTemperatureFrom(it) }
  }

  @Transactional
  Temperature findById(String temperatureId) {
    TemperatureDTO temperatureDTO = temperatureRepository.findById(temperatureId)
        .orElseThrow { new ServiceException(ServiceError.NOT_FOUND) }
    return buildTemperatureFrom(temperatureDTO)
  }

  @Transactional
  void deleteById(String temperatureId) {
    temperatureRepository.deleteById(temperatureId)
  }

  private TemperatureDTO populateDTOQuestionAnswersFromTemperature(Temperature temperature, TemperatureDTO temperatureDTO) {
    List<AssessmentQuestionAnswerDTO> answers = temperature.questionAnswers.collect { questionAnswer ->
      AssessmentQuestion question = questionService.findById(questionAnswer.question.id)
      AssessmentQuestionDTO questionDTO = new AssessmentQuestionDTO()
      InvokerHelper.setProperties(questionDTO, question.properties)
      return new AssessmentQuestionAnswerDTO(
          temperature: temperatureDTO,
          question: questionDTO,
          answer: questionAnswer.answer
      )
    }
    temperatureDTO.questionAnswers = answers.toSet()
    return temperatureDTO
  }

  private Temperature buildTemperatureFrom(TemperatureDTO temperature) {
    OrganizationDTO organizationDTO = organizationRepository.findById(temperature.organizationId).orElse(null)
    String orgName = organizationDTO?.orgName ?: 'UNKNOWN'
    return new Temperature(
        id: temperature.id,
        organizationId: temperature.organizationId,
        organizationName: orgName,
        temperature: temperature.temperature,
        userId: temperature.userId,
        latitude: temperature.latitude,
        longitude: temperature.longitude,
        timestamp: temperature.timestamp,
        created: temperature.created,
        createdBy: temperature.createdBy,
        lastModified: temperature.lastModified,
        lastModifiedBy: temperature.lastModifiedBy,
        questionAnswers: temperature.questionAnswers.collect { questionAnswer ->
          AssessmentQuestion questionDTO = new AssessmentQuestion()
          InvokerHelper.setProperties(questionDTO, questionAnswer.question.properties)
          new AssessmentQuestionAnswer(
              question: questionDTO,
              answer: questionAnswer.answer
          )
        }.toSet()
    )
  }
}
