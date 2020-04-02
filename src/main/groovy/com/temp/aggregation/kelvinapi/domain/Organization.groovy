package com.temp.aggregation.kelvinapi.domain

import org.hibernate.annotations.GenericGenerator
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import java.time.Instant

@Entity
@Table(name = 'organizations')
@EntityListeners(AuditingEntityListener)
class Organization extends OrganizationUpdate {
  @Id
  @GeneratedValue(generator='system-uuid')
  @GenericGenerator(name='system-uuid', strategy = 'uuid')
  String id
  String authorizationCode

  @CreatedDate
  Instant created
  @CreatedBy
  String createdBy
  @LastModifiedDate
  Instant lastModified
  @LastModifiedBy
  String lastModifiedBy
}
