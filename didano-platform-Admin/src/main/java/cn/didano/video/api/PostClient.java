package cn.didano.video.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import cn.didano.base.constant.BackType;
import cn.didano.base.constant.DeletedType;
import cn.didano.base.constant.StaffType;
import cn.didano.base.json.In_Channel_Status;
import cn.didano.base.json.In_Channel_Status_All;
import cn.didano.base.json.Out;
import cn.didano.base.json.OutList;
import cn.didano.base.json.Out_View_staff_channel;
import cn.didano.base.json.Out_View_student_channel;
import cn.didano.base.model.ChannelStatus;
import cn.didano.base.model.Hand_staff4PhoneBook;
import cn.didano.base.model.Tb_staff;
import cn.didano.base.model.Tb_staffData;
import cn.didano.base.model.Vd_auth_time_control;
import cn.didano.base.model.Vd_channel;
import cn.didano.base.model.Vd_channelExample;
import cn.didano.base.model.View_staff_channel;
import cn.didano.base.model.View_student_channel;
import cn.didano.base.service.AuthTimeControlService;
import cn.didano.base.service.ChannelService;
import cn.didano.base.service.MailListService;
import cn.didano.base.service.StaffService;
import cn.didano.base.service.ViewChannelService;
import cn.didano.base.util.TimeUtil;
import cn.didano.video.service.WebsocketService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * HTML5客户端POST服务,此服务和内部rest post有重复，在于，对外网将来有统计、日志的、网关处理
 * 
 * @author stephen Created on 2016年12月17日 下午6:38:30
 */
@Api(value = "HTML5客户端POST服务", tags = "HTML5客户端POST服务")
@RestController
@RequestMapping(value = "/video/client/post/")
public class PostClient {
	static Logger logger = Logger.getLogger(PostClient.class);
	@Autowired
	private ChannelService channelService;
	@Autowired
	private StaffService staffService;
	@Autowired
	private ViewChannelService viewChannelService;
	@Autowired
	private AuthTimeControlService controlService;
	@Autowired
	private WebsocketService websocketService;
	
	@Autowired
	private MailListService mailListService;
	
	
	
	@PostMapping(value = "channel_get_by_student/{studentId}")
	@ApiOperation(value="按学生获取视频频道集合", notes = "按学生获取视频频道集合")
	@ResponseBody
	public Out<OutList<Out_View_student_channel>> channel_get_by_student(@ApiParam(value = "学生ID", required = true) @PathVariable("studentId") int studentId) {
		logger.info("访问  PostClient:channel_get_by_student studentId=" + studentId);
		Out<OutList<Out_View_student_channel>> out = new Out<OutList<Out_View_student_channel>>();
		List<View_student_channel> lists = viewChannelService.selectVideosByStudent(studentId);
		List<Out_View_student_channel> mylists = new ArrayList<Out_View_student_channel>();
		try {
			// channel 循环
			for (View_student_channel view_student_channel : lists) {
				Out_View_student_channel out_View_student_channel = new Out_View_student_channel();
				BeanUtils.copyProperties(out_View_student_channel, view_student_channel);
				int channelId = view_student_channel.getChannelId();
				List<Vd_auth_time_control> few = controlService.selectByChannelId(channelId);
				boolean isOn = false;
				boolean isOneTrue = false;
				// 循环判断当前时间是不是处在某个开放时间段中
				for (Vd_auth_time_control vd_auth_time_control : few) {
					if (!isOn) {// 如果还不true,判断下一个
						isOneTrue = TimeUtil.inRange(vd_auth_time_control.getStart(), vd_auth_time_control.getEnd());
						if (isOneTrue) {
							isOn = true;
						}
					}
					out_View_student_channel.getStart_ends()
							.add(vd_auth_time_control.getStart() + "-" + vd_auth_time_control.getEnd());
				}
				// 设置时间控制状态
				out_View_student_channel.setOn(isOn);
				mylists.add(out_View_student_channel);
			}
			OutList<Out_View_student_channel> outlist = new OutList<Out_View_student_channel>(lists.size(), mylists);
			out.setBackTypeWithLog(outlist, BackType.SUCCESS);
		} catch (Exception e) {
			logger.error(e.getMessage());
			out.setBackTypeWithLog(BackType.FAIL_SEARCH_NORMAL, e.getMessage());
		}
		return out;
	}

	@PostMapping(value = "channel_get_by_staff/{staffId}")
	@ApiOperation(value="按老师获取视频频道集合", notes = "按老师获取视频频道集合")
	@ResponseBody
	public Out<OutList<Out_View_staff_channel>> channel_get_by_staff(@ApiParam(value = "老师ID", required = true)@PathVariable("staffId") int staffId) {
		logger.info("访问  PostClient:channel_get_by_staff staffId="+staffId);
		Out<OutList<Out_View_staff_channel>> back = new Out<OutList<Out_View_staff_channel>>();
		Tb_staff staff4SchoolId = staffService.findById(staffId);
		List<Out_View_staff_channel> mylists = new ArrayList<Out_View_staff_channel>();
		try{
			//判断是不是园长进行登录
			if (staff4SchoolId.getType() == StaffType.SCHOOLMASTER.getIndex()) {
				//channel 循环
				List<View_staff_channel> lists = viewChannelService.selectVideosByStaff(staffId);
				for (View_staff_channel view_staff_channel : lists) {
					Out_View_staff_channel out_View_staff_channel = new Out_View_staff_channel();
					BeanUtils.copyProperties(out_View_staff_channel, view_staff_channel);
					int channelId = view_staff_channel.getChannelId();
					List<Vd_auth_time_control> few= controlService.selectByChannelId(channelId);
					boolean isOn = false;
					boolean isOneTrue = false;
					//循环判断当前时间是不是处在某个开放时间段中
					for (Vd_auth_time_control vd_auth_time_control : few) {
						if(!isOn){//如果还不true,判断下一个
							isOneTrue = TimeUtil.inRange(vd_auth_time_control.getStart(),vd_auth_time_control.getEnd()); 
							if(isOneTrue){
								isOn = true;
							}
						} 
						out_View_staff_channel.getStart_ends().add(vd_auth_time_control.getStart()+"-"+vd_auth_time_control.getEnd());
					}
						//设置时间控制状态
						out_View_staff_channel.setOn(isOn);
						mylists.add(out_View_staff_channel);
					}
					OutList<Out_View_staff_channel> outlist = new OutList<Out_View_staff_channel>(mylists.size(),mylists);
					back.setBackTypeWithLog(outlist, BackType.SUCCESS);
				}else{
					// 为了得到班级id
					Hand_staff4PhoneBook staff4ClassId = mailListService.findbystaffbyid(staffId);
					System.err.println(staff4ClassId.getClassId());
					//channel 循环
					Tb_staffData t=new Tb_staffData();
					t.setClassId(staff4ClassId.getClassId());
					t.setId(staffId);
					t.setSchoolId(staff4ClassId.getSchoolId());
					List<View_staff_channel> lists = viewChannelService.selectAllView_channel_info_staff(t);
					for (View_staff_channel view_staff_channel : lists) {
						Out_View_staff_channel out_View_staff_channel = new Out_View_staff_channel();
						BeanUtils.copyProperties(out_View_staff_channel, view_staff_channel);
						int channelId = view_staff_channel.getChannelId();
						List<Vd_auth_time_control> few= controlService.selectByChannelId(channelId);
						boolean isOn = false;
						boolean isOneTrue = false;
						//循环判断当前时间是不是处在某个开放时间段中
						for (Vd_auth_time_control vd_auth_time_control : few) {
							if(!isOn){//如果还不true,判断下一个
								isOneTrue = TimeUtil.inRange(vd_auth_time_control.getStart(),vd_auth_time_control.getEnd()); 
								if(isOneTrue){
									isOn = true;
								}
							}
							out_View_staff_channel.getStart_ends().add(vd_auth_time_control.getStart()+"-"+vd_auth_time_control.getEnd());
						}
						//设置时间控制状态
						out_View_staff_channel.setOn(isOn);
						mylists.add(out_View_staff_channel);
					}
					OutList<Out_View_staff_channel> outlist = new OutList<Out_View_staff_channel>(mylists.size(),mylists);
					back.setBackTypeWithLog(outlist, BackType.SUCCESS);
				}
			}catch(Exception e){
			logger.error(e.getMessage());
			back.setBackTypeWithLog(BackType.FAIL_SEARCH_NORMAL,e.getMessage());
		}
		return back;
	}
	
	@ApiOperation(value="设置频道开关", notes = "设置频道开关，提供给园长或者老师")
	@PostMapping(value = "channel_set_status")
	@ResponseBody
	public Out<String> channel_set_status(@RequestBody In_Channel_Status in_ChannelStatus) {
		logger.info("访问  PostClient:channel_set_status,in_ChannelStatus="+in_ChannelStatus);
		Out<String> back = null;
		ChannelStatus channelStatus = new ChannelStatus();
		try {
			BeanUtils.copyProperties(channelStatus, in_ChannelStatus);
			websocketService.OperateVideo(channelStatus);
			// TODO 没有区分是否改变，只要设置开关，一律通知客户端
			channelService.updateByChannelStatus(channelStatus);
			logger.info(BackType.SUCCESS_OPER_SWITCH.getMessage() + ":教职工ID[" + in_ChannelStatus.getStaffId() + "]设置频道状态"
					+ in_ChannelStatus.getStatus());
			back = new Out<String>(BackType.SUCCESS_OPER_SWITCH);
		} catch (Exception e) {
			logger.error(BackType.FAIL_OPER_SWITCH.getMessage() + ":教职工ID[" + in_ChannelStatus.getStaffId() + "]设置频道状态"
					+ in_ChannelStatus.getStatus() + ":" + e.getMessage());
			back = new Out<String>(BackType.FAIL_OPER_SWITCH);
		}
		return back;
	}
	
	@ApiOperation(value="设置本校所有频道开关", notes = "设置本校所有频道开关，提供给园长")
	@PostMapping(value = "channel_set_status_all")
	@ResponseBody
	public Out<String> channel_set_status_all(@RequestBody In_Channel_Status_All in_Channel_Status_All) {
		logger.info("访问  PostClient:channel_set_status_all,in_Channel_Status_All="+in_Channel_Status_All);
		Out<String> back = new Out<String>();;
		ChannelStatus channelStatus = null;
		try {
			//找出当前老师所处学校
			int staffId = in_Channel_Status_All.getStaffId();
			int schoolId =  staffService.selectByPrimaryKey(staffId).getSchoolId();
			//找出该校下的所有channelId
			List<Vd_channel> list =  channelService.selectAllBySchoolId(schoolId);
			
			if(list.size()>0){
				Vd_channel channel = new Vd_channel();
				channel.setStatus(in_Channel_Status_All.getStatus());
				Vd_channelExample example = new Vd_channelExample();
				Vd_channelExample.Criteria cri = example.createCriteria();
				//更新条件 当前学校，而且未删除的
				cri.andSchoolIdEqualTo(schoolId);
				cri.andDeletedEqualTo(DeletedType.N0_DELETED.getValue());
				//更新设置
				int rowNum = channelService.updateByExampleSelective(channel,example);
				logger.info("channel_set_status_all:更新设置：rowNum"+rowNum);
				//循环关闭每个频道
				for(int i=0;i<list.size();i++){
					channelStatus = new ChannelStatus();
					BeanUtils.copyProperties(channelStatus, in_Channel_Status_All);
					channelStatus.setChannelId(list.get(i).getId());
					websocketService.OperateVideo(channelStatus);
					logger.info("channel_set_status_all:操作channel=["+list.get(i).getId()+"]视频开关");
				}
				back.setBackTypeWithLog(BackType.SUCCESS);
			}else{
				back.setBackTypeWithLog(BackType.FAIL_DELETE_NORMAL);
			}
		} catch (Exception e) {
			logger.error(BackType.FAIL_OPER_SWITCH.getMessage() + ":教职工ID[" + in_Channel_Status_All.getStaffId() + "]设置频道状态"
					+ in_Channel_Status_All.getStatus() + ":" + e.getMessage());
			back.setBackTypeWithLog(BackType.FAIL_OPER_SWITCH,e.getMessage());
		}
		return back;
	}
}
