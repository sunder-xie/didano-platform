/**
 * 示例说明
 * 
 * HelloOSS是OSS Java SDK的示例程序，您可以修改endpoint、accessKeyId、accessKeySecret、bucketName后直接运行。
 * 运行方法请参考README。
 * 
 * 本示例中的并不包括OSS Java SDK的所有功能，详细功能及使用方法，请参看“SDK手册 > Java-SDK”，
 * 链接地址是：https://help.aliyun.com/document_detail/oss/sdk/java-sdk/preface.html?spm=5176.docoss/sdk/java-sdk/。
 * 
 * 调用OSS Java SDK的方法时，抛出异常表示有错误发生；没有抛出异常表示成功执行。
 * 当错误发生时，OSS Java SDK的方法会抛出异常，异常中包括错误码、错误信息，详细请参看“SDK手册 > Java-SDK > 异常处理”，
 * 链接地址是：https://help.aliyun.com/document_detail/oss/sdk/java-sdk/exception.html?spm=5176.docoss/api-reference/error-response。
 * 
 * OSS控制台可以直观的看到您调用OSS Java SDK的结果，OSS控制台地址是：https://oss.console.aliyun.com/index#/。
 * OSS控制台使用方法请参看文档中心的“控制台用户指南”， 指南的来链接地址是：https://help.aliyun.com/document_detail/oss/getting-started/get-started.html?spm=5176.docoss/user_guide。
 * 
 * OSS的文档中心地址是：https://help.aliyun.com/document_detail/oss/user_guide/overview.html。
 * OSS Java SDK的文档地址是：https://help.aliyun.com/document_detail/oss/sdk/java-sdk/install.html?spm=5176.docoss/sdk/java-sdk。
 * 
 */

package cn.didano.video.service;

import static cn.didano.base.util.OssUtil.getChannelName;
import static com.aliyun.oss.internal.OSSUtils.ensureLiveChannelNameValid;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.CreateLiveChannelRequest;
import com.aliyun.oss.model.CreateLiveChannelResult;
import com.aliyun.oss.model.LiveChannelStatus;
import com.aliyun.oss.model.LiveChannelTarget;

import cn.didano.base.exception.ServiceException;
import cn.didano.base.model.Vd_channel;
import cn.didano.base.service.ChannelService;
import cn.didano.video.entity.OssInfo;
import cn.didano.video.exception.VideoExceptionEnums;

/**
 * oss服务，
 * HLS 视频直播技术，看考文档
 * http://www.xuebuyuan.com/2135081.html
 * https://datatracker.ietf.org/doc/draft-pantos-http-live-streaming/
 * https://developer.apple.com/streaming/
 * 
 * @author stephen Created on 2016年12月26日 下午12:39:56
 */
@Service
public class OssManagerService {
	static Logger logger = Logger.getLogger(OssManagerService.class);
	@Autowired
	OssInfo ossInfo;
	@Autowired
	private ChannelService channelService;

	public void startCreateOssChannel(Vd_channel channel) throws ServiceException {
		class CreateOssChannelThread extends Thread {
			@Override
			public void run() {
					createChannel(channel);
			}
			public void createChannel(Vd_channel channel){
				String endpoint = ossInfo.getEndpoint();
				String accessKeyId = ossInfo.getAccessKeyId();
				String accessKeySecret = ossInfo.getAccessKeySecret();
				String bucketName = ossInfo.getBucketname();
				String cdn = ossInfo.getCdn();
				String channelName = getChannelName(channel.getId(), channel.getEnName(), channel.getSchoolId(),channel.getClassId());
				OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
				try {
					//采用阿里的工具检查名称合法性
					ensureLiveChannelNameValid(channelName);
					if (ossClient.doesBucketExist(bucketName)) {
						logger.info("您已经创建Bucket：" + bucketName + "。");
					} else {
						logger.warn("您的Bucket不存在：" + bucketName + "。");
					}
					//参数设定参考https://cloud.baidu.com/doc/LSS/FAQ/30.5C.96.3F.69.E1.28.8D.A6.86.D5.43.4C.88.13.86.9B.BB.html
					// 用来指定持久化为HLS格式时，多长时间切割一个ts分片，单位秒 10 
					// 用来指定持久化为HLS格式时，m3u8文件中最多包含多少个ts分片 5
					// 时间延迟太长，改为24秒，分片8秒，共3片
		            LiveChannelTarget target = new LiveChannelTarget("HLS", 8, 3, "playlist.m3u8");
		            CreateLiveChannelRequest createLiveChannelRequest = new CreateLiveChannelRequest(
		                    bucketName, channelName, channel.getDes(), LiveChannelStatus.Enabled, target);
		            CreateLiveChannelResult result = ossClient.createLiveChannel(createLiveChannelRequest);
		            //没调到异常，同时不为空，说明创建成功
		            if(result!=null){
		            	channel.setOssPushUrl(result.getPublishUrls().get(0));
		            	//http://jiankong.didano.cn/school[4]class[22]channel[69]name[rtert]/playlist.m3u8
		            	//http://testxiaonuo.oss-cn-shanghai.aliyuncs.com/school[4]class[22]channel[69]name[rtert]/playlist.m3u8
		            	logger.info("back play url="+result.getPlayUrls().get(0));
		            	//替换为cdn加速地址
		            	String playurl = cdn +  "/"+ channelName + "/"+ "playlist.m3u8";
						channel.setOssPlayUrl(playurl);
						channelService.updateByPrimaryKeySelective(channel);
		            }
		            logger.info("创建channel成功 channel="+channelName);
				} catch (OSSException oe) {
					oe.printStackTrace();
					logger.error(oe.getErrorMessage());
					throw new ServiceException(VideoExceptionEnums.FAIL_OSS_CREATE_FOLDER);
				} catch (ClientException ce) {
					ce.printStackTrace();
					logger.error(ce.getErrorMessage());
					throw new ServiceException(VideoExceptionEnums.FAIL_OSS_CREATE_FOLDER);
				} catch (Exception e) {
					e.printStackTrace();
					logger.error(e.getMessage());
					throw new ServiceException(VideoExceptionEnums.FAIL_OSS_CREATE_FOLDER);
				} finally {
					ossClient.shutdown();
				}
				logger.info("Oss关闭：createChannel Completed");
			}
		}
		// 创建一个新的线程
		new CreateOssChannelThread().start();
	}

	public OssInfo getOssInfo() {
		return ossInfo;
	}

}
