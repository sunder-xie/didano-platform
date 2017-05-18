package cn.didano.video.exception;

import cn.didano.base.exception.ExceptionEnums;

/**
 * 接口反馈信息
 * 反馈信息是一个体系，包括
 * 1，数据库错误反馈信息
 * 2，平台信息
 * 3，接口反馈信息
 * 4，模块反馈信息
 * 
 * @author stephen Created on 2016年12月26日 上午11:24:32
 */
public enum VideoExceptionEnums implements ExceptionEnums {
	FAIL_OSS_CREATE_FOLDER(1151,"创建OSS频道失败"),
	FAIL_PAY_CREATE_ACCOUNT(1152,"创建PAY账户失败"),
	FAIL_WEBSOCKET_CLOSE(351,"失败"),
	FAIL_WEBSOCKET_SEND(352,"发送信息失败");

	public int code;
	public String message;

	private VideoExceptionEnums(int code, String message) {
		this.code = code;
		this.message = message;
	}

	private VideoExceptionEnums(int code, String message, Exception e) {
		this.code = code;
		this.message = message;
	}

	@Override
	public int getCode() {
		return code;
	}

	@Override
	public String getMessage() {
		return message;
	}

}
