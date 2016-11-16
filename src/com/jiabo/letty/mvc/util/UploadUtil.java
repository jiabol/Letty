package com.jiabo.letty.mvc.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UploadUtil {
	private static final Logger log = LoggerFactory.getLogger(UploadUtil.class);

	public static List<FileItem> getFiles(HttpServletRequest request) {
		List<FileItem> list = new ArrayList<FileItem>();
		DiskFileItemFactory diskFactory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(diskFactory);
		List<FileItem> fileItems;
		try {
			fileItems = upload.parseRequest(request);

			Iterator<FileItem> iter = fileItems.iterator();
			while (iter.hasNext()) {
				FileItem item = (FileItem) iter.next();
				if (item.isFormField())
					continue;
				list.add(item);
			}
		} catch (FileUploadException e) {
			log.error("", e);
		}
		return list;
	}

	public static String getFileName(FileItem item) {
		String filename = item.getName();
		try {
			filename = URLDecoder.decode(filename, "utf-8");
		} catch (UnsupportedEncodingException e) {
			log.error("", e);
		}
		int index = filename.lastIndexOf("\\");
		filename = filename.substring(index + 1, filename.length());
		return filename;
	}
}
