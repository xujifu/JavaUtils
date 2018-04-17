/**
 * @description 扫描目录，返回文件列表
 * @author xujifu
 * @time 2016-06-12
 */
package com.netentsec.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class DirScanning {
	
	public static Logger logger = Logger.getLogger(DirScanning.class);
	private SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public String PathAnalysis(String path){
		String name = "";
		String systemos = System.getProperties().getProperty("os.name");
		if(systemos.indexOf("Windos") != -1){
			if(path == "" || path == null || path.length() == 0)
				return name;
			String[] tmp = path.split("\\\\");
		}else{
			if(path == "" || path == null || path.length() == 0)
				return name;
			String[] tmp = path.split("/");
			name = tmp[tmp.length - 1];
		}
		return name;
	}
	
	/**
	 * 扫描某目录下的所有目录，删除空目录
	 * @param path
	 * @return
	 */
	public boolean cleanEmptyDir(String path){
		File f = new File(path);
		if(!f.isDirectory()){
			return false;
		}else{
			File[] t = f.listFiles();
			for(File file : t){
				if(file.isDirectory()){
					if(file.listFiles() == null || file.listFiles().length <= 0){
						file.delete();
					}else{
						if(cleanEmptyDir(file.getPath())){
							if(file.listFiles() == null || file.listFiles().length <= 0){
								file.delete();
							}
						}
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * 扫描某目录下的文件,返回相对路径列表
	 * @param path
	 * @param path_tmp
	 * @return
	 */
	public List<String> dirscan(String path, String path_tmp){
    	List<String> resourcename = new ArrayList<String>();
    	File f = new File(path);
    	if(!f.isDirectory()){
    		return null;
    	}else{
//    		logger.info("begin scanning!");
//    		Date scandate = new Date();
//			logger.info("当前扫描时间为 : " + datetimeFormat.format(scandate));
    		File[] t = f.listFiles();
    		for(File file:t){
    			if(!file.isDirectory()){
    				if(pathAddRule(path_tmp + "/" + file.getName())){
    					resourcename.add(path_tmp + "/" + file.getName());
    				}
    			}else{
    				if(pathAddRule(path_tmp + "/" + file.getName())){
	    				List<String> tmp = dirscan(path + "/" + file.getName(), path_tmp + "/" + file.getName());
	    				if(tmp != null){
		    				for(String tmpname:tmp){
		    					resourcename.add(tmpname);
		    				}
	    				}
    				}
    			}
    		}
    		return resourcename;
    	}
    }
	
	public boolean pathAddRule(String path){
		if(path.indexOf("current_dir") != -1 ||
				path.indexOf(".conf") != -1 ||
				path.indexOf(".exit") != -1 ||
				path.indexOf(".pid") != -1 ||
				path.indexOf(".err") != -1 ||
				path.indexOf(".old") != -1 ){
			return false;
		}
		return true;
	}
}
