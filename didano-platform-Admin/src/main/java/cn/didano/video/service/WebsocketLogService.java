package cn.didano.video.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import cn.didano.base.constant.DeletedType;
import cn.didano.base.dao.Vd_websocket_logMapper;
import cn.didano.base.exception.DBExceptionEnums;
import cn.didano.base.exception.ServiceException;
import cn.didano.base.model.Vd_websocket_log;
import cn.didano.base.model.Vd_websocket_logExample;

/**
 * 视频控制通道日志表服务
 * 
 * @author stephen.wang 2017年5月10日
 */
@Service
public class WebsocketLogService {
	@Autowired
	private Vd_websocket_logMapper vd_socket_logMapper;

	/**
	 * 查询所有
	 * 
	 * @return
	 */
	public List<Vd_websocket_log> selectAll() {
		Vd_websocket_logExample condition = new Vd_websocket_logExample();
		Vd_websocket_logExample.Criteria criteria = condition.createCriteria();
		// 对于已经deleted=1的不显示
		criteria.andDeletedEqualTo(DeletedType.N0_DELETED.getValue());
		return new PageInfo<Vd_websocket_log>(vd_socket_logMapper.selectByExample(condition)).getList();
	}

	/**
	 * 查询集合
	 * 
	 * @param page
	 * @param size
	 * @return
	 */
	public List<Vd_websocket_log> selectAll(int page, int size) {
		PageHelper.startPage(page, size);
		Vd_websocket_logExample condition = new Vd_websocket_logExample();
		Vd_websocket_logExample.Criteria criteria = condition.createCriteria();
		// 对于已经deleted=1的不显示
		criteria.andDeletedEqualTo(DeletedType.N0_DELETED.getValue());
		return new PageInfo<Vd_websocket_log>(vd_socket_logMapper.selectByExample(condition)).getList();
	}

	/**
	 * 查询单条
	 * 
	 * @param id
	 * @return
	 */
	public Vd_websocket_log selectByPrimaryKey(Integer id) {
		return vd_socket_logMapper.selectByPrimaryKey(id);
	}

	/**
	 * 插入
	 * 
	 * @param record
	 * @return 有值，id ,否则返回-1
	 */
	public int insertSelective(Vd_websocket_log record) {
		if (record == null)
			throw new ServiceException(DBExceptionEnums.ERROR_DB_CONTENT_NULL);
		return vd_socket_logMapper.insertSelective(record);
	}

	/**
	 * 删除
	 * 
	 * @param id
	 * @return 删除行数
	 */
	public int deleteByPrimaryKey(int id) {
		if (id < 1)
			throw new ServiceException(DBExceptionEnums.ERROR_DB_LESS_1);
		return vd_socket_logMapper.deleteByPrimaryKey(id);
	}

	/**
	 * 更新
	 * 
	 * @param record
	 * @return 更新行数
	 */
	public int updateByPrimaryKeySelective(Vd_websocket_log record) {
		if (record == null)
			throw new ServiceException(DBExceptionEnums.ERROR_DB_CONTENT_NULL);
		if (record.getId() == null || record.getId() < 1)
			throw new ServiceException(DBExceptionEnums.ERROR_DB_LESS_1);
		return vd_socket_logMapper.updateByPrimaryKeySelective(record);
	}

	/**
	 * 更新
	 * 
	 * @param record
	 * @return 更新行数
	 */
	public int updateByExampleSelective(Vd_websocket_log record, Vd_websocket_logExample example) {
		if (record == null)
			throw new ServiceException(DBExceptionEnums.ERROR_DB_CONTENT_NULL);
		return vd_socket_logMapper.updateByExampleSelective(record, example);
	}

}
