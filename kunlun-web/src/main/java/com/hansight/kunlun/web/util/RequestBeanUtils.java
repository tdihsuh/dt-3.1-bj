package com.hansight.kunlun.web.util;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

//import com.japhone.entity.User;

public class RequestBeanUtils {
	
	public static void copy(Object obj,HttpServletRequest request) throws Exception{
		
		setValue(obj,request);
	}
	
	private static void setValue(Object obj,HttpServletRequest request) throws Exception{
		
		Enumeration<?> enums = request.getParameterNames(); 
		if(enums == null) return;
		
		Class<?> clazz = Class.forName(obj.getClass().getName());
		Field[] fields = clazz.getDeclaredFields();
		if(fields == null || fields.length == 0) return;
		String value;
		String fieldName;
		String paramName;
		
		for(Field field:fields){
			fieldName = field.getName();
			enums = request.getParameterNames(); 
			while(enums.hasMoreElements()){
				paramName = String.valueOf(enums.nextElement());
				if(fieldName.equals(paramName)){
					value = request.getParameter(fieldName);
					setValue(clazz,obj,fieldName,value);
				}
			}
		}
	}
	
	private static void setValue(Class<?> clazz,Object obj,String fieldName,String value) throws Exception{
		Field field = clazz.getDeclaredField(fieldName);
		field.setAccessible(true);
		
		String type = field.getType().getName(); 
		if(type.equals("int") || type.equals("java.lang.Integer")){  
	        if(value!=null && !value.equals("")){ 
	        	field.set(obj,Integer.parseInt(value));  
	        }  
	    }else if(type.equals("double") || type.equals("java.lang.Double")){  
	        if(value!=null && !value.equals("")){
	        	field.set(obj,Double.valueOf(value));  
	        }  
	    }else if(type.equals("float") || type.equals("java.lang.Float")){  
	        if(value!=null && !value.equals("")){  
	        	field.set(obj,Float.valueOf(value));  
	        }  
	    }else if(type.equals("char") || type.equals("java.lang.Character")){  
	        if(value!=null && !value.equals("")){  
	        	field.set(obj,Character.valueOf(value.charAt(0)));  
	        }  
	    }else if(type.equals("java.util.Date")){  
	        if(value!=null && !value.equals("")){  
	            String format = value.length()==10?"yyyy-MM-dd":"yyyy-MM-dd HH:mm:ss";  
	            SimpleDateFormat sdf = new SimpleDateFormat(format);
	            field.set(obj,sdf.parse(value));  
	        }                  
	    }else if(type.equals("java.lang.String")){
	    	field.set(obj,value);
	    }else if(type.equals("java.lang.Long")){
	    	if(value!=null && !value.equals("")){  
	        	field.set(obj,Long.valueOf(value));  
	        }
	    }else{
	    		throw new Exception("===========request参数类型绑定错误!");
	    }
	}
	
	public static void main(String args[]){
		try{
//			User user = new User();
//			Class<?> clazz = Class.forName(user.getClass().getName());
//			Field[] fields = clazz.getDeclaredFields();
//			System.out.println(fields.length);
//			for(Field field:fields){
//				System.out.println(field.getName() + ":" +field.getType().getName());
//			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
