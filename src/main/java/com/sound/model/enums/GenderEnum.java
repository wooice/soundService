package com.sound.model.enums;

public enum GenderEnum {

	MALE(true, "man"),
	FAMELE(false, "woman");

	private boolean genderValue;
	private String gender;

	private GenderEnum(boolean genderValue, String gender) {
		this.genderValue = genderValue;
		this.gender = gender;
	}

	public static boolean getGenderValue(String genderName)
	{
		for(GenderEnum gender: GenderEnum.values())
		{
			if(gender.getGender().equalsIgnoreCase(genderName))
			{
				return gender.isGenderValue();
			}
		}

		return true;
	}

	public static String getGenderName(boolean genderValue)
	{
		for(GenderEnum gender: GenderEnum.values())
		{
			if(gender.isGenderValue() == genderValue)
			{
				return gender.getGender();
			}
		}

		return null;
	}
	
	public boolean isGenderValue() {
		return genderValue;
	}

	public void setGenderValue(boolean genderValue) {
		this.genderValue = genderValue;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

}
