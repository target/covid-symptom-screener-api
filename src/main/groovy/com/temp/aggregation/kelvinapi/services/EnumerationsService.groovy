package com.temp.aggregation.kelvinapi.services

import com.temp.aggregation.kelvinapi.domain.ApprovalStatus
import com.temp.aggregation.kelvinapi.domain.Enumeration
import com.temp.aggregation.kelvinapi.domain.OrganizationSector
import com.temp.aggregation.kelvinapi.domain.Role
import org.springframework.stereotype.Service

@Service
class EnumerationsService {
  static private final List<Class<? extends Enum>> ENUMERATIONS = [
      ApprovalStatus,
      OrganizationSector,
      Role
  ]

  List<Enumeration> listEnumerations() {
    return ENUMERATIONS.collect { Class<? extends Enum> enumClass ->
      return new Enumeration(
          name: enumClass.simpleName,
          values: EnumSet.allOf(enumClass)*.toString()
      )
    }
  }

  Enumeration getEnumeration(String name) {
    Enumeration enumeration = ENUMERATIONS.findResult { Class<? extends Enum> enumClass ->
      if (enumClass.simpleName.equalsIgnoreCase(name)) {
        return new Enumeration(
            name: enumClass.simpleName,
            values: EnumSet.allOf(enumClass)*.toString()
        )
      }
    }

    return enumeration
  }

}
