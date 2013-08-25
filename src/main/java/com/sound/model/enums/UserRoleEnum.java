package com.sound.model.enums;

import com.sound.constant.Constant;

public enum UserRoleEnum {

  GUEST(0, Constant.GUEST_ROLE), USER(1, Constant.USER_ROLE), ADMIN(5, Constant.ADMIN_ROLE);

  private Integer typeId;

  private String typeName;

  private UserRoleEnum(int typeId, String typeName) {
    this.typeId = typeId;
    this.typeName = typeName;
  }

  public static Integer getTypeId(String typeName) {
    for (UserRoleEnum type : UserRoleEnum.values()) {
      if (type.getTypeName().equalsIgnoreCase(typeName)) {
        return type.getTypeId();
      }
    }

    return null;
  }

  public static String getTypeName(Integer typeId) {
    for (UserRoleEnum type : UserRoleEnum.values()) {
      if (type.getTypeId() == typeId) {
        return type.getTypeName();
      }
    }

    return null;
  }

  public Integer getTypeId() {
    return typeId;
  }

  public void setTypeId(Integer typeId) {
    this.typeId = typeId;
  }

  public String getTypeName() {
    return typeName;
  }

  public void setTypeName(String typeName) {
    this.typeName = typeName;
  }

}
