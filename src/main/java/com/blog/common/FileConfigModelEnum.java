package com.blog.common;

public enum FileConfigModelEnum {
	IMAGE("image", "图片模块");
	private String model;
	private String note;

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	private FileConfigModelEnum(String model, String note) {
		this.model = model;
		this.note = note;
	}
}
