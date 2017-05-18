package cn.didano.maillist.controller;

import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageInfo;

import cn.didano.base.constant.BackType;
import cn.didano.base.exception.ServiceException;
import cn.didano.base.json.In_ic_card;
import cn.didano.base.json.Out;
import cn.didano.base.json.OutList;
import cn.didano.base.model.ChannelStatus;
import cn.didano.base.model.Hand_icCardAndSchool_id;
import cn.didano.base.model.Hand_ic_card;
import cn.didano.base.model.Tb_ic_card;
import cn.didano.base.model.Tb_school;
import cn.didano.base.service.IcCardService;
import cn.didano.base.service.SchoolService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(value = "IC卡服务", tags = "IC卡服务，提供给表现层")
@RestController
@RequestMapping(value = "/base/ICCard/post/")
public class IcCardController {
	static Logger logger = Logger.getLogger(IcCardController.class);
	@Autowired
	private IcCardService icCardServices;
	@Autowired
	private SchoolService schoolService;
   
	
	

	/**
	 * 添加ic卡信息
	 * @param tb_ic_card
	 * @return
	 */
	@ApiOperation(value = "添加ic卡信息", notes = "添加ic卡信息")
	@PostMapping(value = "icCard_insert")
	@ResponseBody
	public Out<String> icCard_insert(@ApiParam(value = "添加ic卡信息", required = true) @RequestBody In_ic_card tb_ic_card) {
		logger.info("访问  PostController:channelAdd,tb_ic_card=" + tb_ic_card);
		Tb_ic_card vd_ic = new Tb_ic_card();
		Out<String> back = new Out<String>();
		try {
			Date date=new Date();
			BeanUtils.copyProperties(vd_ic, tb_ic_card);
			vd_ic.setCreated(date);
			vd_ic.setUpdated(date);
			int rowNum = icCardServices.insertSelective(vd_ic);// insert
			if (rowNum > 0) {
				back.setBackTypeWithLog(BackType.SUCCESS_INSERT, "Id=" + vd_ic.getId() + ":rowNum=" + rowNum);
			} else {
				// 更新有问题
				back.setBackTypeWithLog(BackType.FAIL_UPDATE_AFTER_INSERT, "rowNum=" + rowNum);
			}
		} catch (ServiceException e) {
			// 服务层错误，包括 内部service 和 对外service
			back.setServiceExceptionWithLog(e.getExceptionEnums());
		} catch (Exception ex) {
			back.setBackTypeWithLog(BackType.FAIL_INSERT_NORMAL, ex.getMessage());
		}
		return back;
	}
	
	/**
	 * 查询所有ic卡信息  分页
	 * @param tb_ic_card
	 * @return
	 */
	@ApiOperation(value = "查询所有的ic卡信息，并且是存在的信息", notes = "查询所有的ic卡信息，并且是存在的信息")
	@RequestMapping(value = "icCard_selectAll/{page}/{size}", method = { RequestMethod.GET, RequestMethod.POST })
	@PostMapping(value = "icCard_selectAll")
	@ResponseBody
	public Out<PageInfo<Tb_ic_card>> icCard_selectAll(@ApiParam(value = "查询所有的ic卡信息", required = true)@PathVariable("page") int page, @PathVariable("size") int size) {
		logger.info("访问  PostController:icCard_selectAll");
		PageInfo<Tb_ic_card> pageInfo = icCardServices.selectAll(page, size);
		Out<PageInfo<Tb_ic_card>> back = new Out<PageInfo<Tb_ic_card>>(BackType.SUCCESS, pageInfo);
		return back;
	}
	
	/**
	 * 根据ic卡号进行查询
	 * @param tb_ic_card
	 * @return
	 */
	@ApiOperation(value = "根据ic编号进行查询信息，并且是存在的信息", notes = "根据ic编号进行查询信息，并且是存在的信息")
	@PostMapping(value = "icCard_selectNow/{icNumber}")
	@ResponseBody
	public Out<OutList<Tb_ic_card>> icCard_selectNow(@PathVariable("icNumber")String  icNumber) {
		logger.info("访问  PostController:icCard_selectNow");
		List<Tb_ic_card> controls = null;
		OutList<Tb_ic_card> outList = null;
		Out<OutList<Tb_ic_card>> back = new Out<OutList<Tb_ic_card>>();
		//Tb_ic_card ic=new Tb_ic_card();
		//ic.setIcNumber(icNumber2);
		try {
			controls = icCardServices.selectNow(icNumber);
			System.err.println("可以");
			outList = new OutList<Tb_ic_card>(controls.size(), controls);
			back.setBackTypeWithLog(outList, BackType.SUCCESS_SEARCH_NORMAL);
		} catch (ServiceException e) {
			logger.warn(e.getMessage());
			back.setServiceExceptionWithLog(e.getExceptionEnums());
		}
		return back;
	}
	
	
	/**
	 * 根据ic编号和学校的id进行查询信息，并且是存在的信息
	 * @param tb_ic_card
	 * @return
	 */
	@ApiOperation(value = "根据ic编号和学校的id进行查询信息，并且是存在的信息", notes = "根据ic编号和学校的id进行查询信息，并且是存在的信息,没有选学校，默认school_id传0")
	@PostMapping(value = "icCard_selectwhereicNumberAndschool_ic/{icNumber}/{school_id}")
	@ResponseBody
	public Out<OutList<Tb_ic_card>> icCard_selectwhereicNumberAndschool_ic(@PathVariable("icNumber")String  icNumber,@PathVariable("school_id")Integer  school_id) {
		logger.info("访问  PostController:icCard_selectwhereicNumberAndschool_ic");
		if(school_id==null){
			school_id=0;
		}
		List<Tb_ic_card> controls = null;
		OutList<Tb_ic_card> outList = null;
		Out<OutList<Tb_ic_card>> back = new Out<OutList<Tb_ic_card>>();
		Tb_ic_card ic=new Tb_ic_card();
		ic.setIcNumber(icNumber);
		ic.setSchoolId(school_id);
		try {
			controls = icCardServices.selectNoeIcInfo(ic);
			System.err.println("可以");
			outList = new OutList<Tb_ic_card>(controls.size(), controls);
			back.setBackTypeWithLog(outList, BackType.SUCCESS_SEARCH_NORMAL);
		} catch (ServiceException e) {
			logger.warn(e.getMessage());
			back.setServiceExceptionWithLog(e.getExceptionEnums());
		}
		return back;
	}
	/**
	 * c查询某个范围内的ic卡信息（根据两个ic编号），并且是存在的信息
	 * @param tb_ic_card
	 * @return
	 */
	@ApiOperation(value = "查询某个范围内的ic卡信息（根据两个ic编号），并且是存在的信息", notes = "查询某个范围内的ic卡信息（根据两个ic编号），并且是存在的信息")
	@PostMapping(value = "select_between_icInfo/{icNumberNoe}/{icNumberTow}")
	@ResponseBody
	public Out<OutList<Tb_ic_card>> select_between_icInfo(@PathVariable("icNumberNoe")String  icNumberNoe,@PathVariable("icNumberTow")String  icNumberTow) {
		logger.info("访问  PostController:select_between_icInfo");
		List<Tb_ic_card> controls = null;
		OutList<Tb_ic_card> outList = null;
		Out<OutList<Tb_ic_card>> back = new Out<OutList<Tb_ic_card>>();
		try {
			controls = icCardServices.selectNow(icNumberNoe,icNumberTow);
			System.err.println("可以");
			outList = new OutList<Tb_ic_card>(controls.size(), controls);
			back.setBackTypeWithLog(outList, BackType.SUCCESS_SEARCH_NORMAL);
		} catch (ServiceException e) {
			logger.warn(e.getMessage());
			back.setServiceExceptionWithLog(e.getExceptionEnums());
		}
		return back;
	}
	/**
	 * c查询某个范围内的ic卡信息（根据两个ic编号和一个学校的ID编号），并且是存在的信息
	 * @param tb_ic_card
	 * @return
	 */
	@ApiOperation(value = "查询某个范围内的ic卡信息（根据两个ic编号和一个学校的ID编号），并且是存在的信息", notes = "查询某个范围内的ic卡信息（根据两个ic编号和一个学校的ID编号），并且是存在的信息,没有选学校，默认school_id传0")
	@PostMapping(value = "select_between_icInfoAndSchool_id/{icNumberNoe}/{icNumberTow}/{school_id}")
	@ResponseBody
	public Out<OutList<Tb_ic_card>> select_between_icInfoAndSchool_id(@PathVariable("icNumberNoe")String  icNumberNoe,@PathVariable("icNumberTow")String  icNumberTow,@PathVariable("school_id")Integer  school_id) {
		logger.info("访问  PostController:select_between_icInfoAndSchool_id");
		if(school_id==null){
			school_id=0;
		}
		System.err.println(icNumberNoe+"可以"+school_id);
		List<Tb_ic_card> controls = null;
		OutList<Tb_ic_card> outList = null;
		Out<OutList<Tb_ic_card>> back = new Out<OutList<Tb_ic_card>>();
		Hand_icCardAndSchool_id hicCard=new Hand_icCardAndSchool_id(icNumberNoe,icNumberTow,school_id);
		try {
			System.err.println("可以");
			controls = icCardServices.selectAllIcInfo(hicCard);
			System.err.println("可以");
			outList = new OutList<Tb_ic_card>(controls.size(), controls);
			back.setBackTypeWithLog(outList, BackType.SUCCESS_SEARCH_NORMAL);
		} catch (ServiceException e) {
			logger.warn(e.getMessage());
			back.setServiceExceptionWithLog(e.getExceptionEnums());
		}
		return back;
	}
	
	
	/**
	 * 根据卡的类型进行查询ic卡信息
	 * @param tb_ic_card
	 * @return
	 */
	@ApiOperation(value = "根据卡的类型进行查询ic卡信息", notes = "根据卡的类型进行查询ic卡信息")
	@PostMapping(value = "select_ic_whereType/{ic_type}")
	@ResponseBody
	public Out<OutList<Tb_ic_card>> select_ic_whereType(@PathVariable("ic_type")int  ic_type) {
		logger.info("访问  PostController:select_ic_whereType");
		List<Tb_ic_card> controls = null;
		OutList<Tb_ic_card> outList = null;
		Out<OutList<Tb_ic_card>> back = new Out<OutList<Tb_ic_card>>();
		try {
			System.err.println("可以");
			controls = icCardServices.select_ic_whereType(ic_type);
			System.err.println("可以");
			outList = new OutList<Tb_ic_card>(controls.size(), controls);
			back.setBackTypeWithLog(outList, BackType.SUCCESS_SEARCH_NORMAL);
		} catch (ServiceException e) {
			logger.warn(e.getMessage());
			back.setServiceExceptionWithLog(e.getExceptionEnums());
		}
		return back;
	}
	
	
	/**
	 * 根据卡的类型   学校进行查询ic卡信息
	 * @param tb_ic_card
	 * @return
	 */
	
	
	
	
	
	/**
	 *  根据卡的类型   学校    卡状态   进行查询ic卡信息
	 * @param tb_ic_card
	 * @return
	 */
	
	
	
	
	/**
	 *  查询所有的学校
	 * @param tb_school
	 * @return
	 */
	@ApiOperation(value = "查询所有的学校", notes = "查询所有的学校")
	@PostMapping(value = "select_schoolAll")
	@ResponseBody
	public Out<OutList<Tb_school>> select_schoolAll() {
		logger.info("访问  PostController:select_schoolAll");
		List<Tb_school> controls = null;
		OutList<Tb_school> outList = null;
		Out<OutList<Tb_school>> back = new Out<OutList<Tb_school>>();
		try {
			controls = schoolService.selectAll();
			System.err.println("可以");
			outList = new OutList<Tb_school>(controls.size(), controls);
			back.setBackTypeWithLog(outList, BackType.SUCCESS_SEARCH_NORMAL);
		} catch (ServiceException e) {
			logger.warn(e.getMessage());
			back.setServiceExceptionWithLog(e.getExceptionEnums());
		}
		return back;
	}
	
	/**
	 * 查询学校的基准线
	 * @param tb_school
	 * @return
	 */
	@ApiOperation(value = "查询学校的基准线", notes = "查询学校的基准线")
	@PostMapping(value = "select_ICMaxNumber/{school_id}")
	@ResponseBody
	public Out<OutList<Tb_ic_card>> select_ICMaxNumber(@PathVariable("school_id")int  school_id) {
		logger.info("访问  PostController:select_ICMaxNumber");
		List<Tb_ic_card> controls = null;
		OutList<Tb_ic_card> outList = null;
		Out<OutList<Tb_ic_card>> back = new Out<OutList<Tb_ic_card>>();
		try {
			controls = icCardServices.select_ICMaxNumber(school_id);
			System.err.println("可以");
			outList = new OutList<Tb_ic_card>(controls.size(), controls);
			back.setBackTypeWithLog(outList, BackType.SUCCESS_SEARCH_NORMAL);
		} catch (ServiceException e) {
			logger.warn(e.getMessage());
			back.setServiceExceptionWithLog(e.getExceptionEnums());
		}
		return back;
	}
	
	
	/**
	 * 根据卡的状态进行查询（是否未绑定）
	 * @param tb_school
	 * @return
	 */
	@ApiOperation(value = "根据卡的状态进行查询（是否未绑定）", notes = "根据卡的状态进行查询（是否未绑定）")
	@PostMapping(value = "select_ICStatus/{status}")
	@ResponseBody
	public Out<OutList<Tb_ic_card>> select_ICStatus(@PathVariable("status")int  status) {
		logger.info("访问  PostController:select_ICStatus");
		List<Tb_ic_card> controls = null;
		OutList<Tb_ic_card> outList = null;
		Out<OutList<Tb_ic_card>> back = new Out<OutList<Tb_ic_card>>();
		try {
			controls = icCardServices.select_ICStatus(status);
			System.err.println("可以");
			outList = new OutList<Tb_ic_card>(controls.size(), controls);
			back.setBackTypeWithLog(outList, BackType.SUCCESS_SEARCH_NORMAL);
		} catch (ServiceException e) {
			logger.warn(e.getMessage());
			back.setServiceExceptionWithLog(e.getExceptionEnums());
		}
		return back;
	}
	/**
	 * 编辑ic卡  根据ic卡的id进行更新数据
	 * 单条进行更新
	 * 
	 * @param 
	 * @return
	 */
	@ApiOperation(value = "编辑ic卡", notes = "编辑ic卡内容")
	@PostMapping(value = "edit_ic")
	@ResponseBody
	public Out<String> edit_ic(
			@ApiParam(value = "编编辑ic卡对象", required = true) @RequestBody Tb_ic_card tb_ic_card) {
		logger.info("访问  PostController:channelEdit,tb_ic_card=" + tb_ic_card);
		Tb_ic_card ic_card = new Tb_ic_card();
		Out<String> back = new Out<String>();
		ChannelStatus channelStatus = new ChannelStatus(tb_ic_card.getId(), tb_ic_card.getStatus());
		try {
			BeanUtils.copyProperties(ic_card, tb_ic_card);
			int rowNum = icCardServices.updateByPrimaryKeySelective(ic_card);// insert
			if (rowNum > 0) {
				back.setBackTypeWithLog(BackType.SUCCESS_UPDATE,
						"id=" + channelStatus.getChannelId() + "rowNum=" + rowNum);
			} else {
				back.setBackTypeWithLog(BackType.FAIL_UPDATE_NORMAL,
						"id=" + channelStatus.getChannelId() + "rowNum=" + rowNum);
			}
		} catch (ServiceException e) {
			// 服务层错误，包括 内部service 和 对外service
			back.setServiceExceptionWithLog(e.getExceptionEnums());
		} catch (Exception ex) {
			back.setBackTypeWithLog(BackType.FAIL_UPDATE_NORMAL, ex.getMessage());
		}
		return back;
	}
	/**
	 * 
	 * 对一批卡的状态和学校进行修改
	 * @param 
	 * @return
	*/
	
	@ApiOperation(value = "对一批卡的状态和学校进行修改(和学校的交付)", notes = "对一批卡的状态和学校进行修改(和学校的交付)")
	@PostMapping(value = "update_icCardInfo/{ic_numberOne}/{ic_numberTow}")
	@ResponseBody
	public Out<String> update_icCardInfo(
		@ApiParam(value = "对一批卡的状态和学校进行修改(和学校的交付)", required = true) @RequestBody Hand_ic_card hand_ic_card) {
		logger.info("访问  PostController:update_icCardInfo");
		Out<String> back = new Out<String>();
		try {
			int rowNum =icCardServices.updateIcCardInfo(hand_ic_card);
			if (rowNum > 0) {
				back.setBackTypeWithLog(BackType.SUCCESS_UPDATE);

			}
		}catch (Exception ex) {
			back.setBackTypeWithLog(BackType.FAIL_UPDATE_NORMAL, ex.getMessage());
		}
		return back;
	} 
	
	
	/**
	 * 删除ic卡信息
	 * 逻辑删除  不能进行物理删除
	 * 
	 * @param c_channel
	 * @return
	 */
	@ApiOperation(value = "删除ic卡信息", notes = "根据ID进行删除ic卡的信息")
	@PostMapping(value = "remove_ic/{remove_id}")
	@ResponseBody
	public Out<String> time_control_remove(@PathVariable("remove_id") int remove_id) {
		logger.info("访问  PostController:time_control_remove,remove_id=" + remove_id);
		Out<String> back = new Out<String>();
		Tb_ic_card ic_card=new Tb_ic_card();
		ic_card.setId(remove_id);
		ic_card.setDeleted(true);
		try {
			int rowNum = icCardServices.updateByPrimaryKeySelective(ic_card);
			if (rowNum > 0) {
				back.setBackTypeWithLog(BackType.SUCCESS_DELETE, "remove_id=" + remove_id + ":rowNum=" + rowNum);
			} else {
				back.setBackTypeWithLog(BackType.FAIL_DELETE_NO_DELETE, "remove_id=" + remove_id + ":rowNum=" + rowNum);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			back.setBackTypeWithLog(BackType.FAIL_DELETE_NORMAL, ex.getMessage());
		}
		return back;
	}
	
}
