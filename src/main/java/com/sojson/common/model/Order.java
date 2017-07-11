package com.sojson.common.model;

import java.io.Serializable;
import java.util.Date;

public class Order   implements Serializable{
	private static final long serialVersionUID = 1L;
	private Long id;
	private Long productId;
	private Date createTime;
	private Integer pnum;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Integer getPnum() {
		return pnum;
	}

	public void setPnum(Integer pnum) {
		this.pnum = pnum;
	}

	public String toString() {
		return "Order [id=" + this.id + ", productId=" + this.productId
				+ ", createTime=" + this.createTime + ", pnum=" + this.pnum
				+ "]";
	}

}
