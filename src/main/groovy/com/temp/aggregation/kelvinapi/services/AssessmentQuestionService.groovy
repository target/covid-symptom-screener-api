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
      AssessmentQuestion question = new AssessmentQuestion()
      InvokerHelper.setProperties(question, it.properties)
      return question
    }
  }

  AssessmentQuestion create(AssessmentQuestion question) {
    if (repository.existsByDisplayValue(question.displayValue)) {
      throw new ServiceException(ServiceError.ASSESSMENT_QUESTION_CONFLICT)
    }
    AssessmentQuestionDTO assessmentQuestion = new AssessmentQuestionDTO()
    InvokerHelper.setProperties(assessmentQuestion, question.properties)
    AssessmentQuestionDTO saved = repository.save(assessmentQuestion)
    AssessmentQuestion created = new AssessmentQuestion()
    InvokerHelper.setProperties(created, saved.properties)
    return created
  }

  AssessmentQuestion findById(String id) {
    AssessmentQuestionDTO questionDTO = repository.findById(id).orElseThrow {
      new ServiceException(ServiceError.NOT_FOUND, 'AssessmentQuestion')
    }
    AssessmentQuestion question = new AssessmentQuestion()
    InvokerHelper.setProperties(question, questionDTO.properties)
    return question
  }

  AssessmentQuestion save(String id, AssessmentQuestion question) {
    AssessmentQuestionDTO questionDTO = repository.findById(id).orElseThrow {
      new ServiceException(ServiceError.NOT_FOUND, 'AssessmentQuestion')
    }
    setUpdatableProperties(questionDTO, question)
    AssessmentQuestionDTO saved = repository.save(questionDTO)
    AssessmentQuestion updated = new AssessmentQuestion()
    InvokerHelper.setProperties(updated, saved.properties)
    return updated
  }

  AssessmentQuestion disable(String id) {
    AssessmentQuestionDTO existing = repository.findById(id).orElseThrow {
      new ServiceException(ServiceError.NOT_FOUND, 'AssessmentQuestion')
    }
    existing.status = DISABLED
    AssessmentQuestionDTO saved = repository.save(existing)
    AssessmentQuestion question = new AssessmentQuestion()
    InvokerHelper.setProperties(question, saved.properties)
    return question
  }

  private void setUpdatableProperties(AssessmentQuestionDTO questionDTO, AssessmentQuestion question) {
    questionDTO.displayValue = question.displayValue
    questionDTO.status = question.status
    questionDTO.sortPriority = question.sortPriority
  }
}
