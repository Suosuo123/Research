package com.bx.research.entity;

import com.lidroid.xutils.db.annotation.Transient;

import java.io.Serializable;

public class BaseEntity implements Serializable {
	@Transient
	private static final long serialVersionUID = 1L;
	@Transient
	private int status = 0;

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
}
