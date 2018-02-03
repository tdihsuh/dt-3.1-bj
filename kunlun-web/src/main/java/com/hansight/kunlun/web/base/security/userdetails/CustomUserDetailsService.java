package com.hansight.kunlun.web.base.security.userdetails;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.hansight.kunlun.web.base.user.entity.TRole;
import com.hansight.kunlun.web.base.user.entity.TUser;
//import com.hansight.kunlun.web.base.user.entity.TUserRole;
import com.hansight.kunlun.web.base.user.service.TRoleService;
//import com.hansight.kunlun.web.base.user.service.TUserRoleService;
import com.hansight.kunlun.web.base.user.service.TUserService;
import com.hansight.kunlun.web.base.user.service.vo.TUserRoleQueryBean;

public class CustomUserDetailsService implements UserDetailsService{
	
	@Autowired
	private TUserService tUserService;
//	@Autowired
//	private TUserRoleService tUserRoleService;
	@Autowired
	private TRoleService tRoleService;

	public UserDetails loadUserByUsername(String userName)
			throws UsernameNotFoundException{
		
		try{
			final TUser user = tUserService.queryTUserByUserId(userName);
			return new User(user.getUserId(),user.getPassword(),true,true,true,true,getRoles(user));
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	private List<GrantedAuthority> getRoles(TUser user) throws Exception{
		List<GrantedAuthority> roles = new ArrayList<GrantedAuthority>();
		TUserRoleQueryBean tUserRoleQueryBean = new TUserRoleQueryBean();
		tUserRoleQueryBean.setUserId(user.getId());
//		List<TUserRole> list = tUserRoleService.list(tUserRoleQueryBean);
		List<String> roleIds = new ArrayList<String>();
		for(TRole role:user.getRoles()){
			roleIds.add(role.getId());
		}
		if(roleIds != null && roleIds.size() > 0){
			List<TRole> rList = tRoleService.queryTRoleByIds(roleIds);
			for(TRole role:rList){
				roles.add(new GrantedAuthorityImpl(role.getName()));
			}
		}
		return roles;
	}

}
