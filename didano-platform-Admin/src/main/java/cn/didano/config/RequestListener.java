package cn.didano.config;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;

/**
 * 为websocket添加httpsession
 * @author stephen
 * Created on 2016年12月29日 下午6:06:39 
 */
@WebListener
public class RequestListener implements ServletRequestListener {

	public void requestInitialized(ServletRequestEvent sre) {
		// 将所有request请求都携带上httpSession
		((HttpServletRequest) sre.getServletRequest()).getSession();

	}

	public RequestListener() {
		// TODO Auto-generated constructor.... stub
	}

	public void requestDestroyed(ServletRequestEvent arg0) {
		// TODO Auto-generated method stub
	}
}
