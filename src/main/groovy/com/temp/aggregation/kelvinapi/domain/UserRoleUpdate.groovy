package com.temp.aggregation.kelvinapi.domain

import javax.persistence.Id
import javax.persistence.MappedSuperclass
import javax.validation.constraints.NotNull

@MappedSuperclass
class UserRoleUpdate {
  @Id
  String emailAddress
  @NotNull(message = 'role must not be null')
  Role role
}
