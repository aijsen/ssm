package com.sojson.common.dao;

import com.sojson.common.model.Order;

public interface OrderRepository {
	void saveOrder(Order order);
}
