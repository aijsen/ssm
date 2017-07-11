package com.sojson.permission.service;

import com.sojson.common.model.Order;

//@Repository
public interface OrderService {
	
	boolean loadOrder(Order o);
	
	int insertOrder(Order o);
	
}
