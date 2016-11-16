package com.jiabo.letty.mvc.util;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class IOUtil {
	public static String readToString(InputStream stream) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int i = 0;
		byte[] bytes = new byte[1024];
		while ((i = stream.read(bytes)) > -1) {
			bos.write(bytes, 0, i);
		}
		bos.close();
		stream.close();
		return new String(bos.toByteArray());
	}

	public static String readToString(String file) throws IOException {
		FileInputStream stream = new FileInputStream(file);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int i = 0;
		byte[] bytes = new byte[1024];
		while ((i = stream.read(bytes)) > -1) {
			bos.write(bytes, 0, i);
		}
		bos.close();
		stream.close();
		return new String(bos.toByteArray());
	}
}
