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
package com.petasoft.export.util;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;

/**
 * @author ���� 2015-05-15
 */
public class PubFun {
	public static String Version = "1.5.00";
	public static String Buildat = "2015-03-23";

	/**
	 * �ļ��ֶηָ�����˫�ֽڣ�7C1B
	 */
	public static String datSeparator = new String(new char[] { '|', '\033' });
	public static String tabSeparator = new String(new char[] { '\t' });
	public static String comSeparator = new String(new char[] { ',' });

	public static String procConcat = "|";
	public static String procSplit = "\\|";

	public static SimpleDateFormat dateHFormat = new SimpleDateFormat(
			"yyyy-MM-dd");
	public static SimpleDateFormat dateXFormat = new SimpleDateFormat(
			"yyyy/MM/dd");
	public static SimpleDateFormat shortFormat = new SimpleDateFormat(
			"yyyyMMdd");
	public static SimpleDateFormat timeFormat = new SimpleDateFormat(
			"yyyy-MM-dd hh:mm:ss");

	/**
	 * ��ӡ�汾��Ϣ
	 * 
	 * @param Program
	 * @return
	 */
	public static String getVersion(String Program) {
		return Program + " Version: " + Version + " Buildat: " + Buildat;
	}

	/**
	 * �滻����
	 * 
	 * @param str
	 *            �������ڸ�ʽ���ַ���
	 * @param date
	 *            �滻ΪĿ������
	 * @return
	 */
	public static String replaceDate(String str, String date) {
		try {
			String shortDate = date;
			shortDate = shortDate.replaceAll("-", "");
			shortDate = shortDate.replaceAll("/", "");
			String result = str;
			result = result.replace("YYYY-MM-DD",
					dateHFormat.format(shortFormat.parse(shortDate)));
			result = result.replace("YYYY/MM/DD",
					dateXFormat.format(shortFormat.parse(shortDate)));
			result = result.replace("YYYYMMDD", shortDate);
			return result;
		} catch (ParseException e) {
			return str;
		}
	}

	/**
	 * ��ȡ�쳣��Ϣ
	 * 
	 * @param e
	 * @return
	 */
	public static String getExceptionString(Exception e) {
		String str = "\n" + e.toString();
		StackTraceElement[] ste = e.getStackTrace();
		for (int iste = 0; iste < ste.length; ++iste) {
			str = str + "\n\tat " + ste[iste].toString();
		}
		return str + "\n";
	}

	/**
	 * ɾ���ļ�
	 * 
	 * @param filename
	 *            �ļ���
	 * @return
	 */
	public static int delFile(String filename) {
		File file = new File(filename);
		file.delete();
		return 1;
	}

	/**
	 * ѹ���ļ�
	 * 
	 * @param filename
	 *            �ļ���
	 * @return
	 */
	public static int zipFile(String filename) {
		File file = new File(filename);
		file.delete();
		return 1;
	}

	/**
	 * �滻����
	 * 
	 * @param props
	 * @param cmds
	 * @return
	 */
	public static String putParamToCommand(Properties props, String cmds) {
		int start = 0;
		int pos;
		int end = 0;
		while ((pos = cmds.indexOf('$', start)) >= 0) {
			end = pos;
			boolean encloser = false;
			pos++;
			if (cmds.charAt(pos) == '{') {
				encloser = true;
				++pos;
			}
			int varNameLen = 0;
			while (pos + ++varNameLen < cmds.length())
				if (!cmds.substring(pos + varNameLen, pos + varNameLen + 1)
						.matches("[a-zA-Z0-9_]"))
					break;
			String varName = cmds.substring(pos, pos + varNameLen);
			String val = props.getProperty(varName, "");
			pos += ((encloser) ? 1 + varNameLen : varNameLen);
			cmds = cmds.substring(0, end) + val + cmds.substring(pos);
			start = end + val.length();
		}
		return cmds;
	}

	/**
	 * ��ȡ�����ļ�
	 * 
	 * @param intercode
	 * @param incrflag
	 * @param time
	 * @param shortDate
	 *            YYYYMMDD
	 * @return
	 */
	public static String getDatFileName(String intercode, String incrflag,
			int time, String shortDate) {
		return replaceDate(
				String.format("YYYYMMDD_01_%s_%s_%3$02d.dat", new Object[] {
						intercode, incrflag, time }), shortDate);
	}

	/**
	 * ��ȡУ���ļ�
	 * 
	 * @param intercode
	 * @param incrflag
	 * @param time
	 * @param shortDate
	 *            YYYYMMDD
	 * @return
	 */
	public static String getChkFileName(String intercode, String incrflag,
			int time, String shortDate) {
		return replaceDate(
				String.format("YYYYMMDD_01_%s_%s_%3$02d.chk", new Object[] {
						intercode, incrflag, time }), shortDate);
	}

	/**
	 * ��ȡ��־�ļ�
	 * 
	 * @param intercode
	 * @param incrflag
	 * @param time
	 * @param shortDate
	 *            YYYYMMDD
	 * @return
	 */
	public static String getLogFileName(String intercode, String incrflag,
			Integer time, String shortDate) {
		return replaceDate(
				String.format("YYYYMMDD_01_%s_%s_%3$02d.log", new Object[] {
						intercode, incrflag, time }), shortDate);
	}
}
