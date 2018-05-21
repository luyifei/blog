package com.blog.common;

public enum ResponseType {
	NON_EXISTENT_ORDER(10001, "订单不存在"), SIGN_ERROR(10002, "签名错误"), SYSTEM_ERROR(10003, "系统异常");

	private Integer code;
	private String msg;

	private ResponseType(Integer code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

}
