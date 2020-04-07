package com.temp.aggregation.kelvinapi.services

import com.temp.aggregation.kelvinapi.domain.AssessmentQuestionDTO
import com.temp.aggregation.kelvinapi.domain.AssessmentQuestion
import com.temp.aggregation.kelvinapi.domain.AssessmentQuestionStatus
import com.temp.aggregation.kelvinapi.exceptions.ServiceError
import com.temp.aggregation.kelvinapi.exceptions.ServiceException
import com.temp.aggregation.kelvinapi.repositories.AssessmentQuestionRepository
import org.codehaus.groovy.runtime.InvokerHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

import static com.temp.aggregation.kelvinapi.domain.AssessmentQuestionStatus.DISABLED

@Service
class AssessmentQuestionService {
  @Autowired
  AssessmentQuestionRepository repository

  List<AssessmentQuestion> findByStatuses(List<AssessmentQuestionStatus> statuses) {
    Sort questionSort = Sort.by('sortPriority', 'displayValue')
    List<AssessmentQuestionDTO> assessmentQuestions = repository.findAllByStatusIn(statuses, questionSort)
    return assessmentQuestions.collect {
      AssessmentQuestion dto = new AssessmentQuestion()
      InvokerHelper.setProperties(dto, it.properties)
      return dto
    }
  }

  AssessmentQuestion create(AssessmentQuestion assessmentQuestionDTO) {
    if (repository.existsByDisplayValue(assessmentQuestionDTO.displayValue)) {
      throw new ServiceException(ServiceError.ASSESSMENT_QUESTION_CONFLICT)
    }
    AssessmentQuestionDTO assessmentQuestion = new AssessmentQuestionDTO()
    InvokerHelper.setProperties(assessmentQuestion, assessmentQuestionDTO.properties)
    AssessmentQuestionDTO saved = repository.save(assessmentQuestion)
    AssessmentQuestion dto = new AssessmentQuestion()
    InvokerHelper.setProperties(dto, saved.properties)
    return dto
  }

  AssessmentQuestion findById(String id) {
    AssessmentQuestionDTO question = repository.findById(id).orElseThrow {
      new ServiceException(ServiceError.NOT_FOUND, 'AssessmentQuestion')
    }
    AssessmentQuestion dto = new AssessmentQuestion()
    InvokerHelper.setProperties(dto, question.properties)
    return dto
  }

  AssessmentQuestion save(String id, AssessmentQuestion assessmentQuestionDTO) {
    AssessmentQuestionDTO assessmentQuestion = repository.findById(id).orElseThrow {
      new ServiceException(ServiceError.NOT_FOUND, 'AssessmentQuestion')
    }
    setUpdatableProperties(assessmentQuestion, assessmentQuestionDTO)
    AssessmentQuestionDTO saved = repository.save(assessmentQuestion)
    AssessmentQuestion dto = new AssessmentQuestion()
    InvokerHelper.setProperties(dto, saved.properties)
    return dto
  }

  AssessmentQuestion disable(String id) {
    AssessmentQuestionDTO existing = repository.findById(id).orElseThrow {
      new ServiceException(ServiceError.NOT_FOUND, 'AssessmentQuestion')
    }
    existing.status = DISABLED
    AssessmentQuestionDTO saved = repository.save(existing)
    AssessmentQuestion dto = new AssessmentQuestion()
    InvokerHelper.setProperties(dto, saved.properties)
    return dto
  }

  private void setUpdatableProperties(AssessmentQuestionDTO question, AssessmentQuestion dto) {
    question.displayValue = dto.displayValue
    question.status = dto.status
    question.sortPriority = dto.sortPriority
  }
}
