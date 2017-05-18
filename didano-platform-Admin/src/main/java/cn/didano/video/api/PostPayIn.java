package cn.didano.video.api;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import cn.didano.base.constant.BackType;
import cn.didano.base.exception.ServiceException;
import cn.didano.base.json.Out;
import cn.didano.base.model.Bs_pay;
import cn.didano.base.service.PayService;
import cn.didano.pingxx.webhook.Root;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * post操作 操作 add/edit/delete
 * 
 * @author stephen Created on 2016年12月17日 下午6:38:30
 */
@Api(value = "对外API服务", tags = "远程webhook服务")
@RestController
@RequestMapping(value = "/video/api/post/")
public class PostPayIn {
	static Logger logger = Logger.getLogger(PostPayIn.class);
	@Autowired
	private PayService payService;

	/*
	 * { "id": "evt_ugB6x3K43D16wXCcqbplWAJo", "created": 1427555101,
	 * "livemode": true, "type": "charge.succeeded", "data": { "object": { "id":
	 * "ch_Xsr7u35O3m1Gw4ed2ODmi4Lw", "object": "charge", "created": 1427555076,
	 * "livemode": true, "paid": true, "refunded": false, "app":
	 * "app_1Gqj58ynP0mHeX1q", "channel": "upacp", "order_no": "123456789",
	 * "client_ip": "127.0.0.1", "amount": 100, "amount_settle": 100,
	 * "currency": "cny", "subject": "Your Subject", "body": "Your Body",
	 * "extra": {}, "time_paid": 1427555101, "time_expire": 1427641476,
	 * "time_settle": null, "transaction_no": "1224524301201505066067849274",
	 * "refunds": { "object": "list", "url":
	 * "/v1/charges/ch_L8qn10mLmr1GS8e5OODmHaL4/refunds", "has_more": false,
	 * "data": [] }, "amount_refunded": 0, "failure_code": null, "failure_msg":
	 * null, "metadata": {}, "credential": {}, "description": null } },
	 * "object": "event", "pending_webhooks": 0, "request":
	 * "iar_qH4y1KbTy5eLGm1uHSTS00s" }
	 */

	/*
	 * "id": "ch_Xsr7u35O3m1Gw4ed2ODmi4Lw", "object": "charge", "created":
	 * 1427555076, "livemode": true, "paid": true, "refunded": false, "app":
	 * "app_1Gqj58ynP0mHeX1q", "channel": "upacp", "order_no": "123456789",
	 * "client_ip": "127.0.0.1", "amount": 100, "amount_settle": 100,
	 * "currency": "cny", "subject": "Your Subject", "body": "Your Body",
	 * "extra": {}, "time_paid": 1427555101, "time_expire": 1427641476,
	 * "time_settle": null, "transaction_no": "1224524301201505066067849274",
	 */

	/*
	 * "id": "ch_Xsr7u35O3m1Gw4ed2ODmi4Lw", "created": 1427555076, "paid": true,
	 * "app": "app_1Gqj58ynP0mHeX1q", "channel": "upacp", "order_no":
	 * "123456789", "amount": 100, "amount_settle": 100, "subject":
	 * "Your Subject", ===>商品类型 "body": "Your Body", "transaction_no":
	 * "1224524301201505066067849274",
	 */

	/**
	 * 支付信息回调
	 * 
	 * @param c_channel
	 * @return
	 */
	@ApiOperation("接收支付信息")
	@PostMapping(value = "pay_message_in")
	@ResponseBody
	public Out pay_message_in(@RequestBody Root root) {
		cn.didano.pingxx.webhook.Object ob = root.getData().getObject();
		Bs_pay bs_pay = new Bs_pay();
		Out back = new Out();
		try {
			BeanUtils.copyProperties(bs_pay, ob);
			bs_pay.setChargeId(ob.getId());
			bs_pay.setOrderNo(ob.getOrder_no());
			bs_pay.setTransactionNo(ob.getTransaction_no());
			bs_pay.setAmountSettle((float) ob.getAmount_settle());
			int bac = payService.insertSelective(bs_pay);
			if (bac < 1) {
				logger.warn(BackType.FAIL_INSERT_NO_INSERT.getMessage());
				back = new Out(BackType.FAIL_INSERT_NO_INSERT);
			} else {
				logger.info((BackType.SUCCESS_INSERT.getMessage() + ":insert back=" + bac));
				back = new Out(BackType.SUCCESS_INSERT, "insert back=" + bac);
			}
		} catch (ServiceException de) {
			back.setServiceExceptionWithLog(de.getExceptionEnums());
		} catch (Exception e) {
			e.printStackTrace();
			back.setBackTypeWithLog(BackType.FAIL_INSERT_NORMAL);
		}
		return back;
	}

}
