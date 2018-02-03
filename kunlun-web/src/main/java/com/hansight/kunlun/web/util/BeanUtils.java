package com.hansight.kunlun.web.util;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;

public class BeanUtils {
	
	/**
	 * 复制bean
	 * 如果源bean中的变量为null则不赋值
	 * @param source
	 * @param target
	 * @throws Exception
	 */
	public static void copyExcludeNull(Object source,Object target) throws Exception{
		if(source == null || target == null) return;
		
		Class<?> tClazz = Class.forName(target.getClass().getName());
		Class<?> sClazz = Class.forName(source.getClass().getName());
		Field[] tFields = tClazz.getDeclaredFields();
		Field[] sFields = sClazz.getDeclaredFields();
		
		if(tFields == null || tFields.length == 0 || sFields == null || sFields.length == 0) return;
		
		for(Field tField:tFields){
			tField.setAccessible(true);
			for(Field sField:sFields){
				if(tField.getName().equals(sField.getName())){
					sField.setAccessible(true);
					if(sField.get(source) == null) break;
					setValue(tClazz,target,tField.getName(),sField.get(source));
				}
			}
		}
	}
	
	private static void setValue(Class<?> clazz,Object obj,String fieldName,Object val) throws Exception{
		
		String value = "";
		if(val != null) value = val.toString();
		
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
	    }else if(type.equals("java.util.List") || type.equals("java.util.ArrayList")
	    		|| type.equals("java.util.Set")){
	    	if(val != null){
	    		field.set(obj, val);
	    	}
	    }else{
	    		throw new Exception("===========BeanUtils参数类型错误!");
	    }
	}
	
	public static void main(String[] args){
	}

}
