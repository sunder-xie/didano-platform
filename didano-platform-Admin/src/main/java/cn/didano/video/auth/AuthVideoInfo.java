package cn.didano.video.auth;


import cn.didano.video.auth.ws.VideoWebsocket;

public class AuthVideoInfo {
	private String openId;
	private int studentId;
	private int channelId;
	private boolean isopen;
	private VideoWebsocket websocket;
	private String sessionId;

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public int getStudentId() {
		return studentId;
	}

	public void setStudentId(int studentId) {
		this.studentId = studentId;
	}

	public boolean isIsopen() {
		return isopen;
	}

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openid) {
		this.openId = openid;
	}

	public int getChannelId() {
		return channelId;
	}

	public void setChannelId(int channelId) {
		this.channelId = channelId;
	}

	public void setIsopen(boolean isopen) {
		this.isopen = isopen;
	}

	public VideoWebsocket getWebsocket() {
		return websocket;
	}

	public void setWebsocket(VideoWebsocket websocket) {
		this.websocket = websocket;
	}

}
