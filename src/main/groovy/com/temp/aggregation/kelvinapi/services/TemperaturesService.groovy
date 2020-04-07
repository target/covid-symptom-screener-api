package com.temp.aggregation.kelvinapi.services

import com.temp.aggregation.kelvinapi.domain.*
import com.temp.aggregation.kelvinapi.exceptions.ServiceError
import com.temp.aggregation.kelvinapi.exceptions.ServiceException
import com.temp.aggregation.kelvinapi.repositories.AssessmentQuestionAnswerRepository
import com.temp.aggregation.kelvinapi.repositories.TemperatureRepository
import org.codehaus.groovy.runtime.InvokerHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import javax.persistence.EntityManager

import static com.temp.aggregation.kelvinapi.domain.ApprovalStatus.APPROVED

@Service
class TemperaturesService {

  @Autowired
  TemperatureRepository temperatureRepository

  @Autowired
  OrganizationsService organizationService

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
    List<Temperature> dtos = page.content.collect { temperature ->
      Temperature dto = buildDTOFrom(temperature)
      return dto
    }
    return new PageImpl<Temperature>(dtos, page.pageable, page.totalElements)
  }

  @Transactional
  List<Temperature> saveAll(List<Temperature> temperatureDTOs, String organizationAuthCode) {
    Organization organizationDTO = organizationService.getApprovedOrganizationByAuthCode(organizationAuthCode)
    if (!organizationDTO) {
      throw new ServiceException(ServiceError.ORGANIZATION_NOT_APPROVED)
    }
    List<TemperatureDTO> temperatures = temperatureDTOs.collect { temperatureDTO ->
      TemperatureDTO temperature = new TemperatureDTO()
      InvokerHelper.setProperties(temperature, temperatureDTO.properties)
      temperature.organizationId = organizationDTO.id
      populateQuestionAnswersFromDTO(temperatureDTO, temperature)
      return temperature
    }
    List<TemperatureDTO> saved = temperatureRepository.saveAll(
        temperatures
    )
    return saved.collect { buildDTOFrom(it) }
  }

  @Transactional
  Temperature findById(String temperatureId) {
    TemperatureDTO temperature = temperatureRepository.findById(temperatureId)
        .orElseThrow { new ServiceException(ServiceError.NOT_FOUND) }
    return buildDTOFrom(temperature)
  }

  @Transactional
  void deleteById(String temperatureId) {
    temperatureRepository.deleteById(temperatureId)
  }

  private TemperatureDTO populateQuestionAnswersFromDTO(Temperature temperatureDTO, TemperatureDTO temperature) {
    List<AssessmentQuestionAnswerDTO> answers = temperatureDTO.questionAnswers.collect { questionAnswer ->
      AssessmentQuestion questionDTO = questionService.findById(questionAnswer.question.id)
      AssessmentQuestionDTO question = new AssessmentQuestionDTO()
      InvokerHelper.setProperties(question, questionDTO.properties)
      return new AssessmentQuestionAnswerDTO(
          temperature: temperature,
          question: question,
          answer: questionAnswer.answer
      )
    }
    temperature.questionAnswers = answers.toSet()
    return temperature
  }

  private Temperature buildDTOFrom(TemperatureDTO temperature) {
    Organization organizationDTO = organizationService.getOrganization(temperature.organizationId)
    if (!organizationDTO || organizationDTO.approvalStatus != APPROVED) {
      throw new ServiceException(ServiceError.ORGANIZATION_NOT_APPROVED)
    }
    return new Temperature(
        id: temperature.id,
        organizationId: temperature.organizationId,
        organizationName: organizationDTO.orgName,
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
