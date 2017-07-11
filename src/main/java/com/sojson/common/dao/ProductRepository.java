package com.sojson.common.dao;

import java.util.HashMap;

import com.sojson.common.model.Product;

public interface ProductRepository {
	Product selectProductById(Long id);
	void reduceNum(HashMap<String,Integer> hm);
}
