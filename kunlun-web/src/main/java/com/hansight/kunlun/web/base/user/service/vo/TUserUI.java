package com.hansight.kunlun.web.base.user.service.vo;

import java.util.HashSet;
import java.util.Set;

import com.hansight.kunlun.web.base.user.entity.TRole;


public class TUserUI {
	
	private String id;
	private String userId;
	private String email;
	private String nickName;
	
	private Set<TRole> roles = new HashSet<TRole>();
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public Set<TRole> getRoles() {
		return roles;
	}
	public void setRoles(Set<TRole> roles) {
		this.roles = roles;
	}

}
