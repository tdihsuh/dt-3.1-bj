package com.hansight.kunlun.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
	public static String readCP(String name) throws IOException {
		InputStream in = FileUtils.class.getClassLoader().getResourceAsStream(
				name);
		try {
			return read(in);
		} finally {
			CloseUtils.close(in);
		}
	}

	public static String read(InputStream in) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		StringBuffer sb = new StringBuffer();
		int offset = 0;
		char[] cbuf = new char[1024];

		while ((offset = br.read(cbuf)) != -1) {
			sb.append(cbuf, 0, offset);
		}
		CloseUtils.close(br);
		return sb.toString();
	}

	public static String readTxt(String name) throws IOException {
		InputStream in = null;
		try {
			in = new FileInputStream(name);
			return read(in);
		} finally {

			CloseUtils.close(in);
		}
	}
	public static List<File> getAllFile(String path){
		if(path == null || path.trim().equals("")) return null;

		List<File> fileList = new ArrayList<File>();

		File folder = new File(path);
		if(folder.isDirectory()){
			String[] arr;
			String folderPath;
			File dir;
			arr = folder.list();
			if(arr != null && arr.length > 0){
				for(String f:arr){
					folderPath = path + "/" + f;
					dir = new File(folderPath);
					if(dir.isDirectory()){
						fileList.addAll(getFile(dir));
					}else if(dir.isFile()){
						fileList.add(dir);
					}
				}
			}
		}else if(folder.isFile()){
			fileList.add(folder);
		}

		return fileList;
	}

	public static List<File> getFile(File folder){
		if(folder == null) return null;

		List<File> fileList = new ArrayList<File>();
		if(folder.isDirectory()){
			String[] arr;
			String folderPath;
			File dir;
			arr = folder.list();
			if(arr != null && arr.length > 0){
				for(String f:arr){
					folderPath = folder.getAbsolutePath() + "/" + f;
					dir = new File(folderPath);
					if(dir.isDirectory()){
						fileList.addAll(getFile(dir));
					}else if(dir.isFile()){
						fileList.add(dir);
					}
				}
			}
		}else if(folder.isFile()){
			fileList.add(folder);
		}
		return fileList;
	}

	public static void main(String[] args){
//		String path = "D:/test/template/src";
		String path = "D:/test/template/WebRoot/template";
		List<File> list = getAllFile(path);
		for(File file:list){
			System.out.println(file.getName());
		}
	}
}
