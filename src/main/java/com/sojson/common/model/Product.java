package com.sojson.common.model;

import java.io.Serializable;

public class Product   implements Serializable{
	private static final long serialVersionUID = 1L;
	private int id;
	private String name;
	private int size;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String toString() {
		return "Product [id=" + this.id + ", name=" + this.name + ", size="
				+ this.size + "]";
	}

}
