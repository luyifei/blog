package com.blog.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Response {

	public static final Response DEFAULT_ERROR = Response.error(-2, "服务失败");

	private Object data;
	private int code;
	private String msg;

	public Response() {
		super();
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	@Override
	public String toString() {
		return "ResponseV2 [data=" + data + ", code=" + code + ", msg=" + msg + "]";
	}

	public static Response ok() {
		Response res = new Response();
		res.code = 0;
		res.msg = "success";
		return res;
	}

	public static Response ok(Object result) {
		Response res = new Response();
		res.data = result;
		res.code = 0;
		res.msg = "success";
		return res;
	}

	public static Response ok(int code, String msg) {
		Response res = new Response();
		res.code = code;
		res.msg = msg;
		return res;
	}

	public static Response ok(ResponseType responseType) {
		Response res = new Response();
		res.code = responseType.getCode();
		res.msg = responseType.getMsg();
		return res;
	}

	public static Response error(int code, String msg) {
		Response res = new Response();
		res.code = code;
		res.msg = msg;
		return res;
	}

}
