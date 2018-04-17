/**
 * @description 存储文件属性信息
 * @author xujifu
 * @time 2016-06-12
 */
package com.netentsec.file;

import java.util.Date;

public class FileProperties {
	public boolean isDirectory;
	public String absoluteFile;//相对路径
	public String absolutePath;//绝对路径
	public long lastModifyDate;//最后修改时间
	public String fileName;//文件名称
	public long fileSize;//文件大小
}
