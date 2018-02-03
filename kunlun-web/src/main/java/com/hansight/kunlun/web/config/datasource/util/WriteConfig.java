package com.hansight.kunlun.web.config.datasource.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletResponse;

public class WriteConfig {
	/***生成agent文件***/
	 public static boolean agentWrite(String agentId,String path, String file_Name){
			//System.out.println("id = " + agentId+""+file_Name);
			boolean b = false;
			FileOutputStream fos = null;
			try {
				String fileName =path()+ path+file_Name+".conf";
				File f = new File(fileName);
				if(f.exists()){
					byte[] by = String.valueOf(agentId).getBytes();
					fos = new FileOutputStream(fileName, false);
					fos.write(by);
				}else{
					f.createNewFile();
					byte[] by = String.valueOf(agentId).getBytes();
					fos = new FileOutputStream(fileName, false);
					fos.write(by);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					fos.close();
					b = true;
				} catch (Exception e) {
				}
			}
			return b;
		}
	public static void downLoad(String path ,String fileName,HttpServletResponse response) throws Exception{
		String filePath = WriteConstants.PATH+fileName+".conf";
		//System.out.println("filePath::::"+filePath);
		File file = new File(path()+filePath);
		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		OutputStream out = null;
		response.setContentType("application/x-download");
		String str = file.getName();
		response.setHeader("Content-disposition", "attachment; filename="
				+ new String(str.getBytes("UTF-8"), "UTF-8"));
		try {
			bis = new BufferedInputStream(fis);
			out = response.getOutputStream();
			bos = new BufferedOutputStream(out);
			byte[] buff = new byte[32];
			while (-1 != (bis.read(buff, 0, buff.length))) {
				bos.write(buff, 0, buff.length);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			bis.close();
			fis.close();
			bos.close();
			out.close();
		}
	} 
	
	
	public static String path(){
		String root = null;
		try {
			root = Thread.currentThread().getContextClassLoader().getResource("").toURI().getPath();
			if(root.indexOf("WEB-INF")>0){  
				root = root.substring(0,root.indexOf("/WEB-INF/"));  					   
			}else{ 
			
			}
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return root;
	}
	 public static boolean delAllFile(String path) {
	       boolean flag = false;
	       File file = new File(path);
//	       if (!file.exists()) {
//	         return flag;
//	       }
	       if (!file.isDirectory()) {
	         return flag;
	       }
	       String[] tempList = file.list();
	       File temp = null;
	       for (int i = 0; i < tempList.length; i++) {
	          if (path.endsWith(File.separator)) {
	             temp = new File(path + tempList[i]);
	          } else {
	              temp = new File(path + File.separator + tempList[i]);
	          }
	          if (temp.isFile()) {
	             temp.delete();
	          }
	          if (temp.isDirectory()) {
	             delAllFile(path + "/" + tempList[i]);//先删除文件夹里面的文件
	             flag = true;
	          }
	
	       }
	       return flag;
	 }
	
	
	public static void main(String[] args) throws URISyntaxException {
		String root1 = Thread.currentThread().getContextClassLoader().getResource("").toURI().getPath();
		if(root1.indexOf("target")>0){  
			root1 = root1.substring(0,root1.indexOf("/target/"));  					   
		}else{ 
			
		}
		System.out.println(root1.substring(1, root1.length()));
		//String imgPath =root+ Constants.IMAGE_PATH+Constants.NETWORK_PATH;
	}

}
