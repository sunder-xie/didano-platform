package cn.didano.video.service;

import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.didano.base.constant.BackType;
import cn.didano.base.exception.ServiceException;
import cn.didano.base.json.Out;
import cn.didano.base.model.ChannelStatus;
import cn.didano.base.service.AuthTimeControlService;
import cn.didano.video.auth.AuthVideoInfo;
import cn.didano.video.auth.ws.VideoWebsocketChannel;

/**
 * 
 * WebsocketPool > WebsocketChannel 按频道；WebsocketPool
 * 
 * @author stephen Created on 2016年12月23日 下午6:24:47
 */
@Service
public class WebsocketService {
	static Logger logger = Logger.getLogger(WebsocketService.class);
	@Autowired
	private AuthTimeControlService controlService;
	
	// concurrent包的线程安全Set，用来存放每个频道的websocket的·链接
	private static ConcurrentHashMap<Integer, VideoWebsocketChannel> WebsocketChannelMap = new ConcurrentHashMap<Integer, VideoWebsocketChannel>();

	/**
	 * 删除频道
	 * 
	 * @param channelId
	 */
	public static void removeChannel(int channelId) {
		VideoWebsocketChannel tmp = WebsocketChannelMap.get(channelId);
		WebsocketChannelMap.remove(channelId);
		tmp = null;
		// TODO 定时清理
	}

	/**
	 * 增加频道
	 * 
	 * @param websocketChannel
	 */
	public static synchronized void addChannel(int channelId) {
		VideoWebsocketChannel websocketChannel = new VideoWebsocketChannel(channelId);
		WebsocketChannelMap.put(channelId, websocketChannel);
	}

	public static ConcurrentHashMap<Integer, VideoWebsocketChannel> getWebsocketChannelMap() {
		return WebsocketChannelMap;
	}

	/**
	 * 后台操作视频开关 比如一个频道可能有100人观看，则这个频道当前有100视频通道，服务器，要操作100个通道websocket
	 * 
	 * @param studentId
	 * @param open
	 */
	public void OperateVideo(ChannelStatus channelStatus) throws ServiceException {
		logger.debug("WebsocketManager readey oper channel[" + channelStatus.getChannelId() + "] Status:"
				+ channelStatus.getStatus());
		String data = null;
		if (ifChannelexitBychannelId(channelStatus.getChannelId())) {
			try {
				boolean open = controlService.checkIsControByRemote(channelStatus.getChannelId());
				channelStatus.setOpen(open);
				// 获取当前频道相关的视频通道，比如一个频道可能有100人观看，则这个频道当前有100视频通道，服务器，要操作100个通道websocket
				Enumeration<AuthVideoInfo> en = WebsocketService.getWebsocketChannelMap()
						.get(channelStatus.getChannelId()).getWayMap().elements();
				Out<ChannelStatus> back = new Out<ChannelStatus>();
				// 向客户端传递打开信息
				while (en.hasMoreElements()) {
					AuthVideoInfo tmp = en.nextElement();
					back.setBackTypeWithLog(channelStatus, BackType.SUCCESS_WEBSOCKET_SEND);
					// 采用fasterxml将对象转换为json对象
					// Convert object to JSON string
					ObjectMapper mapper = new ObjectMapper();
					data = mapper.writeValueAsString(back);
					logger.debug("OperateVideo : send client data=" + data);
					tmp.getWebsocket().sendMessage(data);
					logger.debug("sessionId=" + tmp.getSessionId() + "openid=" + tmp.getOpenId());
				}
			} catch (ServiceException e) {
				logger.error(e.getMessage());
			} catch (JsonProcessingException js) {
				logger.error(js.getMessage());
			}
		}
	}

	/**
	 * 某条视频频道是否存在 频道 一个摄像头一个频道 通道 每个看的人能建立一个websocket通道
	 * 
	 * @param channelId
	 * @return
	 */
	private static boolean ifChannelexitBychannelId(int channelId) {
		boolean ifexit = WebsocketService.getWebsocketChannelMap().get(channelId) != null ? true : false;
		return ifexit;
	}
}
