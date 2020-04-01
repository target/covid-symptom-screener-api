package com.temp.aggregation.kelvinapi.controllers

import spock.lang.Specification

class CaseInsensitiveEnumConverterSpec extends Specification {
  CaseInsensitiveEnumConverter enumCaseConverter = new CaseInsensitiveEnumConverter(EnumCaseConverterEnum)

  def 'setAsText'() {

    when: 'setting as text'
    enumCaseConverter.setAsText(text)

    then: 'an Enum value should be set regardless of casing'
    enumCaseConverter.value == EnumCaseConverterEnum.VALUE

    where:
    text    || _
    'VALUE' || _
    'value' || _
    'VaLue' || _
  }

  private static enum EnumCaseConverterEnum {
    VALUE
  }

}
