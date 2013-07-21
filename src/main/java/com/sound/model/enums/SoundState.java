package com.sound.model.enums;

public enum SoundState {

	PRIVATE(1, "private"),
	PUBLIC(2, "public"),
	DELETE(3, "delete");

	private int statusId;
	private String statusName;

	private SoundState(int statusId, String statusName) {
		this.statusId = statusId;
		this.statusName = statusName;
	}

	public static Integer getStateId(String status)
	{
		for (SoundState state: SoundState.values())
		{
			if (state.getStatusName().equalsIgnoreCase(status))
			{
				return state.getStatusId();
			}
		}

		return null;
	}
	
	public static String getStateName(Integer statusId)
	{
		for (SoundState state: SoundState.values())
		{
			if (state.getStatusId() == statusId)
			{
				return state.getStatusName();
			}
		}

		return null;
	}
	
	public int getStatusId() {
		return statusId;
	}

	public void setStatusId(int statusId) {
		this.statusId = statusId;
	}

	public String getStatusName() {
		return statusName;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}

}
