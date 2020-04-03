package com.temp.aggregation.kelvinapi.domain

import groovy.transform.EqualsAndHashCode
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.Table
import java.time.Instant

@Entity
@EntityListeners(AuditingEntityListener)
@Table(name = 'user_roles')
@EqualsAndHashCode
class UserRole extends UserRoleUpdate {
  @CreatedDate
  Instant created
  @CreatedBy
  String createdBy
  @LastModifiedDate
  Instant lastModified
  @LastModifiedBy
  String lastModifiedBy
}
