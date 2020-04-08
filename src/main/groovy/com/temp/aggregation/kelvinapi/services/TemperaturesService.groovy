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
      InvokerHelper.setProperties(temperatureDTO, temperatureDTO.properties)
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

  private Temperature buildTemperatureFrom(TemperatureDTO temperatureDTO) {
    Organization organization = organizationService.getOrganization(temperatureDTO.organizationId)
    if (!organization || organization.approvalStatus != APPROVED) {
      throw new ServiceException(ServiceError.ORGANIZATION_NOT_APPROVED)
    }
    return new Temperature(
        id: temperatureDTO.id,
        organizationId: temperatureDTO.organizationId,
        organizationName: organization.orgName,
        temperature: temperatureDTO.temperature,
        userId: temperatureDTO.userId,
        latitude: temperatureDTO.latitude,
        longitude: temperatureDTO.longitude,
        timestamp: temperatureDTO.timestamp,
        created: temperatureDTO.created,
        createdBy: temperatureDTO.createdBy,
        lastModified: temperatureDTO.lastModified,
        lastModifiedBy: temperatureDTO.lastModifiedBy,
        questionAnswers: temperatureDTO.questionAnswers.collect { questionAnswer ->
          AssessmentQuestion question = new AssessmentQuestion()
          InvokerHelper.setProperties(question, questionAnswer.question.properties)
          new AssessmentQuestionAnswer(
              question: question,
              answer: questionAnswer.answer
          )
        }.toSet()
    )
  }
}
