package com.sojson.permission.service.impl;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sojson.common.dao.OrderRepository;
import com.sojson.common.dao.ProductRepository;
import com.sojson.common.dao.URoleMapper;
import com.sojson.common.model.Order;
import com.sojson.common.model.Product;
import com.sojson.common.utils.LockUtil;
import com.sojson.core.mybatis.OracleOfBaseMybatisDao;
import com.sojson.permission.service.OrderService;

@Service("orderService")
public class OrderServiceImpl extends OracleOfBaseMybatisDao<URoleMapper> implements OrderService {

//	@Resource
	@Autowired
	private OrderRepository orderMapper;
//	@Resource
	@Autowired
	private ProductRepository productMapper;


	@Override
	@Transactional
	public boolean loadOrder(Order o) {
		LockUtil.init("192.168.1.110:2181,jfapp1:2181");
		LockUtil.getExclusiveLock();
		//获取当前的产品库存数量
		Product nowp = productMapper.selectProductById(o.getProductId());
		if(nowp.getSize()>=o.getPnum()){
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			orderMapper.saveOrder(o);
			HashMap<String,Integer> hm = new HashMap<String,Integer>();
			hm.put("nums", o.getPnum());
			hm.put("id",nowp.getId());
			productMapper.reduceNum(hm);
			System.out.println("库存充足，购买成功");
		}else{
			System.out.println("库存不足，购买失败");
			return false;
		}
		LockUtil.unlockForExclusive();
		return true;
	}
	
	@Override
	@Transactional
	public int insertOrder(Order o) {
		
		orderMapper.saveOrder(o);
		
		return 1;
	}
	
	
	
	
}
