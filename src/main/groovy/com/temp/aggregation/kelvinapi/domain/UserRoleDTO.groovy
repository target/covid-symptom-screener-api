package com.temp.aggregation.kelvinapi.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

import javax.validation.constraints.NotNull

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class UserRoleDTO implements AuditedDTO {

  @NotNull(message = 'email_address must not be null')
  String emailAddress

  @NotNull(message = 'role must not be null')
  Role role
}
