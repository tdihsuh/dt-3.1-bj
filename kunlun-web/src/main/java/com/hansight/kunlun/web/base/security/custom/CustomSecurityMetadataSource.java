package com.hansight.kunlun.web.base.security.custom;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.util.AntPathMatcher;

import com.hansight.kunlun.web.base.user.entity.TAuthorityRequestmap;
import com.hansight.kunlun.web.base.user.entity.TRequestmap;
import com.hansight.kunlun.web.base.user.entity.TRole;
import com.hansight.kunlun.web.base.user.entity.TRoleAuthority;
import com.hansight.kunlun.web.base.user.service.TAuthorityRequestmapService;
import com.hansight.kunlun.web.base.user.service.TRequestmapService;
import com.hansight.kunlun.web.base.user.service.TRoleAuthorityService;
import com.hansight.kunlun.web.base.user.service.TRoleService;
import com.hansight.kunlun.web.base.user.service.vo.TAuthorityRequestmapQueryBean;
import com.hansight.kunlun.web.base.user.service.vo.TRequestmapQueryBean;
import com.hansight.kunlun.web.base.user.service.vo.TRoleAuthorityQueryBean;
import com.hansight.kunlun.web.util.Tools;

/**
 *  资源源数据定义，将所有的资源和权限对应关系建立起来，即定义某一资源可以被哪些角色访问
 * @author liukai
 *
 */
public class CustomSecurityMetadataSource implements FilterInvocationSecurityMetadataSource{
	
	
	private TRoleService tRoleService;
	
	private TRequestmapService tRequestmapService;
	
	private TAuthorityRequestmapService tAuthorityRequestmapService;
	 
	private TRoleAuthorityService tRoleAuthorityService; 
	
    /* 保存资源和权限的对应关系  key-资源url  value-权限 */  
    private Map<String,Collection<ConfigAttribute>> resourceMap = new HashMap<String,Collection<ConfigAttribute>>();   
    private AntPathMatcher urlMatcher = new AntPathMatcher();  
      
    public CustomSecurityMetadataSource(
    		TRoleService tRoleService,
    		TRequestmapService tRequestmapService,
    		TAuthorityRequestmapService tAuthorityRequestmapService,
    		TRoleAuthorityService tRoleAuthorityService){
    	this.tRoleService = tRoleService;
    	this.tRequestmapService = tRequestmapService;
    	this.tAuthorityRequestmapService = tAuthorityRequestmapService;
    	this.tRoleAuthorityService = tRoleAuthorityService;
        loadResourcesDefine();  
    }  
      
    @Override  
    public Collection<ConfigAttribute> getAllConfigAttributes() {  
        return null;  
    }  
  
    private void loadResourcesDefine(){
		/**
		 * 查询所有资源，封装每一资源都有哪些角色
		 */
    	List<TRequestmap> requestmapList = null;
    	Set<ConfigAttribute> configAttributes = null;
		List<TAuthorityRequestmap> arList = null;
		List<TRoleAuthority> raList = null;
		TRole role = null;
		ConfigAttribute configAttribute = null;
		
		try{
			TRequestmapQueryBean tRequestmapQueryBean = new TRequestmapQueryBean();
			tRequestmapQueryBean.setUseFlag(1L);
			requestmapList = tRequestmapService.list(tRequestmapQueryBean);
			
			for(TRequestmap requestmap:requestmapList){
				configAttributes = new HashSet<ConfigAttribute>();
				
				//查询哪些权限资源包含该请求
				TAuthorityRequestmapQueryBean tAuthorityRequestmapQueryBean = new TAuthorityRequestmapQueryBean();
				tAuthorityRequestmapQueryBean.setRequestmapId(requestmap.getId());
				arList = tAuthorityRequestmapService.list(tAuthorityRequestmapQueryBean);
				//查询哪些角色包含该请求
				for(TAuthorityRequestmap ar:arList){
					
					//查询哪些角色包含该权限
					TRoleAuthorityQueryBean tRoleAuthorityQueryBean = new TRoleAuthorityQueryBean();
					tRoleAuthorityQueryBean.setAuthorityId(ar.getAuthorityId());
					raList = tRoleAuthorityService.list(tRoleAuthorityQueryBean);
					
					for(TRoleAuthority ra:raList){						
						role = tRoleService.get(ra.getRoleId());
						
						configAttribute = new SecurityConfig(role.getName());
						configAttributes.add(configAttribute);
					}
				}
				
				resourceMap.put(requestmap.getUrl(), configAttributes);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
    }  
    /*  
     * 根据请求的资源地址，获取它所拥有的权限 
     */  
    @Override  
    public Collection<ConfigAttribute> getAttributes(Object obj)  
            throws IllegalArgumentException {  
        //获取请求的url地址  
        String requestUrl = ((FilterInvocation)obj).getRequestUrl();
        if(requestUrl.indexOf("?")!=-1){
        	requestUrl = requestUrl.substring(0, requestUrl.indexOf("?"));  
        }
          
        Iterator<String> it = resourceMap.keySet().iterator();  
        while(it.hasNext()){
            String url = it.next();
            /**
            if(urlMatcher.match(requestUrl,url))  {
                return resourceMap.get(requestUrl);
            }**/
            if(Tools.compareUrl(requestUrl, url))  {
                return resourceMap.get(url);
            }
        }  
        return null;  
    }  
  
    @Override  
    public boolean supports(Class<?> arg0) {    
        return true;  
    }

}
