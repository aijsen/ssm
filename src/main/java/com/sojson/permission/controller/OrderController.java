package com.sojson.permission.controller;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.sojson.common.controller.BaseController;
import com.sojson.common.model.Order;
import com.sojson.permission.service.OrderService;


@Controller
@Scope(value="prototype")
@RequestMapping("order")
public class OrderController extends BaseController {
	
	@Autowired
	OrderService orderService;
	
	@RequestMapping(value="saveOrder")
	public ModelAndView saveOrder(){
		Order order = new Order();
		order.setId(1L);
		order.setCreateTime(new Date());
		order.setPnum(1);
		order.setProductId(1L);
		int ins = orderService.insertOrder(order);
		return new ModelAndView("order/index","page",ins);
	}
	
	
	@RequestMapping(value="loadOrder")
	public ModelAndView loadOrder(ModelMap modelMap){
		
		Order order = new Order();
		order.setProductId(1L);
		boolean doOrder = orderService.loadOrder(order);
		return new ModelAndView("order/index","page",doOrder);
	}
	
	
	
	
}