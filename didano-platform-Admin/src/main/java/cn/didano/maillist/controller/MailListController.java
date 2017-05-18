package cn.didano.maillist.controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import cn.didano.base.constant.BackType;
import cn.didano.base.constant.IcCardType;
import cn.didano.base.constant.StaffType;
import cn.didano.base.exception.ServiceException;
import cn.didano.base.json.In_Student_Edit;
import cn.didano.base.json.In_Teacher_Edit;
import cn.didano.base.json.Out;
import cn.didano.base.json.OutList;
import cn.didano.base.model.Hand_WholeStudentParents4PhoneBook;
import cn.didano.base.model.Hand_ic_type;
import cn.didano.base.model.Hand_parent4mailList;
import cn.didano.base.model.Hand_staff4PhoneBook;
import cn.didano.base.model.Hand_staffTransit4PhoneBook;
import cn.didano.base.model.Hand_student4MailListHasParents;
import cn.didano.base.model.Hand_wholeStudent4PhoneBook;
import cn.didano.base.model.Hand_wholeStudentParent4PhoneBook;
import cn.didano.base.model.Tb_bossData;
import cn.didano.base.model.Tb_class;
import cn.didano.base.model.Tb_classStudent;
import cn.didano.base.model.Tb_deleteParentDate;
import cn.didano.base.model.Tb_ic_card;
import cn.didano.base.model.Tb_relation;
import cn.didano.base.model.Tb_schoolparent4;
import cn.didano.base.model.Tb_sign_type;
import cn.didano.base.model.Tb_staff;
import cn.didano.base.model.Tb_staffData;
import cn.didano.base.model.Tb_staff_class;
import cn.didano.base.model.Tb_student;
import cn.didano.base.model.Tb_student4List;
import cn.didano.base.model.Tb_studentData;
import cn.didano.base.model.Tb_studentparent;
import cn.didano.base.model.Tb_teacherAndStudent;
import cn.didano.base.service.ClassService;
import cn.didano.base.service.IcCardService;
import cn.didano.base.service.MailListService;
import cn.didano.base.service.StaffService;
import cn.didano.base.service.StudentService;
import cn.didano.config.AppConfigProperties;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.sf.json.JSONObject;

@Api(value = "通讯录服务", tags = "通讯录服务，提供给表现层")
@RestController
@RequestMapping(value = "/base/maillist/post/")
public class MailListController {

	static Logger logger = Logger.getLogger(MailListController.class);

	@Autowired
	private MailListService mailListService;
	@Autowired
	private StudentService studentService;
	@Autowired
	private StaffService staffService;
	@Autowired
	private ClassService classService;
	@Autowired
	AppConfigProperties appConfigProperties;
	@Autowired
	IcCardService iCCardService;

	/**
	 * 家长查看本班老师
	 *
	 * @throws ParseException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	@PostMapping(value = "Parent_findteacher/{student_id}")
	@ApiOperation(value = "家长查看本班老师", notes = "通过老师id查询小朋友")
	@ResponseBody
	public Out<OutList<Hand_staff4PhoneBook>> Parent_findteacher(@PathVariable("student_id") Integer student_id) {
		logger.info("访问  MailListController:Parent_findteacher,student_id=" + student_id);
		Hand_WholeStudentParents4PhoneBook student = null;
		List<Hand_staff4PhoneBook> classstaff = null;
		OutList<Hand_staff4PhoneBook> outList = null;
		Out<OutList<Hand_staff4PhoneBook>> back = new Out<OutList<Hand_staff4PhoneBook>>();
		try {
			// 通过学生id查找到该学生
			student = mailListService.findById(student_id);
			// 通过班级id查找该班级所有老师
			classstaff = mailListService.findTeacherByClass(student.getClassId());
			List<Hand_staff4PhoneBook> doctor = new ArrayList<Hand_staff4PhoneBook>();
			Hand_staff4PhoneBook staff1 = null;
			for (Hand_staff4PhoneBook teacher : classstaff) {
				staff1 = new Hand_staff4PhoneBook();
				BeanUtils.copyProperties(staff1, teacher);
				staff1.setSchoolId(teacher.getSchoolId());
				doctor.add(staff1);
			}
			outList = new OutList<Hand_staff4PhoneBook>(doctor.size(), doctor);
			back.setBackTypeWithLog(outList, BackType.SUCCESS_SEARCH_NORMAL);
		} catch (IllegalAccessException e) {
			logger.warn(e.getMessage());
			back.setBackType(BackType.INFO_ILLEGALACCESSEXCEPTION, e.getMessage());
		} catch (InvocationTargetException e) {
			logger.warn(e.getMessage());
			back.setBackType(BackType.INFO_INVOCATIONTARGETEXCEPTION, e.getMessage());
		} catch (ServiceException e) {
			logger.warn(e.getMessage());
			back.setServiceExceptionWithLog(e.getExceptionEnums());
		}
		return back;
	}

	/**
	 * 查询班级关系对应 当前登录老师学校的所有班级
	 */
	@PostMapping(value = "findStaff_ClassInfo/{staff_id}")
	@ApiOperation(value = "查询班级关系对应", notes = "查询班级关系对应")
	@ResponseBody
	public Out<OutList<Tb_class>> findStaff_ClassInfo(@PathVariable("staff_id") Integer id) {
		logger.info("访问  MailListController : findStaff_ClassInfo,staff_id=" + id);
		// 查询当前登录的员工
		Tb_staff staff = staffService.findById(id);
		List<Tb_class> classs = null;
		OutList<Tb_class> outList = null;
		Out<OutList<Tb_class>> back = new Out<OutList<Tb_class>>();
		try {
			// 查询该学校所有班级
			classs = classService.findAll(staff.getSchoolId());
			if (classs.size() > 0) {
				outList = new OutList<Tb_class>(classs.size(), classs);
				back.setBackTypeWithLog(outList, BackType.SUCCESS_UPDATE);
			} else {
				back.setBackTypeWithLog(outList, BackType.FAIL_UPDATE_NORMAL);
			}
		} catch (ServiceException e) {
			logger.warn(e.getMessage());
			back.setServiceExceptionWithLog(e.getExceptionEnums());
		}
		return back;
	}

	/**
	 * 查询家长关系对应
	 */
	@PostMapping(value = "relation")
	@ApiOperation(value = "查询家长关系对应", notes = "查询家长关系对应")
	@ResponseBody
	public Out<OutList<Tb_relation>> relation() {
		logger.info("访问  MailListController :relation");
		List<Tb_relation> relation = null;
		OutList<Tb_relation> outList = null;
		Out<OutList<Tb_relation>> back = new Out<OutList<Tb_relation>>();
		try {
			// 查找所有家长关系及其对应id
			relation = mailListService.findrelation();
			if (relation.size() > 0) {
				outList = new OutList<Tb_relation>(relation.size(), relation);
				back.setBackTypeWithLog(outList, BackType.SUCCESS_UPDATE);
			} else {
				back.setBackTypeWithLog(outList, BackType.FAIL_UPDATE_NORMAL);
			}
		} catch (ServiceException e) {
			logger.warn(e.getMessage());
			back.setServiceExceptionWithLog(e.getExceptionEnums());
		}
		return back;
	}

	/**
	 * 通过名字搜索
	 *
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	@PostMapping(value = "studentstaff_searchByName/{name}/{id}")
	@ApiOperation(value = "通过名字搜索", notes = "通过名字搜索")
	@ResponseBody
	public Out<Tb_bossData> studentstaff_searchByName(@PathVariable("name") String name,
			@PathVariable("id") Integer id) {
		logger.info("访问  MailListController:studentstaff_searchByName,name=" + name + ",id=" + id);
		// 得到当前登录人员的信息，为了得到学校id
		Tb_staff staff4SchoolId = staffService.findById(id);
		// 为了得到班级id
		Hand_staff4PhoneBook staff4ClassId = mailListService.findbystaffbyid(id);
		// 按名字查询老师集合
		List<Tb_staff> staffAll = new ArrayList<Tb_staff>();
		try {
			// 判断当前登录人员是园长还是老师
			if (staff4SchoolId.getType() == StaffType.SCHOOLMASTER.getIndex()) {
				// 设置要查询的人员的名字和该学校id
				Tb_staff staffNameData = new Tb_staff();
				staffNameData.setName("%" + name + "%");
				staffNameData.setSchoolId(staff4SchoolId.getSchoolId());
				// 通过名字和学校id进行模糊查询
				List<Hand_staffTransit4PhoneBook> staffFindByName = staffService.findByNameSchool(staffNameData);
				Hand_staff4PhoneBook target = null;
				Tb_sign_type sign_time = null;
				for (Hand_staffTransit4PhoneBook staff : staffFindByName) {
					target = new Hand_staff4PhoneBook();
					BeanUtils.copyProperties(target, staff);
					// 判断该员工是否需要签到
					if (target.getSignTypeId() != 0) {
						sign_time = staffService.findTypeByID(target.getSignTypeId());
						target.setIn_time(sign_time.getInTime());
						target.setOut_time(sign_time.getOutTime());
					}
					target.setStaff_ic_number(staff.getStaff_ic_number());
					staffAll.add(target);
				}
			} else {
				// 设置要查询的人员的名字和该学校id
				Tb_staff staffNameData = new Tb_staff();
				staffNameData.setName("%" + name + "%");
				staffNameData.setSchoolId(staff4SchoolId.getSchoolId());
				// 查询该名字为园长的人
				List<Hand_staffTransit4PhoneBook> boss = staffService.findBossByNameschool(staffNameData);
				Tb_staffData data2 = new Tb_staffData();
				data2.setName("%" + name + "%");
				data2.setSchoolId(staff4SchoolId.getSchoolId());
				staffAll.addAll(boss);
				// 查询改名字为教职工的人
				List<Hand_staffTransit4PhoneBook> teacherAll = mailListService.findTeacherByNameClass(data2);
				Tb_sign_type sign_time = null;
				// 为教职工补充签到时间等数据
				if (!teacherAll.isEmpty()) {
					for (Hand_staffTransit4PhoneBook one : teacherAll) {
						Hand_staff4PhoneBook target0 = new Hand_staff4PhoneBook();
						BeanUtils.copyProperties(target0, one);
						if (target0.getSignTypeId() != 0) {
							sign_time = staffService.findTypeByID(target0.getSignTypeId());
							target0.setIn_time(sign_time.getInTime());
							target0.setOut_time(sign_time.getOutTime());
						}
						target0.setStaff_ic_number(one.getStaff_ic_number());
						staffAll.add(target0);
					}
				}
				Tb_staff tf = new Tb_staff();
				tf.setName("%" + name + "%");
				tf.setSchoolId(staff4SchoolId.getSchoolId());
				// 查询该名字为医生或勤务的人
				List<Hand_staffTransit4PhoneBook> helper = staffService.findByNameType(tf);
				Tb_sign_type t1 = null;
				if (!helper.isEmpty()) {
					for (Hand_staffTransit4PhoneBook one : helper) {
						Hand_staff4PhoneBook target1 = new Hand_staff4PhoneBook();
						BeanUtils.copyProperties(target1, one);
						if (target1.getSignTypeId() != 0) {
							t1 = staffService.findTypeByID(target1.getSignTypeId());
							target1.setIn_time(t1.getInTime());
							target1.setOut_time(t1.getOutTime());
						}
						target1.setStaff_ic_number(one.getStaff_ic_number());
						staffAll.add(target1);
					}
				}
			}
		} catch (IllegalAccessException e) {
			logger.warn("BeanUtils.copyProperties异常：" + e.getMessage());
		} catch (InvocationTargetException e) {
			logger.warn("BeanUtils.copyProperties异常：" + e.getMessage());
		}
		Tb_bossData data = new Tb_bossData();
		List<Hand_WholeStudentParents4PhoneBook> student = null;
		List<Tb_classStudent> student2 = new ArrayList<Tb_classStudent>();
		// 关于同名学生的处理
		Out<Tb_bossData> back = new Out<Tb_bossData>();
		try {
			if (staffAll.size() != 0) {
				data.getStaff().addAll(staffAll);
			}
			Tb_studentData data1 = new Tb_studentData();
			if (staff4SchoolId.getType() == StaffType.SCHOOLMASTER.getIndex()) {
				// yang修改sql
				data1.setName("%" + name + "%");
				data1.setSchoolid(staff4SchoolId.getSchoolId());
				// 按名字查询学生集合
				student = mailListService.findByname(data1);
			} else {
				// yang修改sql
				data1.setName("%" + name + "%");
				data1.setSchoolid(staff4ClassId.getClassId());
				student = mailListService.findBynameClass(data1);
			}
			// 循环，取每个
			// 当出现一个新的class 创建一个hashmap2 key=student_id value= list
			// 循环将处理，当getkey by student_id =null,创建一个list，并将家长写入list中
			// 如果有，通过hashmap 的key获取这个家长list，然后写入
			// 组装我所需要的数据格式
			Map<Integer, List<Hand_WholeStudentParents4PhoneBook>> map = new HashMap<Integer, List<Hand_WholeStudentParents4PhoneBook>>();
			for (Hand_WholeStudentParents4PhoneBook list : student) {
				List<Hand_parent4mailList> parent = mailListService.findparent(list.getId());
				list.getParent().addAll(parent);
				if (map.containsKey(list.getClassId())) {
					map.get(list.getClassId()).add(list);
				} else {
					List<Hand_WholeStudentParents4PhoneBook> mailLists = new ArrayList<Hand_WholeStudentParents4PhoneBook>();
					mailLists.add(list);
					map.put(list.getClassId(), mailLists);
				}
			}
			Set<Integer> keys = map.keySet();
			Iterator<Integer> it = keys.iterator();
			// 通过班级分类
			while (it.hasNext()) {
				Integer key = it.next();
				List<Hand_WholeStudentParents4PhoneBook> val = map.get(key);
				Tb_classStudent s = new Tb_classStudent();
				s.setClassId(key);
				s.setClassName(classService.selectNameByPrimaryKey(key));
				s.setStudent(val);
				student2.add(s);
			}
			data.setStudentall(student2);
			back.setBackTypeWithLog(data, BackType.SUCCESS_SEARCH_NORMAL);
		} catch (ServiceException e) {
			logger.warn(e.getMessage());
			back.setServiceExceptionWithLog(e.getExceptionEnums());
		}
		return back;
	}

	/**
	 *
	 * 删除员工
	 *
	 * @param teacher_id
	 * @return
	 */
	@PostMapping(value = "deleteStaff/{staff_id}")
	@ApiOperation(value = "删除员工", notes = "删除员工")
	@ResponseBody
	public Out<String> deleteStaff(@PathVariable("staff_id") Integer staff_id) {
		logger.info("访问  MailListController:deleteStaff,staff_id=" + staff_id);
		Out<String> back = new Out<String>();
		try {
			int rowNum = staffService.delete(staff_id);
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
	 * 新增或者编辑职工
	 *
	 * 根据条件创建老师，创建老师之后，再根据生成的老师id与生成其他表的数据
	 *
	 * @param c_channel
	 * @return
	 */
	@ApiOperation(value = "新增或者编辑职工", notes = "新增或者编辑职工")
	@PostMapping(value = "Teacher_add")
	@ResponseBody
	public Out<String> Teacher_add(
			@ApiParam(value = "新增编辑职工", required = true) @RequestBody In_Teacher_Edit teacher_a) {
		logger.info("访问  MailListController:Teacher_add,teacher_a=" + teacher_a);
		// 操作人的信息
		Tb_staff operator = staffService.findById(teacher_a.getStaffid());
		Tb_staff vd_staff = new Tb_staff();
		Tb_staff_class vd_class = new Tb_staff_class();
		Tb_sign_type vd_date = new Tb_sign_type();
		Out<String> back = new Out<String>();
		Tb_ic_card tb_ic_card = null;
		try {
			BeanUtils.copyProperties(vd_staff, teacher_a);
			int rowNum = 0;
			int rowNum3 = 0;
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
			boolean boo = false;
			if (teacher_a.getStaff_ic_number() != "") {
				// 如果输入的编号IC卡表里面不存在，那么返回错误信息，如果有才继续
				tb_ic_card = iCCardService.selectIcByNumber(teacher_a.getStaff_ic_number(), operator.getSchoolId(),
						IcCardType.ADULT.getIndex());
				if (tb_ic_card != null) {
					boo = true;
				}
			} else {
				boo = true;
			}
			if (boo) {// 新增
				// id为null则为新增，id不为null则为编辑
				if (vd_staff.getId() == null) {// 新增
					if (teacher_a.getStaff_ic_number() != null) {
						Tb_ic_card td = new Tb_ic_card();
						td.setIcNumber(teacher_a.getStaff_ic_number());
						iCCardService.updateInfoByic_number(td);
					}
					// 判断要新增的人员是否为园长
					if (teacher_a.getType() != StaffType.SCHOOLMASTER.getIndex()) {
						vd_date.setSchoolId(operator.getSchoolId());
						vd_date.setInTime(sdf.parse(teacher_a.getSetIntime()));
						vd_date.setOutTime(sdf.parse(teacher_a.getSetOuttime()));
						vd_date.setCreated(new Date());
						rowNum3 = staffService.insertTypeSelective(vd_date);
					}
					vd_staff.setCreated(new Date());
					// 根据员工类型看是否需要设置签到时间
					if (teacher_a.getType() != StaffType.SCHOOLMASTER.getIndex()) {
						vd_staff.setSignTypeId(vd_date.getId());
					} else {
						vd_staff.setSignTypeId(0);
					}
					vd_staff.setSchoolId(operator.getSchoolId());
					// yang 新增RFID
					if (tb_ic_card != null) {
						vd_staff.setRfid(tb_ic_card.getRfid());
						// 添加icCardId
						vd_staff.setIcCardId(tb_ic_card.getId());
					} else {
						vd_staff.setRfid("");
						// 添加icCardId
						vd_staff.setIcCardId(88888888);
					}
					rowNum = staffService.insertTeacherSelective(vd_staff);// insert
					// 如果新增人员为老师，设置他所带班级的对应关系
					if (teacher_a.getType() == StaffType.TEACHEER.getIndex()) {
						vd_class.setClassId(teacher_a.getClassId());
						vd_class.setCreated(new Date());
						vd_class.setSchoolId(vd_staff.getSchoolId());
						vd_class.setStaffId(vd_staff.getId());
						staffService.insertClassSelective(vd_class);
					}
					// 引入二维码接口，生成对应的二维码
					HttpPost httpPost = new HttpPost(appConfigProperties.getQrcodePath());
					CloseableHttpClient client = HttpClients.createDefault();
					String respContent = null;

					// 新增家长json方式
					JSONObject jsonParam = new JSONObject();
					jsonParam.put("school_id", vd_staff.getSchoolId());
					jsonParam.put("type", 2);
					jsonParam.put("staff_id", vd_staff.getId());

					StringEntity entity = new StringEntity(jsonParam.toString(), "utf-8");// 解决中文乱码问题
					entity.setContentEncoding("UTF-8");
					entity.setContentType("application/json");
					httpPost.setEntity(entity);

					HttpResponse resp = client.execute(httpPost);
					if (resp.getStatusLine().getStatusCode() == 200) {
						HttpEntity he = resp.getEntity();
						respContent = EntityUtils.toString(he, "UTF-8");
					}
					logger.info("调用php生成二维码接口返回信息:" + respContent);
					if (rowNum > 0) {
						back.setBackTypeWithLog(BackType.SUCCESS_INSERT, "Id=" + "," + ":rowNum3=" + rowNum3);

					} else {
						// 更新有问题
						back.setBackTypeWithLog(BackType.FAIL_UPDATE_AFTER_INSERT, "rowNum=" + rowNum);
					}
				} else {// 编辑
					if (teacher_a.getStaff_ic_number() != null) {
						Tb_ic_card td = new Tb_ic_card();
						td.setIcNumber(teacher_a.getStaff_ic_number());
						iCCardService.updateInfoByic_number(td);
					}
					// 如果修改职工类型为园长，则设置为不用签到
					if (teacher_a.getType() == StaffType.SCHOOLMASTER.getIndex()) {
						vd_staff.setSignTypeId(0);
					} else {
						// 判断该员工是否已经有签到时间数据，没有的话就新增
						if (staffService.findById(teacher_a.getId()).getSignTypeId() != 0) {
							vd_date.setSchoolId(operator.getSchoolId());
							vd_date.setInTime(sdf.parse(teacher_a.getSetIntime()));
							vd_date.setOutTime(sdf.parse(teacher_a.getSetOuttime()));
							vd_date.setId(staffService.findById(teacher_a.getId()).getSignTypeId());
							rowNum3 = staffService.updateType(vd_date);
						} else {
							vd_date.setSchoolId(operator.getSchoolId());
							vd_date.setInTime(sdf.parse(teacher_a.getSetIntime()));
							vd_date.setOutTime(sdf.parse(teacher_a.getSetOuttime()));
							vd_date.setCreated(new Date());
							rowNum3 = staffService.insertTypeSelective(vd_date);
							vd_staff.setSignTypeId(vd_date.getId());
						}
					}

					// yang 编辑的时候也可能修改编号，编号导致同步修改RFID
					// 新卡解绑ic_card_id使用88888888
					if (tb_ic_card != null) {
						vd_staff.setRfid(tb_ic_card.getRfid());
						vd_staff.setIcCardId(tb_ic_card.getId());
					} else if (teacher_a.getIcCardId() != 0) {
						vd_staff.setRfid("");
						vd_staff.setIcCardId(88888888);
					} else {
						vd_staff.setRfid(null);
						vd_staff.setIcCardId(0);
					}

					rowNum = staffService.updatestaff(vd_staff);
					if (teacher_a.getType() == StaffType.TEACHEER.getIndex()) {
						// 判断该人员是否已经有班级对应关系数据，没有则新增
						if (!staffService.findclassidByStaffid(vd_staff.getId()).isEmpty()) {
							vd_class.setClassId(teacher_a.getClassId());

							vd_class.setId(staffService.findclassidByStaffid(vd_staff.getId()).get(0).getId());
							vd_class.setSchoolId(operator.getSchoolId());
							vd_class.setStaffId(teacher_a.getId());
							staffService.updateclass(vd_class);
						} else {
							vd_class.setClassId(teacher_a.getClassId());
							vd_class.setCreated(new Date());
							vd_class.setSchoolId(operator.getSchoolId());
							vd_class.setStaffId(vd_staff.getId());
							staffService.insertClassSelective(vd_class);
						}
					}
					if (rowNum > 0) {
						back.setBackTypeWithLog(BackType.SUCCESS_UPDATE, "Id=" + "," + ":rowNum3=" + rowNum3);

					} else {
						// 更新有问题
						back.setBackTypeWithLog(BackType.FAIL_UPDATE_NORMAL, "rowNum=" + rowNum);
					}
				} // end else
			} else {
				// 输入的ic卡编号有错
				back.setBackTypeWithLog(BackType.FAIL_OPER_NO_STAFF_CARD);
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
	 * 通过班级查找老师
	 */
	@PostMapping(value = "teacher_searchByClass/{class_id}")
	@ApiOperation(value = "通过班级查找老师", notes = "通过班级查找老师")
	@ResponseBody
	public Out<OutList<Hand_staff4PhoneBook>> teacher_searchByClass(@PathVariable("class_id") Integer class_id) {
		logger.info("访问  MailListController:teacher_searchByClass,class_id=" + class_id);
		List<Hand_staff4PhoneBook> teacher = null;
		OutList<Hand_staff4PhoneBook> outList = null;
		Out<OutList<Hand_staff4PhoneBook>> back = new Out<OutList<Hand_staff4PhoneBook>>();
		try {
			teacher = mailListService.findTeacherByClass(class_id);

			outList = new OutList<Hand_staff4PhoneBook>(teacher.size(), teacher);
			back.setBackTypeWithLog(outList, BackType.SUCCESS_SEARCH_NORMAL);
		} catch (ServiceException e) {
			logger.warn(e.getMessage());
			back.setServiceExceptionWithLog(e.getExceptionEnums());
		}
		return back;
	}

	/**
	 * 通过园长id查询小朋友并通过班级分类
	 *
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	@PostMapping(value = "student_searchByBoss/{staff_id}")
	@ApiOperation(value = "通过园长id查询小朋友并通过班级分类", notes = "园长查询小朋友并通过班级分类")
	@ResponseBody
	public Out<Tb_bossData> student_searchByBoss(@PathVariable("staff_id") Integer staff_id) {
		logger.info("访问  MailListController:student_searchByBoss,staff_id=" + staff_id);
		// 获取当前登录人员的信息
		Tb_staff staff = staffService.findById(staff_id);
		Tb_bossData data = new Tb_bossData();
		List<Hand_WholeStudentParents4PhoneBook> student = null;
		List<Tb_classStudent> student2 = new ArrayList<Tb_classStudent>();
		Tb_classStudent cs = null;
		Out<Tb_bossData> back = new Out<Tb_bossData>();
		try {
			// 取该学校所有班级对应的学生家长
			// yang 修改了sql 添加表
			List<Hand_wholeStudentParent4PhoneBook> studentParents = mailListService
					.findWholeStudentParentsByschool(staff.getSchoolId());
			// 学生-学生家长集合的map
			Map<Integer, List<Hand_wholeStudentParent4PhoneBook>> student2WholesMap = new HashMap<Integer, List<Hand_wholeStudentParent4PhoneBook>>();
			for (Hand_wholeStudentParent4PhoneBook studentParent : studentParents) {
				if (student2WholesMap.containsKey(studentParent.getId())) {
					student2WholesMap.get(studentParent.getId()).add(studentParent);
				} else {
					List<Hand_wholeStudentParent4PhoneBook> newOne = new ArrayList<Hand_wholeStudentParent4PhoneBook>();
					newOne.add(studentParent);
					student2WholesMap.put(studentParent.getId(), newOne);
				}
			}

			// 循环遍历成一个学生家长集合
			List<Hand_wholeStudent4PhoneBook> cspAll = new ArrayList<Hand_wholeStudent4PhoneBook>();
			Set<Integer> keys1 = student2WholesMap.keySet();
			Iterator<Integer> it1 = keys1.iterator();
			while (it1.hasNext()) {
				Integer key = it1.next();
				List<Hand_wholeStudentParent4PhoneBook> hand_wholeStudentParent4PhoneBooks = student2WholesMap.get(key);
				List<Hand_parent4mailList> parentall = new ArrayList<Hand_parent4mailList>();
				Hand_parent4mailList parent = null;
				for (Hand_wholeStudentParent4PhoneBook c : hand_wholeStudentParent4PhoneBooks) {
					if (c.getParent_name() != null) {
						parent = new Hand_parent4mailList();
						parent.setParent_name(c.getParent_name());
						parent.setParent_phone(c.getParent_phone());
						parent.setRelation_id(c.getRelation_id());
						parent.setId(c.getParent_id());
						// yang 编辑家长的rfid
						parent.setParent_ic_number(c.getParent_ic_number());
						parent.setIcCardId(c.getParentIcCardId());
						parentall.add(parent);
					}
				}
				Hand_wholeStudent4PhoneBook csp = new Hand_wholeStudent4PhoneBook();
				BeanUtils.copyProperties(csp, hand_wholeStudentParent4PhoneBooks.get(0));
				if (!parentall.isEmpty()) {
					csp.setParent(parentall);
				}
				cspAll.add(csp);
			}
			// 将学生家长集合通过班级分类
			Map<Integer, List<Hand_WholeStudentParents4PhoneBook>> map = new HashMap<Integer, List<Hand_WholeStudentParents4PhoneBook>>();

			// 以上面学生家长信息（包含学生、家长、卡），组装学生（包含多个家长）信息
			Hand_WholeStudentParents4PhoneBook hand_WholeStudentParents = null;

			for (Hand_wholeStudent4PhoneBook one : cspAll) {
				hand_WholeStudentParents = new Hand_WholeStudentParents4PhoneBook();
				hand_WholeStudentParents.setBirthday(one.getBirthday());
				hand_WholeStudentParents.setClassId(one.getClassId());
				hand_WholeStudentParents.setGender(one.getGender());
				hand_WholeStudentParents.setId(one.getId());
				hand_WholeStudentParents.setName(one.getName());
				hand_WholeStudentParents.setClass_name(one.getTitle());
				hand_WholeStudentParents.setParent(one.getParent());
				// 学生的rfid
				hand_WholeStudentParents.setStudent_ic_number(one.getStudent_ic_number());
				hand_WholeStudentParents.setIcCardId(one.getStudentIcCardId());
				// 返回视频状态
				hand_WholeStudentParents.setIs_use(one.getIs_use());
				hand_WholeStudentParents.setSwitchId(one.getSwitchId());
				if (map.containsKey(one.getClassId())) {
					map.get(one.getClassId()).add(hand_WholeStudentParents);
				} else {
					student = new ArrayList<Hand_WholeStudentParents4PhoneBook>();
					student.add(hand_WholeStudentParents);
					map.put(one.getClassId(), student);
				}
			}
			// 循环map将数据返回给总返回数据
			Set<Integer> keys = map.keySet();
			Iterator<Integer> it = keys.iterator();
			while (it.hasNext()) {
				Integer key = it.next();
				List<Hand_WholeStudentParents4PhoneBook> val = map.get(key);

				cs = new Tb_classStudent();
				cs.setClassId(key);
				if (!val.isEmpty()) {
					cs.setClassName(val.get(0).getClass_name());
				}
				cs.setStudent(val);
				student2.add(cs);
			}
			// 返回数据集合
			List<Hand_staff4PhoneBook> staffall = new ArrayList<Hand_staff4PhoneBook>();
			// 校长
			// yang修改了sql
			List<Hand_staffTransit4PhoneBook> bosses = staffService.findBossByschool(staff.getSchoolId());
			if (!bosses.isEmpty()) {
				for (Tb_staff one : bosses) {
					Hand_staff4PhoneBook target = new Hand_staff4PhoneBook();
					BeanUtils.copyProperties(target, one);
					staffall.add(target);
				}
			}
			// 取教职工（老师）
			// yang 修改sql 添加表tb_ic_card
			List<Hand_staff4PhoneBook> teacherall = mailListService.findteacherByschool(staff.getSchoolId());
			Hand_staff4PhoneBook staff1 = null;
			for (Hand_staff4PhoneBook teacher : teacherall) {
				staff1 = new Hand_staff4PhoneBook();
				BeanUtils.copyProperties(staff1, teacher);

				staff1.setSchoolId(teacher.getSchoolId());
				staff1.setIn_time(teacher.getIn_time());
				staff1.setOut_time(teacher.getOut_time());
				staffall.add(staff1);
			}
			// 取教职工（医生、勤务）
			// 没有该sql
			List<Hand_staffTransit4PhoneBook> workers = staffService.findByType(staff.getSchoolId());
			Tb_sign_type t = null;
			if (!workers.isEmpty()) {
				for (Tb_staff one : workers) {
					Hand_staff4PhoneBook target = new Hand_staff4PhoneBook();
					BeanUtils.copyProperties(target, one);
					if (target.getSignTypeId() != 0) {
						t = staffService.findTypeByID(target.getSignTypeId());
						target.setIn_time(t.getInTime());
						target.setOut_time(t.getOutTime());
					}
					staffall.add(target);
				}
			}
			data.getStaff().addAll(staffall);
			data.getStudentall().addAll(student2);
			back.setBackTypeWithLog(data, BackType.SUCCESS_SEARCH_NORMAL);
		} catch (IllegalAccessException e) {
			logger.warn(e.getMessage());
			back.setBackType(BackType.INFO_ILLEGALACCESSEXCEPTION, e.getMessage());
		} catch (InvocationTargetException e) {
			logger.warn(e.getMessage());
			back.setBackType(BackType.INFO_INVOCATIONTARGETEXCEPTION, e.getMessage());
		} catch (ServiceException e) {
			logger.warn(e.getMessage());
			back.setServiceExceptionWithLog(e.getExceptionEnums());
		}
		return back;
	}

	/**
	 * 老师进入通讯簿 通过老师id查询小朋友------------>
	 *
	 * @throws ParseException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	@PostMapping(value = "student_searchByClass/{staff_id}")
	@ApiOperation(value = "通过老师id查询小朋友", notes = "通过老师id查询小朋友")
	@ResponseBody
	public Out<Tb_teacherAndStudent> student_searchByClass(@PathVariable("staff_id") Integer staff_id) {
		logger.info("访问  MailListController:student_searchByClass,staff_id=" + staff_id);
		// 获取当前登录人员的信息
		Tb_staff staff = staffService.findById(staff_id);
		// 获取当前登录老师的班级id
		Tb_staffData classid = mailListService.findClassIdBySid(staff_id);
		Tb_teacherAndStudent data = new Tb_teacherAndStudent();
		List<Hand_WholeStudentParents4PhoneBook> students = null;
		Tb_classStudent cs = new Tb_classStudent();
		Out<Tb_teacherAndStudent> back = new Out<Tb_teacherAndStudent>();
		try {
			if (staff.getType() == StaffType.TEACHEER.getIndex()) {
				// 获取当前老师所带的所有学生
				students = mailListService.findByTeacher(staff.getId());
				if (!students.isEmpty()) {
					// 添加每个学生对应的父母信息
					for (Hand_WholeStudentParents4PhoneBook list : students) {
						List<Hand_parent4mailList> parent = mailListService.findparent(list.getId());
						list.getParent().addAll(parent);
					}

					cs.getStudent().addAll(students);
				}
				cs.setClassId(classid.getClassId());
				cs.setClassName(classService.selectNameByPrimaryKey(classid.getClassId()));

				List<Hand_staff4PhoneBook> all = new ArrayList<Hand_staff4PhoneBook>();
				// 查找该老师所属学校的园长
				List<Hand_staffTransit4PhoneBook> bosses = staffService.findBossByschool(staff.getSchoolId());
				// 校长
				if (!bosses.isEmpty()) {
					for (Tb_staff one : bosses) {
						Hand_staff4PhoneBook target = new Hand_staff4PhoneBook();
						BeanUtils.copyProperties(target, one);
						all.add(target);
					}
				}
				// 取教职工（老师）
				List<Hand_staff4PhoneBook> classstaff = mailListService.findTeacherByClass(classid.getClassId());

				Hand_staff4PhoneBook staff1 = null;
				// 组装老师的各种数据
				for (Hand_staff4PhoneBook teacher : classstaff) {
					staff1 = new Hand_staff4PhoneBook();
					BeanUtils.copyProperties(staff1, teacher);
					staff1.setSchoolId(teacher.getSchoolId());
					staff1.setIn_time(teacher.getIn_time());
					staff1.setOut_time(teacher.getOut_time());
					staff1.setStaff_ic_number(teacher.getStaff_ic_number());
					staff1.setIcCardId(teacher.getIcCardId());
					all.add(staff1);
				}
				// 教职工（后勤）
				List<Hand_staffTransit4PhoneBook> workers = staffService.findByType(staff.getSchoolId());
				Tb_sign_type t = null;
				if (!workers.isEmpty()) {
					for (Tb_staff one : workers) {
						Hand_staff4PhoneBook target = new Hand_staff4PhoneBook();
						BeanUtils.copyProperties(target, one);
						if (target.getSignTypeId() != 0) {
							t = staffService.findTypeByID(target.getSignTypeId());
							target.setIn_time(t.getInTime());
							target.setOut_time(t.getOutTime());
							target.setStaff_ic_number(target.getStaff_ic_number());
							staff1.setIcCardId(target.getIcCardId());
						}
						all.add(target);
					}
				}
				data.getDoctor().addAll(all);
				data.setStudentall(cs);
			}
			back.setBackTypeWithLog(data, BackType.SUCCESS_SEARCH_NORMAL);
		} catch (IllegalAccessException e) {
			logger.warn(e.getMessage());
			back.setBackType(BackType.INFO_ILLEGALACCESSEXCEPTION, e.getMessage());
		} catch (InvocationTargetException e) {
			logger.warn(e.getMessage());
			back.setBackType(BackType.INFO_INVOCATIONTARGETEXCEPTION, e.getMessage());
		} catch (ServiceException e) {
			logger.warn(e.getMessage());
			back.setServiceExceptionWithLog(e.getExceptionEnums());
		}
		return back;
	}

	/**
	 * 通过小朋友id编辑其家长信息
	 *
	 * @param c_channel
	 * @return
	 */
	@ApiOperation(value = "编辑特定小朋友家长的信息", notes = "编辑小朋友家长信息")
	@PostMapping(value = "parent_edit")
	@ResponseBody
	public Out<String> parent_edit(
			@ApiParam(value = "编辑特定小朋友家长的信息", required = true) @RequestBody List<Hand_parent4mailList> parent_edit) {
		logger.info("访问  MailListController:parent_edit,parent_edit=" + parent_edit);
		List<Hand_parent4mailList> parentall = new ArrayList<Hand_parent4mailList>();
		Out<String> back = new Out<String>();
		try {
			parentall.addAll(parent_edit);
			int rowNum = 0;// insert
			for (Hand_parent4mailList parent : parentall) {
				mailListService.UpdateParent(parent);
				rowNum++;
			}
			if (rowNum > 0) {
				back.setBackTypeWithLog(BackType.SUCCESS_UPDATE, "rowNum=" + rowNum);
			} else {
				back.setBackTypeWithLog(BackType.FAIL_UPDATE_NORMAL, "rowNum=" + rowNum);
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
	 * 通讯录首页 通过小朋友id删除该小朋友及其所有家长信息
	 *
	 * @param teacher_id
	 * @return
	 */
	@PostMapping(value = "deleteByChildId/{student_id}")
	@ApiOperation(value = "通过小朋友id删除该小朋友及其所有家长信息", notes = "通过小朋友id删除")
	@ResponseBody
	public Out<String> deleteByChildId(@PathVariable("student_id") Integer student_id) {
		logger.info("访问  MailListController:deleteByChildId,student_id=" + student_id);
		Out<String> back = new Out<String>();
		List<Hand_parent4mailList> parentall = null;
		try {
			// 删除学生信息
			int rowNum = mailListService.delete(student_id);
			// 查询该学生所有的父母并删除
			parentall = mailListService.findparent(student_id);
			if (!parentall.isEmpty()) {
				mailListService.deleteparent(student_id);
			}
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
	 * 通讯录首页 通过小朋友id查询该小朋友所有家长信息
	 *
	 * @param teacher_id
	 * @return
	 */
	@PostMapping(value = "parent_searchByChildId/{student_id}")
	@ApiOperation(value = "通过小朋友id查询所有父母", notes = "通过小朋友id查询")
	@ResponseBody
	public Out<OutList<Hand_parent4mailList>> parent_searchByChildId(@PathVariable("student_id") Integer student_id) {
		logger.info("访问  MailListController:parent_searchByChildId,student_id=" + student_id);
		List<Hand_parent4mailList> parentall = null;
		OutList<Hand_parent4mailList> outList = null;
		Out<OutList<Hand_parent4mailList>> back = new Out<OutList<Hand_parent4mailList>>();
		try {
			parentall = mailListService.findparent(student_id);
			outList = new OutList<Hand_parent4mailList>(parentall.size(), parentall);
			back.setBackTypeWithLog(outList, BackType.SUCCESS_SEARCH_NORMAL);
		} catch (ServiceException e) {
			logger.warn(e.getMessage());
			back.setServiceExceptionWithLog(e.getExceptionEnums());
		}
		return back;
	}

	/**
	 * 通讯录首页 通过小朋友id查询小朋友及其家长信息
	 *
	 * @param teacher_id
	 * @return
	 */
	@PostMapping(value = "student_searchById/{student_id}")
	@ApiOperation(value = "通过小朋友id查询小朋友及其家长信息", notes = "通过小朋友id查询")
	@ResponseBody
	public Out<Hand_WholeStudentParents4PhoneBook> student_searchById(@PathVariable("student_id") Integer student_id) {
		logger.info("访问  MailListController:student_searchById,student_id=" + student_id);
		Hand_WholeStudentParents4PhoneBook student = null;
		Out<Hand_WholeStudentParents4PhoneBook> back = new Out<Hand_WholeStudentParents4PhoneBook>();
		try {
			student = mailListService.findById(student_id);
			List<Hand_parent4mailList> parent = mailListService.findparent(student.getId());
			student.getParent().addAll(parent);

			back.setBackTypeWithLog(student, BackType.SUCCESS_SEARCH_NORMAL);
		} catch (ServiceException e) {
			logger.warn(e.getMessage());
			back.setServiceExceptionWithLog(e.getExceptionEnums());
		}
		return back;
	}

	/**
	 * 通讯录首页 查询所有小朋友及其家长信息
	 *
	 * @param teacher_id
	 * @return
	 */
	@PostMapping(value = "student_searchall")
	@ApiOperation(value = "搜索所有小朋友", notes = "搜索所有小朋友")
	@ResponseBody
	public Out<OutList<Hand_WholeStudentParents4PhoneBook>> student_searchall() {
		logger.info("访问  MailListController:student_searchall");
		List<Hand_WholeStudentParents4PhoneBook> studentall = null;
		OutList<Hand_WholeStudentParents4PhoneBook> outList = null;
		Out<OutList<Hand_WholeStudentParents4PhoneBook>> back = new Out<OutList<Hand_WholeStudentParents4PhoneBook>>();
		try {
			studentall = mailListService.findAll();
			for (Hand_WholeStudentParents4PhoneBook list : studentall) {
				List<Hand_parent4mailList> parent = mailListService.findparent(list.getId());
				list.getParent().addAll(parent);
			}
			outList = new OutList<Hand_WholeStudentParents4PhoneBook>(studentall.size(), studentall);
			back.setBackTypeWithLog(outList, BackType.SUCCESS_SEARCH_NORMAL);
		} catch (ServiceException e) {
			logger.warn(e.getMessage());
			back.setServiceExceptionWithLog(e.getExceptionEnums());
		}
		return back;
	}

	/**
	 * 新建小朋友
	 *
	 * 根据条件创建小朋友，创建小朋友之后，再根据生成的小朋友ID建立父母的联系
	 *
	 * @param c_channel
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	@ApiOperation(value = "新增编辑小朋友", notes = "新增编辑小朋友")
	@PostMapping(value = "Student_add_edit")
	@ResponseBody
	public Out<String> Student_add_edit(
			@ApiParam(value = "新增编辑小朋友", required = true) @RequestBody In_Student_Edit student_a) {
		// throws IllegalAccessException, InvocationTargetException,
		// ClientProtocolException, IOException {
		logger.info("访问  MailListController:Student_add_edit,student_a=" + student_a);
		Tb_class tb_class = classService.selectById(student_a.getClassId());
		Hand_student4MailListHasParents vd_student = new Hand_student4MailListHasParents();
		Hand_WholeStudentParents4PhoneBook list = new Hand_WholeStudentParents4PhoneBook();
		Tb_schoolparent4 vd_parent = new Tb_schoolparent4();
		Tb_studentparent vd_studentparent = new Tb_studentparent();
		Out<String> back = new Out<String>();
		Tb_ic_card tb_ic_cardNoe = null;
		if (student_a.getStudent_ic_number() != "") {
			// 如果输入的编号IC卡表里面不存在，那么返回错误信息，如果有才继续
			tb_ic_cardNoe = iCCardService.selectIcByNumber(student_a.getStudent_ic_number(), tb_class.getSchoolId(),
					IcCardType.BABY.getIndex());
		}
		try {
			BeanUtils.copyProperties(vd_student, student_a);
			vd_student.setClassId(student_a.getClassId());
			// id为null则为新增，不为null则为编辑
			if (vd_student.getId() == null) {// 新增
				if (student_a.getStudent_ic_number() != null) {
					// 修改表tb_ic_card表数据的状态
					Tb_ic_card td = new Tb_ic_card();
					td.setIcNumber(student_a.getStudent_ic_number());
					iCCardService.updateInfoByic_number(td);
				}
				vd_student.setStatus((byte) 1);
				vd_student.setCreated(new Date());
				vd_student.setSchoolId(tb_class.getSchoolId());
				if (tb_ic_cardNoe != null) {
					// 学生的添加studebt_rfid，传进来的只是编号 要天加ic卡中ic_rfid
					vd_student.setStudent_ic_number(tb_ic_cardNoe.getRfid());
					vd_student.setIcCardId(tb_ic_cardNoe.getId());
				} else {
					vd_student.setStudent_ic_number("");
					vd_student.setIcCardId(88888888);
				}
				int rowNum = studentService.insertStudentSelective(vd_student);// insert

				if (!student_a.getParent().isEmpty()) {
					// 修改家长的ic卡状态
					List<Hand_parent4mailList> parent3 = student_a.getParent();
					for (Hand_parent4mailList tb_parent4mailList : parent3) {
						if (tb_parent4mailList.getParent_ic_number() != null) {
							Tb_ic_card td = new Tb_ic_card();
							td.setIcNumber(tb_parent4mailList.getParent_ic_number());
							iCCardService.updateInfoByic_number(td);
						}
					}
					// List<Tb_ic_card> tb_ic_cardTow = new
					// ArrayList<Tb_ic_card>();
					// HashMap hasIcNumberMap = new HashMap();
					for (Hand_parent4mailList add : student_a.getParent()) {
						// 查询家长的ic卡是否存在
						Tb_ic_card tb_ic_card = null;
						if ("".equals(add.getParent_ic_number())) {
							tb_ic_card = iCCardService.selectIcByNumber(add.getParent_ic_number(),
									tb_class.getSchoolId(), IcCardType.ADULT.getIndex());
						}

						vd_parent.setSchoolId(classService.selectById(vd_student.getClassId()).getSchoolId());
						vd_parent.setPhone(add.getParent_phone());
						vd_parent.setParent_id_number(add.getParent_ic_number());
						vd_parent.setType(1);
						vd_parent.setStatus((byte) 1);
						vd_parent.setCreated(new Date());

						studentService.insertParentSelective(vd_parent);
						vd_studentparent.setSchoolId(vd_student.getSchoolId());
						vd_studentparent.setClassId(vd_student.getClassId());
						vd_studentparent.setStudentId(vd_student.getId());
						vd_studentparent.setParentId(vd_parent.getId());
						vd_studentparent.setRelationId(add.getRelation_id());
						// 添加父母的rfid 在添加的时候有rfid就添加 咩有就默认为null
						if (tb_ic_card != null) {
							vd_studentparent.setRfid(tb_ic_card.getRfid());
							vd_studentparent.setIcCardId(tb_ic_card.getId());

						} else {
							vd_studentparent.setRfid("");
							vd_studentparent.setIcCardId(88888888);
						}
						if (add.getRelation_id() != 99) {
							vd_studentparent.setRelationTitle(
									mailListService.findrealtionById(add.getRelation_id()).getTitle());
						} else {
							vd_studentparent.setRelationTitle(add.getParent_name());
						}
						vd_studentparent.setCreated(new Date());

						studentService.insertStudentParentSelective(vd_studentparent);
						HttpPost httpPost = new HttpPost(appConfigProperties.getQrcodePath());
						logger.info("appConfigProperties.getQrcodePath()=" + appConfigProperties.getQrcodePath());
						CloseableHttpClient client = HttpClients.createDefault();
						String respContent = null;

						// json方式
						JSONObject jsonParam = new JSONObject();
						jsonParam.put("school_id", vd_parent.getSchoolId());
						jsonParam.put("type", 1);
						jsonParam.put("parent_id", vd_parent.getId());
						jsonParam.put("student_id", vd_student.getId());

						StringEntity entity = new StringEntity(jsonParam.toString(), "utf-8");// 解决中文乱码问题
						entity.setContentEncoding("UTF-8");
						entity.setContentType("application/json");
						httpPost.setEntity(entity);

						HttpResponse resp = client.execute(httpPost);
						logger.info("resp.getStatusLine().getStatusCode()=" + resp.getStatusLine().getStatusCode());
						if (resp.getStatusLine().getStatusCode() == 200) {
							HttpEntity he = resp.getEntity();
							respContent = EntityUtils.toString(he, "UTF-8");
						}
						logger.info("respContent=" + respContent);

					}
				}
				if (rowNum >= 0) {
					back.setBackTypeWithLog(BackType.SUCCESS_INSERT, "成功");

				} else {
					// 更新有问题
					back.setBackTypeWithLog(BackType.FAIL_UPDATE_AFTER_INSERT, "rowNum=" + rowNum);
				}
			} else {// 编辑
				if (student_a.getStudent_ic_number() != null) {
					// 修改表tb_ic_card表数据的状态
					Tb_ic_card td = new Tb_ic_card();
					td.setIcNumber(student_a.getStudent_ic_number());
					iCCardService.updateInfoByic_number(td);
				}
				// 新卡和老卡使用88888888开区分
				BeanUtils.copyProperties(list, student_a);
				if (tb_ic_cardNoe != null) {
					// 添加学生的rifid
					list.setStudent_ic_number(tb_ic_cardNoe.getRfid());
					list.setIcCardId(tb_ic_cardNoe.getId());
				} else if (student_a.getIcCardId() != 0) {
					list.setStudent_ic_number("");
					list.setIcCardId(88888888);
				} else {
					list.setIcCardId(0);
				}
				// 更新学生信息
				list.setClassId(student_a.getClassId());

				int rowNum = mailListService.Update(list);// insert 以修改sql

				// 删除掉已经取消的父母信息
				if (!"".equals(student_a.getDeleteParents())) {
					String[] arr = student_a.getDeleteParents().split("_");
					Tb_deleteParentDate date = null;
					for (int i = 0; i < arr.length; i++) {
						date = new Tb_deleteParentDate();
						date.setStudent_id(list.getId());
						date.setParent_id(Integer.parseInt(arr[i]));
						mailListService.deleteparentByid(date);
					}
				}
				if (!student_a.getParent().isEmpty()) {
					// 循环遍历是使用的循环变量
					Integer i = 0;
					for (Hand_parent4mailList add : student_a.getParent()) {
						System.err.println(add.getId() + "_----------->");
						Tb_ic_card tb_ic_card = null;

						if (add.getParent_ic_number() != "") {
							tb_ic_card = iCCardService.selectIcByNumber(add.getParent_ic_number(),
									tb_class.getSchoolId(), IcCardType.ADULT.getIndex());
						}
						// 修改家长的ic卡状态
						List<Hand_parent4mailList> parent3 = student_a.getParent();
						for (Hand_parent4mailList tb_parent4mailList : parent3) {
							if (tb_parent4mailList.getParent_ic_number() != null) {
								Tb_ic_card td = new Tb_ic_card();
								td.setIcNumber(tb_parent4mailList.getParent_ic_number());
								iCCardService.updateInfoByic_number(td);
							}
						}
						// 如果父母的id为null则为新增父母，否则为编辑
						if (add.getId() == null) {// 新增家长
							vd_parent.setSchoolId(classService.selectById(list.getClassId()).getSchoolId());
							vd_parent.setPhone(add.getParent_phone());
							vd_parent.setType(1);
							vd_parent.setStatus((byte) 1);
							vd_parent.setCreated(new Date());
							studentService.insertParentSelective(vd_parent);
							vd_studentparent.setSchoolId(classService.selectById(list.getClassId()).getSchoolId());
							vd_studentparent.setClassId(list.getClassId());
							vd_studentparent.setStudentId(list.getId());
							vd_studentparent.setParentId(vd_parent.getId());
							vd_studentparent.setRelationId(add.getRelation_id());
							// 家长信息中选择其他的时候
							if (add.getRelation_id() != 99) {
								vd_studentparent.setRelationTitle(
										mailListService.findrealtionById(add.getRelation_id()).getTitle());
							} else {
								vd_studentparent.setRelationTitle(add.getParent_name());
							}
							vd_studentparent.setCreated(new Date());
							// yang 添加家长的rfid
							if (tb_ic_card != null) {
								vd_studentparent.setRfid(tb_ic_card.getRfid());
								vd_studentparent.setIcCardId(tb_ic_card.getId());
							} else {
								vd_studentparent.setIcCardId(0);
							}
							studentService.insertStudentParentSelective(vd_studentparent);
							// 引入二维码接口，为家长生成对应的二维码
							HttpPost httpPost = new HttpPost(appConfigProperties.getQrcodePath());
							logger.info("appConfigProperties.getQrcodePath()=" + appConfigProperties.getQrcodePath());
							CloseableHttpClient client = HttpClients.createDefault();
							String respContent = null;

							// json方式
							JSONObject jsonParam = new JSONObject();
							jsonParam.put("school_id", vd_parent.getSchoolId());
							jsonParam.put("type", 1);
							jsonParam.put("parent_id", vd_parent.getId());
							jsonParam.put("student_id", list.getId());

							StringEntity entity = new StringEntity(jsonParam.toString(), "utf-8");// 解决中文乱码问题
							entity.setContentEncoding("UTF-8");
							entity.setContentType("application/json");
							httpPost.setEntity(entity);

							HttpResponse resp = client.execute(httpPost);
							logger.info("resp.getStatusLine().getStatusCode()=" + resp.getStatusLine().getStatusCode());
							if (resp.getStatusLine().getStatusCode() == 200) {
								HttpEntity he = resp.getEntity();
								respContent = EntityUtils.toString(he, "UTF-8");
							}
							logger.info("respContent=" + respContent);
						} else {// 编辑家长
							Tb_studentparent tsp = new Tb_studentparent();
							tsp.setId(student_a.getId());
							tsp.setParentId(add.getId());
							Hand_parent4mailList p = mailListService.findParentByPid(tsp);

							p.setId(add.getId());
							// 家长信息中选择其他的时候
							if (add.getRelation_id() != 99) {
								Tb_relation relation = mailListService.findrealtionById(add.getRelation_id());
								p.setParent_name(relation.getTitle());
								p.setRelation_id(relation.getId());
							} else {
								p.setRelation_id(99);
								p.setParent_name(add.getParent_name());
							}
							p.setParent_phone(add.getParent_phone());
							// yang 添加家长的rfid
							if (tb_ic_card != null) {
								p.setParent_ic_number(tb_ic_card.getRfid());
								p.setIcCardId(tb_ic_card.getId());
							} else if (student_a.getParent().get(i).getIcCardId() != 0) {
								p.setParent_ic_number("");
								p.setIcCardId(88888888);
							} else {
								p.setIcCardId(0);
							}
							mailListService.UpdateParent(p);
						}
						i = i + 1;
					}
				}
				if (rowNum > 0) {
					back.setBackTypeWithLog(BackType.SUCCESS_UPDATE, "rowNum=" + (rowNum + rowNum));
				} else {
					back.setBackTypeWithLog(BackType.FAIL_UPDATE_NORMAL, "rowNum=" + (rowNum + rowNum));
				}
			}
		} catch (IllegalAccessException e) {
			logger.warn(e.getMessage());
			back.setBackType(BackType.INFO_ILLEGALACCESSEXCEPTION, e.getMessage());
		} catch (InvocationTargetException e) {
			logger.warn(e.getMessage());
			back.setBackType(BackType.INFO_INVOCATIONTARGETEXCEPTION, e.getMessage());
		} catch (IOException e) {
			logger.warn(e.getMessage());
			back.setBackType(BackType.INFO_IOEXCEPTION, e.getMessage());
		} catch (ServiceException e) {
			// 服务层错误，包括 内部service 和 对外service
			back.setServiceExceptionWithLog(e.getExceptionEnums());
		}
		return back;
	}

	/**
	 * 新增编辑小朋友之前验证ic卡号是否被绑定过了
	 *
	 * @param c_channel
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	@ApiOperation(value = "新增编辑小朋友和教师之前验证ic卡号是否被绑定过了", notes = "新增编辑小朋友和教师之前验证ic卡号是否被绑定过了")
	@PostMapping(value = "Student_add_parent_edit")
	@ResponseBody
	public Out<String> Student_add_parent_edit(
			@ApiParam(value = "新增编辑小朋友和教师之前验证ic卡号是否被绑定过了", required = true) @RequestBody Hand_ic_type hand_id_type){
		logger.info("访问  MailListController:Student_add_parent_edit,ic_number=" + hand_id_type);
		// 得到学校的id
		Hand_staff4PhoneBook s1 = mailListService.selectSchoolBystaffId(hand_id_type.getStaff_id());
		Out<String> back = new Out<String>();
		Tb_ic_card tb_ic_card = null;
		Tb_student tb_student = new Tb_student();
		Tb_student4List findStudentByIcNumber = null;
		Hand_wholeStudent4PhoneBook findParentByIcNumber = null;
		Hand_staffTransit4PhoneBook selectInfoByic_number = null;
		Tb_staff tb_staff = new Tb_staff();
		try {
			if (hand_id_type.getIc_number() != "") {
				// 零时变量
				int a = 0;
				// 拿前端传过来的值进行判断，再进行保存类型到a变量中方便查询
				if (hand_id_type.getType() == 2 || hand_id_type.getType() == 3) {
					a = 1;
				} else {
					a = 2;
				}
				// 查询前端传送过来的ic_nukber在tb_ic_card中对应的记录
				tb_ic_card = iCCardService.selectIcByNumber(hand_id_type.getIc_number(), s1.getSchoolId(), a);
			}
			// 判断是学生还是家长
			if (tb_ic_card != null) {
				if (hand_id_type.getType() == 1) {
					// 查询学生信息最后返回该信息
					tb_student.setRfid(tb_ic_card.getRfid());
					tb_student.setId(hand_id_type.getId());
					findStudentByIcNumber = mailListService.findStudentByIcNumber(tb_student);
				} else if (hand_id_type.getType() == 2) {
					// 查询家长的信息最后返回
					tb_student.setRfid(tb_ic_card.getRfid());
					tb_student.setId(hand_id_type.getId());
					findParentByIcNumber = mailListService.findParentByIcNumber(tb_student);
				} else if (hand_id_type.getType() == 3) {
					// 同时区查询教师
					tb_staff.setRfid(tb_ic_card.getRfid());
					tb_staff.setId(hand_id_type.getId());
					selectInfoByic_number = iCCardService.selectInfoByic_number(tb_staff);
				}
				if (findStudentByIcNumber != null) {
					// 学生返回信息
					back.setBackTypeWithLogInfo(BackType.FAIL_OPER_NO_BOUNDSTATE_CARD, "已经被绑定",
							hand_id_type.getIc_number() + "，已经被" + findStudentByIcNumber.getSchoolTitle() + ","
									+ findStudentByIcNumber.getClassTitle() + "的学生‘" + findStudentByIcNumber.getName()
									+ "’绑定");
				} else if (findParentByIcNumber != null) {
					// 家长返回返回的信息
					back.setBackTypeWithLogInfo(BackType.FAIL_OPER_NO_BOUNDSTATE_CARD, "已经被绑定",
							hand_id_type.getIc_number() + "，已经被" + findParentByIcNumber.getSchoolTitle() + ","
									+ findParentByIcNumber.getClassTitle() + "‘" + findParentByIcNumber.getName()
									+ "小朋友的" + findParentByIcNumber.getTitle() + "’绑定");
				} else if (selectInfoByic_number != null) {
					back.setBackTypeWithLogInfo(BackType.FAIL_OPER_NO_BOUNDSTATE_CARD, "已经被绑定",
							hand_id_type.getIc_number() + "，已经被" + selectInfoByic_number.getPhone() + "的教职工‘"
									+ selectInfoByic_number.getName() + "’绑定");
				} else {
					back.setBackType(BackType.SUCCESS, "可以进行绑定");
				}

			} else {
				// 学生返回信息
				back.setBackTypeWithLogInfo(BackType.FAIL_OPER_NO_BOUNDSTATE_CARD,
						"没有该卡号为" + hand_id_type.getIc_number() + "的信息", hand_id_type.getIc_number() + "，在系统中没有记录");
			}
		} catch (ServiceException e) {
			// 服务层错误，包括 内部service 和 对外service
			back.setServiceExceptionWithLog(e.getExceptionEnums());
		}
		return back;
	}
}
