package cn.didano.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 配置信息
 * 
 * @author stephen.wang 2017-03-17
 */
@ConfigurationProperties(prefix = "config")
@Component
public class AppConfigProperties {

	private int status;
	private String no2url;
	private String no2DbUsername;
	private String no2DbPassword;

	private String no3url;
	private String no3DbUsername;
	private String no3DbPassword;

	private String no1qrcodePath;
	private String no2qrcodePath;

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getNo2url() {
		return no2url;
	}

	public void setNo2url(String no2url) {
		this.no2url = no2url;
	}

	public String getNo2DbUsername() {
		return no2DbUsername;
	}

	public void setNo2DbUsername(String no2DbUsername) {
		this.no2DbUsername = no2DbUsername;
	}

	public String getNo2DbPassword() {
		return no2DbPassword;
	}

	public void setNo2DbPassword(String no2DbPassword) {
		this.no2DbPassword = no2DbPassword;
	}

	public String getNo3url() {
		return no3url;
	}

	public void setNo3url(String no3url) {
		this.no3url = no3url;
	}

	public String getNo3DbUsername() {
		return no3DbUsername;
	}

	public void setNo3DbUsername(String no3DbUsername) {
		this.no3DbUsername = no3DbUsername;
	}

	public String getNo3DbPassword() {
		return no3DbPassword;
	}

	public void setNo3DbPassword(String no3DbPassword) {
		this.no3DbPassword = no3DbPassword;
	}

	

	public String getNo1qrcodePath() {
		return no1qrcodePath;
	}

	public void setNo1qrcodePath(String no1qrcodePath) {
		this.no1qrcodePath = no1qrcodePath;
	}

	public String getNo2qrcodePath() {
		return no2qrcodePath;
	}

	public void setNo2qrcodePath(String no2qrcodePath) {
		this.no2qrcodePath = no2qrcodePath;
	}

	/**
	 * 返回生成二维码路径，区分开正式环境和测试环境
	 * 
	 * @return
	 */
	public String getQrcodePath() {
		switch (status) {
		case 1:
			return no1qrcodePath;
		case 2:
		case 3:
		default:
			return no2qrcodePath;
		}
	}
}
