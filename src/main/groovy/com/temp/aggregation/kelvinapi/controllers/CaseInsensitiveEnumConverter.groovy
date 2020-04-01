package com.temp.aggregation.kelvinapi.controllers

import java.beans.PropertyEditorSupport

class CaseInsensitiveEnumConverter extends PropertyEditorSupport {

  Class<?> enumClass

  CaseInsensitiveEnumConverter(Class<?> enumClass) {
    this.enumClass = enumClass
  }

  @Override
  @SuppressWarnings('UnnecessarySetter')
  void setAsText(String text) throws IllegalArgumentException {
    String capitalized = text.toUpperCase()
    Object enumValue = Enum.valueOf(enumClass, capitalized)
    setValue(enumValue)
  }
}
