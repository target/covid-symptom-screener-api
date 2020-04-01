package com.temp.aggregation.kelvinapi.domain

import org.hibernate.annotations.GenericGenerator
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotNull
import java.time.Instant

@Entity
@Table(name = 'temperatures')
class Temperature {
    @Id
    @GeneratedValue(generator= 'system-uuid')
    @GenericGenerator(name= 'system-uuid', strategy = 'uuid')
    String id
    @NotNull
    String organizationId
    @NotNull
    float temperature
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
}
