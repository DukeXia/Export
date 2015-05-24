/*
 * Copyright 2015 Petasoft Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.petasoft.export.util.zip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

/**
 * @author ���� 2015-05-15
 */
public class Gzip {
	/**
	 * 
	 * ��һ���ļ��鵵
	 * 
	 * @param sources
	 *            Ҫ�鵵��ԭ�ļ�����
	 * @param target
	 *            �鵵����ļ�
	 * @return File ���ع鵵����ļ�
	 * @throws Exception
	 */
	public static File tar(File[] sources, File target) throws Exception {
		FileOutputStream out = new FileOutputStream(target);
		TarArchiveOutputStream os = new TarArchiveOutputStream(out);
		for (File file : sources) {
			os.putArchiveEntry(new TarArchiveEntry(file));
			IOUtils.copy(new FileInputStream(file), os);
			os.closeArchiveEntry();
		}
		if (os != null) {
			os.flush();
			os.close();
		}
		return target;
	}

	/**
	 * ��һ���鵵�ļ���鵵
	 * 
	 * @param source
	 *            �鵵�ļ�
	 * @param target
	 *            ��鵵����ļ����·��
	 * @throws Exception
	 */
	public static void untar(File source, File target) throws Exception {
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		ArchiveInputStream ain = new ArchiveStreamFactory()
				.createArchiveInputStream("tar", new FileInputStream(source));
		bis = new BufferedInputStream(ain);
		TarArchiveEntry entry = (TarArchiveEntry) ain.getNextEntry();
		while (entry != null) {
			String name = entry.getName();
			String[] names = name.split("/");
			String fileName = target.getName();
			for (int i = 0; i < names.length; i++) {
				String str = names[i];
				fileName = fileName + File.separator + str;
			}
			if (name.endsWith("/")) {
				File file = new File(fileName);
				if (!file.exists()) {
					file.mkdirs();
				}
			} else {
				File file = new File(fileName);
				File parent = file.getParentFile();
				if (!parent.exists()) {
					parent.mkdirs();
				}
				if (!file.exists()) {
					file.createNewFile();
				}
				bos = new BufferedOutputStream(new FileOutputStream(file));
				int b;
				while ((b = bis.read()) != -1) {
					bos.write(b);
				}
				bos.flush();
				bos.close();
			}
			entry = (TarArchiveEntry) ain.getNextEntry();
		}
		if (bis != null) {
			bis.close();
		}
	}

	/**
	 * ʹ��gzipѹ��һ���ļ�
	 * 
	 * @param source
	 *            Ҫѹ�����ļ�
	 * @param target
	 *            ѹ������ļ����·��
	 * @param delflag
	 *            ѹ�����Ƿ�ɾ��Դ�ļ�
	 * @return ����ѹ������ļ�
	 * @throws Exception
	 */
	public static File gzip(File source, File target, boolean delflag)
			throws Exception {
		if (!target.exists()) {
			target.mkdirs();
		}
		target = new File(target.getAbsolutePath() + File.separator
				+ source.getName() + ".gz");
		FileInputStream in = null;
		GZIPOutputStream out = null;
		in = new FileInputStream(source);
		out = new GZIPOutputStream(new FileOutputStream(target));
		byte[] array = new byte[1024];
		int number = -1;
		while ((number = in.read(array, 0, array.length)) != -1) {
			out.write(array, 0, number);
		}
		if (in != null) {
			in.close();
		}
		if (out != null) {
			out.close();
		}
		if (delflag) {
			source.delete();
		}
		return target;
	}

	/**
	 * ��ѹһ��gzipѹ�����ļ�
	 * 
	 * @param source
	 *            ��Ҫ��ѹ��gzip��ѹ���ļ�
	 * @param target
	 *            ��ѹ����ļ����·��
	 * @param delflag
	 *            ��ѹ���Ƿ�ɾ��Դ�ļ�
	 * @throws Exception
	 */
	public static void gunzip(File source, File target, boolean delflag)
			throws Exception {
		ArchiveInputStream ain = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;

		GZIPInputStream is = new GZIPInputStream(new BufferedInputStream(
				new FileInputStream(source)));
		ain = new ArchiveStreamFactory().createArchiveInputStream("tar", is);
		bis = new BufferedInputStream(ain);
		TarArchiveEntry entry = (TarArchiveEntry) ain.getNextEntry();
		while (entry != null) {
			String name = entry.getName();
			String[] names = name.split("/");
			String fileName = target.getName();
			for (int i = 0; i < names.length; i++) {
				String str = names[i];
				fileName = fileName + File.separator + str;
			}
			if (name.endsWith("/")) {
				File file = new File(fileName);
				if (!file.exists()) {
					file.mkdirs();
				}
			} else {
				File file = new File(fileName);
				File parent = file.getParentFile();
				if (!parent.exists()) {
					parent.mkdirs();
				}
				if (!file.exists()) {
					file.createNewFile();
				}
				bos = new BufferedOutputStream(new FileOutputStream(file));
				int b;
				while ((b = bis.read()) != -1) {
					bos.write(b);
				}
				bos.flush();
				bos.close();
			}
			entry = (TarArchiveEntry) ain.getNextEntry();
		}
		if (bis != null) {
			bis.close();
		}
		if (delflag) {
			source.delete();
		}
	}

	public static void main(String[] args) {
		File[] sources = new File[] { new File("conf/mysql-db-site.xml"),
				new File("conf/mysql-export-site.xml") };
		File target = new File("mysql.tar");
		try {
			// tar(sources, target);
			File zip = gzip(tar(sources, target), new File("gzip/"), false);
			// gzip(new File("conf/mysql-export-site.xml"), new File("gzip/"));
			gunzip(zip, new File("gunzip/"), false);
			// untar(target, new File("untar/"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
