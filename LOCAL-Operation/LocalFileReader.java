/**
 * @description 本地文件操作类
 * @author xujifu
 * @time 2016-06-12
 */
package com.netentsec.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.netentsec.decoder.BaseDecoder;
import com.netentsec.decoder.DecoderFactory;
import com.netentsec.tools.Configurations;
import com.netentsec.util.DecoderUtil;

public class LocalFileReader {
	
	public static Logger logger = Logger.getLogger(LocalFileReader.class);
	
	    /**
	     * 将内容写入本地文件
	     * @param filename
	     * @param content
	     */
	    public void WriteFile(String filename, String content) {
		try (BufferedWriter bw = new BufferedWriter(
		    new OutputStreamWriter(new FileOutputStream(filename), "UTF-8"))) {
		    if (filename == null || filename.equals("")) {
			logger.info("filename is empty");
			return;
		    }
		    File file = new File(filename);
		    if (!file.exists() || file.isDirectory()) {
			logger.info(filename + " file is not exists!");
		    }
		    bw.write(content);
		    bw.flush();
		    bw.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }
	/**
	 * 读取本地文件，将文件内容返回
	 * @param filename
	 * @return
	 */
	public String ReadFile(String filename){
		try {
			if(filename == null || filename.equals("")){
				System.out.println("filename is null!");
				return null;
			}
			File file = new File(filename);
			if(!file.exists() || file.isDirectory()){
				System.out.print(filename + " file is not exists!");
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename),"UTF-8"));
			
			String temp = null;
			StringBuffer sb = new StringBuffer();
			temp = br.readLine();
			while(temp != null){
				sb.append(temp);
				temp= br.readLine();
			}
			return new String(sb.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 创建文件夹
	 * 
	 * @param filepath 文件夹路径
	 * @return
	 */
	public boolean mkdirFolder(String filepath){
		boolean result = false;
		try {  
            File file = new File(filepath.toString());  
            if (!file.exists()) {  
            	result = file.mkdirs();  
            }
            result = true;
        } catch (Exception e) {  
            logger.error("新建目录操作出错" + e.getLocalizedMessage());  
            e.printStackTrace();
            return result;
        }  
		return result;
	}
	
	public boolean mkParentDir(String filepath){
		File file = new File(filepath.toString());
		return mkdirFolder(file.getParent());
	}
	
	/**
	 * 删除文件
	 * 
	 * @param filepath
	 * @return
	 */
	public boolean removeFile(String filepath){
		boolean result = false;
		if(filepath == null || "".equals(filepath)){
			return result;
		}
		File file = new File(filepath);
		if(file.isFile() && file.exists()){
			result = file.delete();
			if (result == Boolean.TRUE) {  
                logger.debug("[REMOE_FILE:" + filepath + "删除成功!]");
            } else {  
                logger.debug("[REMOE_FILE:" + filepath + "删除失败]");
            } 
		}
		return result;
	}
	
	/** 文件重命名 ，重命名名称为{原文件名}-{日期时间}.err
	 * @param path 文件目录 
	 */ 
	public void renameFile(String filepath){ 
		Date now = new Date();
		SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyyMMddHHmmss");//可以方便地修改日期格式
		String current = datetimeFormat.format(now);
		
        File oldfile=new File(filepath); 
        File newfile=new File(filepath+"-"+current + ".old"); 
        if(!oldfile.exists()){
            return;//重命名文件不存在
        }
        if(newfile.exists()){//若在该目录下已经有一个文件和新文件名相同，则不允许重命名 
            System.out.println(filepath+"-"+current + ".old" + "已经存在！");
        	logger.debug(filepath+"-"+current + ".old" + "已经存在！");
        }else{ 
            oldfile.renameTo(newfile); 
        }
	}
	
	public void fileWriter(List<String> content, String filepath){
		FileWriter fw = null;
		try {
			fw = new FileWriter(filepath);
			for(String line : content){
				fw.write(line);
			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if(null != fw){
				try {
					fw.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}
	
		public boolean writeSingleSerializable(Serializable serializable, String uri){
		File file = new File(uri);
		file.delete();
		mkParentDir(uri);
		FileOutputStream foStream = null;
		ObjectOutputStream ooStream = null;
		try {
			foStream = new FileOutputStream(file, true);
			ooStream = new ObjectOutputStream(foStream);
			file.createNewFile();
			ooStream.writeObject(serializable);
			if(ooStream != null) ooStream.close();
			if(foStream != null) foStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public Serializable readSingleSerializable(String uri){
		File file = new File(uri);
		Serializable ct = null;
		FileInputStream fiStream = null;
		ObjectInputStream oiStream = null;
		if(file.exists()){
			try {
				fiStream = new FileInputStream(file);
				oiStream = new ObjectInputStream(fiStream);
				while(fiStream.available() > 0){
					ct = (Serializable)oiStream.readObject();
					return ct;
				}
				oiStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
		return ct;
	}
	
		/**
	 * 多次将序列化类写入同一文件
	 * @param ct
	 * @param filepath
	 * @return
	 */
	public boolean writeSerializable(Serializable ct, String filepath){
		System.out.println(filepath);
		File file = new File(filepath);
		FileOutputStream foStream = null;
		ObjectOutputStream ooStream = null;
		try {
			
			if(file.exists()){
				foStream = new FileOutputStream(file, true);
				ooStream = new ObjectOutputStream(foStream);
				long pos = foStream.getChannel().position() - 4;
				foStream.getChannel().truncate(pos);
				ooStream.writeObject(ct);
			}else{
				foStream = new FileOutputStream(file, true);
				ooStream = new ObjectOutputStream(foStream);
				file.createNewFile();
				ooStream.writeObject(ct);
			}
			if(ooStream != null) ooStream.close();
			if(foStream != null) foStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				if(ooStream != null)ooStream.close();
				if(foStream != null)foStream.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				if(ooStream != null)ooStream.close();
				if(foStream != null)foStream.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return false;
		}
		return true;
	}
	
	/**
	 * 从一个文件中读取多个序列化类
	 * @param filepath
	 * @return
	 */
	public ArrayList<Serializable> readSerializable(String filepath){
		ArrayList<Serializable> list = new ArrayList<Serializable>();
		File file = new File(filepath);
		FileInputStream fiStream = null;
		ObjectInputStream oiStream = null;
		if(file.exists()){
			try {
				fiStream = new FileInputStream(file);
				oiStream = new ObjectInputStream(fiStream);
				while(fiStream.available() > 0){
					Serializable ct = (Serializable)oiStream.readObject();
					list.add(ct);
				}
				oiStream.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
		return list;
	}
	
	/**
	 * 获取文件属性，包括文件名、最后修改时间、文件路径、文件大小信息
	 * @param filepath，绝对路径
	 * @return
	 */
	public FileProperties getFileProperties(String filepath){
		FileProperties fp = new FileProperties();
		File file = new File(filepath);
		fp.lastModifyDate = file.lastModified();
		fp.fileName = file.getName();
		fp.fileSize = file.length();
		fp.absolutePath = file.getAbsolutePath();
		fp.absoluteFile = file.getPath();
		return fp;
	}
	
	/**
	 * 解码Mail日志附件中的内容
	 * @param filepath
	 * @param offset
	 * @param length
	 * @return
	 */
	public String TransCoderMail(String filepath, String offset, String length){
		DecoderFactory df = new DecoderFactory();
        BaseDecoder decoder = df.createDecoder("mailDecoder()");
		String content = "";
		File f = new File(filepath);
        if(!f.exists() || f.isDirectory()){
        	logger.error("filepath is not exists or directory, " + filepath);
        	return "";
        }
		try{
			content = DecoderUtil.FileUtil.readString(filepath);
		}
		catch (Exception e){
			e.printStackTrace();
			logger.error(e.getMessage());
			return "";
		}

		String actual = decoder.decode(content.getBytes(), Integer.parseInt(offset), Integer.parseInt(length));
		return actual;
	}
	
	/**
	 * 解码Web附件内容
	 * @param filepath
	 * @param transfer_encoding
	 * @param content_encoding
	 * @param contentType
	 * @param offset
	 * @param length
	 * @return
	 */
	public String TransCoderWeb(String filepath, String transfer_encoding, 
			String content_encoding, String contentType,
			String offset, String length){
		DecoderFactory df = new DecoderFactory();
        BaseDecoder decoder = df.createDecoder("webDecoder(transfer_encoding,content_encoding,contentType)");
        File f = new File(filepath);
        if(!f.exists() || f.isDirectory()){
        	return "";
        }
        long fileLen = f.length();
        byte[] buffer = new byte[(int) fileLen];
        try {
            RandomAccessFile raf = new RandomAccessFile(f, "r");
            raf.seek(0);
            raf.read(buffer, 0, (int) fileLen);
            raf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace(); 
            return "";
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

		List<String> argValues = new ArrayList<String>();
		argValues.add(transfer_encoding);
		argValues.add(content_encoding);
        argValues.add(contentType);
		
		String actual = decoder.decode(buffer, Integer.parseInt(offset), Integer.parseInt(length), argValues);

		return actual;
	}
	
	/**
	 * 解码post附件内容
	 * @param filepath
	 * @param behaviour_id
	 * @param signature
	 * @param offset
	 * @param length
	 * @return
	 */
	public String TransCodePost(String filepath, String behaviour_id, String signature, String offset, String length){
        DecoderFactory df = new DecoderFactory();
        BaseDecoder decoder = df.createDecoder("postDecoder(behaviour_id,signature)");

		String content = "";
		File f = new File(filepath);
        if(!f.exists() || f.isDirectory()){
        	return "";
        }
		try{
			content = DecoderUtil.FileUtil.readString(filepath);
		}
		catch (Exception e){
            e.printStackTrace(); 
		}

		List<String> argValues = new ArrayList<String>();
		/*
		 * 0：UNKNOWN
		 * 1：login
		 * 2:post
		 * 3:vote
		 * 4:nbLogin
		 * 5:nbPost
		 * 6:otherPost
		 */
		String behavior = "";
		switch(Configurations.PostBehavior.toFunction(behaviour_id.toUpperCase())){
		case LOGIN:
			behavior = "1";
			break;
		case POST:
			behavior = "2";
			break;
		case VOTE:
			behavior = "3";
			break;
		case NBLOGIN:
			behavior = "4";
			break;
		case NBPOST:
			behavior = "5";
			break;
		case OTHERPOST:
			behavior = "6";
			break;
		case UNKNOWN:
			behavior = "0";
			break;
		}
        argValues.add(behavior);
		argValues.add(signature);

		String actual = decoder.decode(content.getBytes(), Integer.parseInt(offset), Integer.parseInt(length),
				argValues);
		//System.out.println("actual="+actual);
		return actual;
	}
	
	/**
	 * 解码url附件内容
	 * @param filepath
	 * @return
	 */
	public String TransCodeUrl(String filepath){
        DecoderFactory df = new DecoderFactory();
        BaseDecoder decoder = df.createDecoder("urlDecoder(uri_domain)");

        String value = "/billboard/pushlog/";
        byte[] byteValue = value.getBytes();

        if (!decoder.withArgument()) {
            value = decoder.decode(byteValue, 0,
                    byteValue.length);
        } else {
            String uri_domain = "http://tieba.baidu.com";

            List<String> argValues = new ArrayList<String>();
            argValues.add(uri_domain);

            value = decoder.decode(byteValue, 0,
                    byteValue.length, argValues);
        }
        //System.out.println("value=" + value);
        return value;
	}
	
	/**
	 * 解码telnet附件内容
	 * @param filepath
	 * @param offset
	 * @param length
	 * @return
	 */
	public String TransCodeTelnet(String filepath, String offset, String length){
		String content = "";
		File f = new File(filepath);
        if(!f.exists() || f.isDirectory()){
        	return "";
        }
		try{
			content = DecoderUtil.FileUtil.readString(filepath);
		}
		catch (Exception e){
            e.printStackTrace(); 
		}
        BaseDecoder decoder = DecoderFactory.createDecoder("telnetDecoder()");
        String actual = decoder.decode(content.getBytes(), Integer.parseInt(offset), Integer.parseInt(length));
        return actual;
	}
	
	/**
	 * 其他文件读取
	 * @param filepath
	 * @param offset
	 * @param length
	 * @return
	 */
	public String TransCodeOthers(String filepath, String offset, String length){
        File f = new File(filepath);
        if(!f.exists() || f.isDirectory()){
        	return "";
        }
        byte[] buffer = new byte[Integer.parseInt(length)];
        String content = "";
        try {
            RandomAccessFile raf = new RandomAccessFile(f, "r");
            raf.seek(Integer.parseInt(offset));
            raf.read(buffer, 0, Integer.parseInt(length));
            raf.close();
            content = new String(buffer, "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace(); 
            return "";
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        
        return content;
	}
	
	public static void main(String[] args){
		LocalFileReader lfr = new LocalFileReader();
//		String split = "";
//		
//		String result = "";
//		char c = 17;
//		char[] strChar = String.valueOf(c).toCharArray();
//		for(int i = 0; i < strChar.length; i ++){
//			result += Integer.toBinaryString(strChar[i]) + " ";
//		}
//		System.out.println("result:" + result + ":" + c);
//		String[] a = lfr.ReadFile("C:/Users/admin/Desktop/ns_data/ns_data/nslcdbload/work/work1/web_1462962959_12222.rpl").split(String.valueOf((char)17));
//		System.out.println(a[0] + " " + a[1]);
//		String file_path = "/usr/local/nswcf/data/logcenter/ns_web/2016/05/11/archive-786636359-65-1.314.165";
//		System.out.println(file_path.substring(file_path.lastIndexOf(".")+1));
//		String[] tmp = file_path.split("\\.");
//		System.out.println(tmp[2]);
		//lfr.TransCoderWeb(filepath, transfer_encoding, content_encoding, contentType, offset, length);
		System.out.println(lfr.TransCoderWeb("D:/archive-1799940691-65-1", "", "gzip", "text/html", "81114", "36"));
//		System.out.println(lfr.TransCodeOthers("D:/logtype.conf", "5", "10"));
	}
}
