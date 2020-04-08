package com.temp.aggregation.kelvinapi.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class UserRole implements Audited {

  @NotBlank(message = 'email_address must not be blank')
  String emailAddress

  @NotNull(message = 'role must not be null')
  Role role
}
