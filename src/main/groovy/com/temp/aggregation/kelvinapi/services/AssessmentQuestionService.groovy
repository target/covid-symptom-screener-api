package com.temp.aggregation.kelvinapi.services

import com.temp.aggregation.kelvinapi.domain.AssessmentQuestion
import com.temp.aggregation.kelvinapi.domain.AssessmentQuestionDTO
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

  List<AssessmentQuestionDTO> findByStatuses(List<AssessmentQuestionStatus> statuses) {
    Sort questionSort = Sort.by('sortPriority', 'displayValue')
    List<AssessmentQuestion> assessmentQuestions = repository.findAllByStatusIn(statuses, questionSort)
    return assessmentQuestions.collect {
      AssessmentQuestionDTO dto = new AssessmentQuestionDTO()
      InvokerHelper.setProperties(dto, it.properties)
      return dto
    }
  }

  AssessmentQuestionDTO create(AssessmentQuestionDTO assessmentQuestionDTO) {
    if (repository.existsByDisplayValue(assessmentQuestionDTO.displayValue)) {
      throw new ServiceException(ServiceError.ASSESSMENT_QUESTION_CONFLICT)
    }
    AssessmentQuestion assessmentQuestion = new AssessmentQuestion()
    InvokerHelper.setProperties(assessmentQuestion, assessmentQuestionDTO.properties)
    AssessmentQuestion saved = repository.save(assessmentQuestion)
    AssessmentQuestionDTO dto = new AssessmentQuestionDTO()
    InvokerHelper.setProperties(dto, saved.properties)
    return dto
  }

  AssessmentQuestionDTO findById(String id) {
    AssessmentQuestion question = repository.findById(id).orElseThrow {
      new ServiceException(ServiceError.NOT_FOUND, 'AssessmentQuestion')
    }
    AssessmentQuestionDTO dto = new AssessmentQuestionDTO()
    InvokerHelper.setProperties(dto, question.properties)
    return dto
  }

  AssessmentQuestionDTO save(String id, AssessmentQuestionDTO assessmentQuestionDTO) {
    AssessmentQuestion assessmentQuestion = repository.findById(id).orElseThrow {
      new ServiceException(ServiceError.NOT_FOUND, 'AssessmentQuestion')
    }
    setUpdatableProperties(assessmentQuestion, assessmentQuestionDTO)
    AssessmentQuestion saved = repository.save(assessmentQuestion)
    AssessmentQuestionDTO dto = new AssessmentQuestionDTO()
    InvokerHelper.setProperties(dto, saved.properties)
    return dto
  }

  AssessmentQuestionDTO disable(String id) {
    AssessmentQuestion existing = repository.findById(id).orElseThrow {
      new ServiceException(ServiceError.NOT_FOUND, 'AssessmentQuestion')
    }
    existing.status = DISABLED
    AssessmentQuestion saved = repository.save(existing)
    AssessmentQuestionDTO dto = new AssessmentQuestionDTO()
    InvokerHelper.setProperties(dto, saved.properties)
    return dto
  }

  private void setUpdatableProperties(AssessmentQuestion question, AssessmentQuestionDTO dto) {
    question.displayValue = dto.displayValue
    question.status = dto.status
    question.sortPriority = dto.sortPriority
  }
}
