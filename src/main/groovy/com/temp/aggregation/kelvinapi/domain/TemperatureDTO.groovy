package com.temp.aggregation.kelvinapi.domain

import groovy.transform.EqualsAndHashCode
import org.hibernate.annotations.GenericGenerator
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

import javax.persistence.*
import java.time.Instant

@Entity
@Table(
    name = 'temperatures',
    indexes = [
        @Index(name = 'organization_id_index', columnList = 'organizationId', unique = false),
        @Index(name = 'user_id_index', columnList = 'userId', unique = false)
    ]
)
@EqualsAndHashCode(excludes = ['questionAnswers'])
@EntityListeners(AuditingEntityListener)
class TemperatureDTO {
  @Id
  @GeneratedValue(generator = 'system-uuid')
  @GenericGenerator(name = 'system-uuid', strategy = 'uuid')
  String id
  String organizationId
  Float temperature
  String userId
  float latitude
  float longitude
  Instant timestamp = Instant.now()

  @CreatedDate
  Instant created
  @CreatedBy
  String createdBy
  @LastModifiedDate
  Instant lastModified
  @LastModifiedBy
  String lastModifiedBy

  @OneToMany(mappedBy = 'temperature', fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  Set<AssessmentQuestionAnswerDTO> questionAnswers
}
