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
  Page<TemperatureDTO> getTemperaturesFor(String organizationId, Pageable pageable) {
    Page<Temperature> page
    if (organizationId) {
      page = temperatureRepository.findAllByOrganizationId(organizationId, pageable)
    } else {
      page = temperatureRepository.findAll(pageable)
    }
    List<TemperatureDTO> dtos = page.content.collect { temperature ->
      TemperatureDTO dto = buildDTOFrom(temperature)
      return dto
    }
    return new PageImpl<TemperatureDTO>(dtos, page.pageable, page.totalElements)
  }

  @Transactional
  List<TemperatureDTO> saveAll(List<TemperatureDTO> temperatureDTOs, String organizationAuthCode) {
    OrganizationDTO organizationDTO = organizationService.getApprovedOrganizationByAuthCode(organizationAuthCode)
    if (!organizationDTO) {
      throw new ServiceException(ServiceError.ORGANIZATION_NOT_APPROVED)
    }
    List<Temperature> temperatures = temperatureDTOs.collect { temperatureDTO ->
      Temperature temperature = new Temperature()
      InvokerHelper.setProperties(temperature, temperatureDTO.properties)
      temperature.organizationId = organizationDTO.id
      populateQuestionAnswersFromDTO(temperatureDTO, temperature)
      return temperature
    }
    List<Temperature> saved = temperatureRepository.saveAll(
        temperatures
    )
    return saved.collect { buildDTOFrom(it) }
  }

  @Transactional
  TemperatureDTO findById(String temperatureId) {
    Temperature temperature = temperatureRepository.findById(temperatureId)
        .orElseThrow { new ServiceException(ServiceError.NOT_FOUND) }
    return buildDTOFrom(temperature)
  }

  @Transactional
  void deleteById(String temperatureId) {
    temperatureRepository.deleteById(temperatureId)
  }

  private Temperature populateQuestionAnswersFromDTO(TemperatureDTO temperatureDTO, Temperature temperature) {
    List<AssessmentQuestionAnswer> answers = temperatureDTO.questionAnswers.collect { questionAnswer ->
      AssessmentQuestionDTO questionDTO = questionService.findById(questionAnswer.question.id)
      AssessmentQuestion question = new AssessmentQuestion()
      InvokerHelper.setProperties(question, questionDTO.properties)
      return new AssessmentQuestionAnswer(
          temperature: temperature,
          question: question,
          answer: questionAnswer.answer
      )
    }
    temperature.questionAnswers = answers.toSet()
    return temperature
  }

  private TemperatureDTO buildDTOFrom(Temperature temperature) {
    OrganizationDTO organizationDTO = organizationService.getOrganization(temperature.organizationId)
    if (!organizationDTO || organizationDTO.approvalStatus != APPROVED) {
      throw new ServiceException(ServiceError.ORGANIZATION_NOT_APPROVED)
    }
    return new TemperatureDTO(
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
          AssessmentQuestionDTO questionDTO = new AssessmentQuestionDTO()
          InvokerHelper.setProperties(questionDTO, questionAnswer.question.properties)
          new AssessmentQuestionAnswerDTO(
              question: questionDTO,
              answer: questionAnswer.answer
          )
        }.toSet()
    )
  }
}
