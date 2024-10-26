package com.purplechat.enums;

public enum ResponseCodeEnum {
	CODE_200(200, "请求成功"),
	CODE_404(404, "请求地址不存在"),
	CODE_600(600, "请求参数错误"),
	CODE_601(601, "信息已经存在"),
	CODE_602(602, "文件不存在"),
	CODE_500(500, "服务器返回错误，请联系管理员"),
	CODE_505(505, "未监测到心跳，请重新登录"),

	CODE_901(901, "登录超时"),
	CODE_902(902, "您不是对方的好友，请先申请成为好友"),
	CODE_903(903, "您已不在群聊，请先加入群聊");

	private Integer code;
	private String msg;
	ResponseCodeEnum(Integer code, String msg){
		this.code = code;
		this.msg = msg;
	}

	public Integer getCode(){ return code; };
	public String getMsg(){ return msg; };
}
