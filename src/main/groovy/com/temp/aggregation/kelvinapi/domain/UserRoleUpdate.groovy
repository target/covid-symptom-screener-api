package com.temp.aggregation.kelvinapi.domain

import javax.persistence.Id
import javax.persistence.MappedSuperclass

@MappedSuperclass
class UserRoleUpdate {
  @Id
  String emailAddress
  Role role
}
