package com.sound.model.enums;

import java.util.ArrayList;
import java.util.List;

public enum UserOccupationType {
  MUSICIAN(1, "Musician/Artist/Producer/Band"), BOOKER(2, "Booker/Promoter/Venue"), LISTENER(3,
      "Fan/Listener"), STUDIO(4, "Studio/Mastering/Agency"), PRESS(5,
      "Press/Blogger/Podcaster/Media"), LABEL(6, "Label/Publisher"), DJ(7, "DJ/Radio Host"), OTHER(
      8, "Other");

  private int typeId;
  private String typeName;

  private UserOccupationType(int typeId, String typeName) {
    this.typeId = typeId;
    this.typeName = typeName;
  }

  public static List<Integer> getTypeIdsByTypes(List<UserOccupationType> types) {
    List<Integer> typeIds = new ArrayList<Integer>();
    for (UserOccupationType type : types) {
      typeIds.add(type.getTypeId());
    }

    return typeIds;
  }

  public static List<UserOccupationType> getTypesByIds(List<Integer> typeIds) {
    List<UserOccupationType> types = new ArrayList<UserOccupationType>();
    for (Integer typeId : typeIds) {
      UserOccupationType.getTypeById(typeId);
    }

    return types;
  }

  public static UserOccupationType getTypeById(Integer typeId) {
    for (UserOccupationType type : UserOccupationType.values()) {
      if (type.getTypeId() == typeId) {
        return type;
      }

    }
    return null;
  }

  public int getTypeId() {
    return typeId;
  }

  public void setTypeId(int typeId) {
    this.typeId = typeId;
  }

  public String getTypeName() {
    return typeName;
  }

  public void setTypeName(String typeName) {
    this.typeName = typeName;
  }
}
