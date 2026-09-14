package com.sist.web.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.servlet.http.Part;

public class FileUploadUtil {

	// 파일 하나 저장 (대표이미지용)
	public static String upload(String uploadPath, Part part) throws Exception {
		File dir = new File(uploadPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		if (part == null || part.getSize() == 0) {
			return "";
		}

		String originName = part.getSubmittedFileName();
		String saveName = System.currentTimeMillis() + "_" + originName;

		// part.write() 대신 스트림을 직접 복사해서 정확한 위치에 저장
		File targetFile = new File(dir, saveName);
		try (InputStream in = part.getInputStream();
		     OutputStream out = new FileOutputStream(targetFile)) {
			in.transferTo(out);
		}

		return saveName;
	}

	// 파일 여러 개 저장 (조리순서 이미지용)
	public static List<String> uploadMultiple(String uploadPath, Collection<Part> parts) throws Exception {
		List<String> savedNames = new ArrayList<>();
		File dir = new File(uploadPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		for (Part part : parts) {
			if (part.getSize() == 0)
				continue;
			if (part.getSubmittedFileName() == null || part.getSubmittedFileName().isEmpty())
				continue;

			String saveName = System.currentTimeMillis() + "_" + part.getSubmittedFileName();
			File targetFile = new File(dir, saveName);
			try (InputStream in = part.getInputStream();
			     OutputStream out = new FileOutputStream(targetFile)) {
				in.transferTo(out);
			}
			savedNames.add(saveName);
		}
		return savedNames;
	}
}