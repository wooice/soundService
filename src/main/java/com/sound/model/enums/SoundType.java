package com.sound.model.enums;

public enum SoundType {

	SOUND(1, "sound"),
	SET(2, "set");
	
	private int typeId;
	private String typeName;

	private SoundType(int typeId, String typeName) {
		this.typeId = typeId;
		this.typeName = typeName;
	}

	public static Integer getTypeId(String typeName)
	{
		for (SoundType type: SoundType.values())
		{
			if (type.getTypeName().equalsIgnoreCase(typeName))
			{
				return type.getTypeId();
			}
		}

		return null;
	}

	public static String getTypeName(Integer typeId)
	{
		for (SoundType type: SoundType.values())
		{
			if (type.getTypeId() == typeId)
			{
				return type.getTypeName();
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
