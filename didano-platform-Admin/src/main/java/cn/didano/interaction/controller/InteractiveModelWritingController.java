package cn.didano.interaction.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import cn.didano.base.constant.BackType;
import cn.didano.base.exception.ServiceException;
import cn.didano.base.interaction.StorageService;
import cn.didano.base.json.Out;
import cn.didano.base.json.OutList;
import cn.didano.base.model.Tb_interactive;
import cn.didano.base.model.Tb_interactive_catalog;
import cn.didano.base.model.Tb_interactive_model;
import cn.didano.base.service.InteractiveModelService;
import cn.didano.video.entity.Interactive;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "互动模板编写", tags = "互动模板服务，提供编写新的模块")
@RestController
@RequestMapping(value = "/video/interactive/post/")
public class InteractiveModelWritingController {

	static Logger logger = Logger.getLogger(InteractiveModelWritingController.class);
     @Autowired
     private InteractiveModelService interactiveService;
     @Autowired
     private StorageService storageService;
     @Autowired
     private Interactive interactive;
     
     /**
 	 * 
 	 * 删除模板
 	 * 
 	 * @param id
 	 * @return
 	 */
 	@PostMapping(value = "deleteModel/{id}")
 	@ApiOperation(value = "删除模板", notes = "删除模板")
 	@ResponseBody
 	public Out<String> deleteModel(@PathVariable("id") Integer id) {
 		logger.info("访问  InteractiveModelWritingController:deleteModel,id=" + id);
 		Out<String> back = new Out<String>();
 		try {
 			int rowNum =interactiveService.deleteModel(id);
 			interactiveService.deleteCatalog(id);
 			if (rowNum > 0) {
 				back.setBackTypeWithLog(BackType.SUCCESS_UPDATE, "rowNum=" + rowNum);
 			} else {
 				back.setBackTypeWithLog(BackType.FAIL_UPDATE_NORMAL, "rowNum=" + rowNum);
 			}
 		} catch (ServiceException e) {
 			logger.warn(e.getMessage());
 			back.setServiceExceptionWithLog(e.getExceptionEnums());
 		}
 		return back;
 	}

     

	/**
	 * 下载
	 */
	@PostMapping(value = "download/{time}")
	@ApiOperation(value = "下载", notes = "下载")
	@ResponseBody
	public Out<OutList<Tb_interactive_model>> download(@PathVariable("time")long time) {
		logger.info("访问   InteractiveModelWritingController :download,time="+time);
		List<Tb_interactive_model> models = null;
		OutList<Tb_interactive_model> outList = null;
		Out<OutList<Tb_interactive_model>> back = new Out<OutList<Tb_interactive_model>>();
		try {
			// 查找所有time时间之后的zip包
			models= interactiveService.findByUpdate(time);
			Tb_interactive_catalog catalog=null;
			Tb_interactive_catalog catalogParent=null;
			for (int i = 0; i < models.size(); i++) {
			   catalog = interactiveService.findCatalogById(models.get(i).getCatalog());
               catalogParent=interactiveService.findCatalogById(catalog.getParentId());
			   StringBuilder sb=new StringBuilder(catalogParent.getName());
			   sb.append("-"+catalog.getName());
			   models.get(i).setCatalogName(sb.toString());
			   sb=new StringBuilder(interactive.getDownload());
			   sb.append(models.get(i).getLocation());
			   models.get(i).setLocation(sb.toString());
			}
			
			if (models.size() > 0) {
				
				outList = new OutList<Tb_interactive_model>(models.size(), models);
				back.setBackTypeWithLog(outList, BackType.SUCCESS_SEARCH_NORMAL);
			} else {
				
				back.setBackTypeWithLog(outList, BackType.FAIL_SEARCH_NORMAL);
			}
		} catch (ServiceException e) {
			logger.warn(e.getMessage());
			back.setServiceExceptionWithLog(e.getExceptionEnums());
		}
		return back;
	}
	
	/**
	 * 查找所有模块
	 */
	@PostMapping(value = "findAllModel")
	@ApiOperation(value = "查找所有模块", notes = "查找所有模块")
	@ResponseBody
	public Out<OutList<Tb_interactive_model>> findAllModel() {
		logger.info("访问   InteractiveModelWritingController :findAllModel");
		List<Tb_interactive_model> models = null;
		OutList<Tb_interactive_model> outList = null;
		Out<OutList<Tb_interactive_model>> back = new Out<OutList<Tb_interactive_model>>();
		try {
			// 查找所有time时间之后的zip包
			models= interactiveService.findAllModel();
			Tb_interactive_catalog catalog=null;
			Tb_interactive_catalog catalogParent=null;
			for (int i = 0; i < models.size(); i++) {
			   catalog = interactiveService.findCatalogById(models.get(i).getCatalog());
               catalogParent=interactiveService.findCatalogById(catalog.getParentId());
			   StringBuilder sb=new StringBuilder(catalogParent.getName());
			   sb.append("-"+catalog.getName());
			   models.get(i).setCatalogName(sb.toString());
			   sb=new StringBuilder(interactive.getDownload());
			   sb.append(models.get(i).getLocation());
			   models.get(i).setLocation(sb.toString());
			}
			
			if (models.size() > 0) {
				
				outList = new OutList<Tb_interactive_model>(models.size(), models);
				back.setBackTypeWithLog(outList, BackType.SUCCESS_SEARCH_NORMAL);
			} else {
				
				back.setBackTypeWithLog(outList, BackType.FAIL_SEARCH_NORMAL);
			}
		} catch (ServiceException e) {
			logger.warn(e.getMessage());
			back.setServiceExceptionWithLog(e.getExceptionEnums());
		}
		return back;
	}
	/**
	 * 查找所有包
	 */
	@PostMapping(value = "catalog")
	@ApiOperation(value = " 查找所有包", notes = " 查找所有包")
	@ResponseBody
	public Out<OutList<Tb_interactive>> catalog() {
		logger.info("访问   InteractiveModelWritingController :findAllModel");
		List<Tb_interactive> locations = null;
		OutList<Tb_interactive> outList = null;
		Out<OutList<Tb_interactive>> back = new Out<OutList<Tb_interactive>>();
		try {
			// 查找所有time时间之后的zip包
			locations=new ArrayList<Tb_interactive>();
			List<Tb_interactive_model> models= interactiveService.findAllModel();
			Tb_interactive location =null;
			for (int i = 0; i < models.size(); i++) {
			  location=new Tb_interactive();
			  StringBuilder sb=new StringBuilder(interactive.getDownload());
			   sb.append(models.get(i).getLocation());
			  location.setLocation(sb.toString());
			  locations.add(location);
			}
			
			if (locations.size() > 0) {
				
				outList = new OutList<Tb_interactive>(locations.size(), locations);
				back.setBackTypeWithLog(outList, BackType.SUCCESS_SEARCH_NORMAL);
			} else {
				
				back.setBackTypeWithLog(outList, BackType.FAIL_SEARCH_NORMAL);
			}
		} catch (ServiceException e) {
			logger.warn(e.getMessage());
			back.setServiceExceptionWithLog(e.getExceptionEnums());
		}
		return back;
	}
}
