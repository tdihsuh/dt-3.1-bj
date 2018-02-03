package com.hansight.kunlun.web.base.user.service.vo;

import java.util.Date;

public class TUserVo{
	private String id;
	private String userId;
	private String password;
	private String email;
	private String nickName;
	private Long useFlag;
	private Date createDate;
	private Long accountNonExpired;
	private Long accountNonLocked;
	private Long credentialsNonExpired;
	private Date lastLoginDate;
	
	public void setId(String id){
		this.id = id;
	}
	
	public Long getUseFlag(){
		return useFlag;
	}
	
	public void setUseFlag(Long useFlag){
		this.useFlag = useFlag;
	}
	
	public Date getCreateDate(){
		return createDate;
	}
	
	public void setCreateDate(Date createDate){
		this.createDate = createDate;
	}
	
	public String getUserId(){
		return userId;
	}
	
	public void setUserId(String userId){
		this.userId = userId;
	}
	
	public void setPassword(String password){
		this.password = password;
	}
	
	public String getEmail(){
		return email;
	}
	
	public void setEmail(String email){
		this.email = email;
	}
	
	public String getNickName(){
		return nickName;
	}
	
	public void setNickName(String nickName){
		this.nickName = nickName;
	}
	
	public Long getAccountNonExpired(){
		return accountNonExpired;
	}
	
	public void setAccountNonExpired(Long accountNonExpired){
		this.accountNonExpired = accountNonExpired;
	}
	
	public Long getAccountNonLocked(){
		return accountNonLocked;
	}
	
	public void setAccountNonLocked(Long accountNonLocked){
		this.accountNonLocked = accountNonLocked;
	}
	
	public Long getCredentialsNonExpired(){
		return credentialsNonExpired;
	}
	
	public void setCredentialsNonExpired(Long credentialsNonExpired){
		this.credentialsNonExpired = credentialsNonExpired;
	}
	
	public Date getLastLoginDate(){
		return lastLoginDate;
	}
	
	public void setLastLoginDate(Date lastLoginDate){
		this.lastLoginDate = lastLoginDate;
	}
	
	public String getId(){
		return id;
	}
	
	public String getPassword(){
		return password;
	}
	
}