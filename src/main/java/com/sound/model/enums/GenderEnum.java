package com.sound.model.enums;

public enum GenderEnum {

  MALE(true, "man"), FAMELE(false, "woman"), UNSET(null, "unset");

  private Boolean genderValue;
  private String gender;

  private GenderEnum(Boolean genderValue, String gender) {
    this.genderValue = genderValue;
    this.gender = gender;
  }

  public static Boolean getGenderValue(String genderName) {
    for (GenderEnum gender : GenderEnum.values()) {
      if (gender.getGender().equalsIgnoreCase(genderName)) {
        return gender.isGenderValue();
      }
    }

    return true;
  }

  public static String getGenderName(Boolean genderValue) {
    for (GenderEnum gender : GenderEnum.values()) {
      if (gender.isGenderValue() == genderValue) {
        return gender.getGender();
      }
    }

    return null;
  }

  public Boolean isGenderValue() {
    return genderValue;
  }

  public void setGenderValue(Boolean genderValue) {
    this.genderValue = genderValue;
  }

  public String getGender() {
    return gender;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }

}
