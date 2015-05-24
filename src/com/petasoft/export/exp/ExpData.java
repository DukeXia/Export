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
package com.petasoft.export.exp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import com.petasoft.export.conf.Configuration;
import com.petasoft.export.util.FileUtil;
import com.petasoft.export.util.JdbcUtil;
import com.petasoft.export.util.PubFun;

/**
 * ���ݲ�ѯ��SQL��䵼�����ݡ�
 * 
 * @author ���� 2015-05-15
 */
public class ExpData extends Thread {

	private Configuration conf;

	private String prevDate;
	private String tranDate;
	private String nextDate;

	private String shortPrevDate;
	private String shortTranDate;
	private String shortNextDate;

	private String prevDate30;
	private String prevDate60;
	private String prevDate90;

	private String shortPrevDate30;
	private String shortPrevDate60;
	private String shortPrevDate90;

	private String sqlFile;
	private String logFile;

	private String sql;

	private int rows;
	private long size;

	public String intercode;
	public String incrflag;
	public String datFile;
	public String chkFile;

	/**
	 * intercode|srctable|incrflag
	 */
	public String key;
	public Integer time;
	/**
	 * [OK|ER]|recordRows|startTime|endTime|usedTime|datFile|chkFile
	 */
	public String status = "";

	/**
	 * ��ʼ����ʱ��
	 */
	private Calendar bgnCal;
	/**
	 * ���ý���ʱ��
	 */
	private Calendar endCal;

	public ExpData(Configuration conf, String key) {
		this.conf = conf;
		this.key = key;
		this.time = conf.expMap.get(key);
		bgnCal = Calendar.getInstance();
		endCal = Calendar.getInstance();

		try {
			if (!parseKey()) {
				writeFile("ж�������������");
			} else {
				writeFile("׼���������ݣ�" + key);
				writeFile("��Ӧ�����ļ���" + datFile);
				writeFile("��ӦУ���ļ���" + chkFile);
				start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		super.run();
		try {
			bgnCal.setTimeInMillis(System.currentTimeMillis());
			boolean ok = export();
			sendResponse(ok);
		} catch (Exception e) {
			sendResponse(false);
			try {
				writeFile(PubFun.getExceptionString(e));
			} catch (IOException e1) {
			}
		}
	}

	private void prepareDate(String runDate) {
		String[] tmpStr = runDate.split("-");
		Calendar curCal = Calendar.getInstance();
		curCal.set(Integer.parseInt(tmpStr[0]),
				Integer.parseInt(tmpStr[1]) - 1, Integer.parseInt(tmpStr[2]));
		Calendar cal = Calendar.getInstance();

		tranDate = PubFun.dateHFormat.format(curCal.getTime());

		cal.setTime(curCal.getTime());
		cal.add(Calendar.DATE, 1);
		nextDate = PubFun.dateHFormat.format(cal.getTime());

		cal.setTime(curCal.getTime());
		cal.add(Calendar.DATE, -1);
		prevDate = PubFun.dateHFormat.format(cal.getTime());

		cal.setTime(curCal.getTime());
		cal.add(Calendar.DATE, -30);
		prevDate30 = PubFun.dateHFormat.format(cal.getTime());

		cal.add(Calendar.DATE, -30);
		prevDate60 = PubFun.dateHFormat.format(cal.getTime());

		cal.add(Calendar.DATE, -30);
		prevDate90 = PubFun.dateHFormat.format(cal.getTime());

		shortPrevDate = prevDate.replaceAll("-", "");
		shortTranDate = tranDate.replaceAll("-", "");
		shortNextDate = nextDate.replaceAll("-", "");

		shortPrevDate30 = prevDate30.replaceAll("-", "");
		shortPrevDate60 = prevDate60.replaceAll("-", "");
		shortPrevDate90 = prevDate90.replaceAll("-", "");
	}

	private void sendResponse(boolean ok) {
		String flag;
		if (ok) {
			flag = "OK";
		} else {
			flag = "ER";
		}
		endCal.setTimeInMillis(System.currentTimeMillis());
		long diff = endCal.getTime().getTime() - bgnCal.getTime().getTime();
		status = String.format("%s|%d|%s|%s|%d|%s|%s", new Object[] { flag,
				rows, PubFun.timeFormat.format(bgnCal.getTime()),
				PubFun.timeFormat.format(endCal.getTime()), Long.valueOf(diff),
				datFile, chkFile });
	}

	/**
	 * ׼������
	 * 
	 * @return
	 * @throws IOException
	 */
	private boolean parseKey() throws IOException {
		String[] procInfo = key.split(PubFun.procSplit);

		intercode = procInfo[0];

		sqlFile = conf.get("export.core.sqlsDir") + "/" + intercode + ".sql";

		incrflag = procInfo[2].trim();

		datFile = PubFun.getDatFileName(intercode, incrflag, time,
				conf.shortDate);
		chkFile = PubFun.getChkFileName(intercode, incrflag, time,
				conf.shortDate);
		logFile = conf.get("export.core.logsDir")
				+ "/"
				+ PubFun.getLogFileName(intercode, incrflag, time,
						conf.shortDate);
		File file = null;

		file = new File(conf.get("export.core.dataDir") + "/" + datFile);
		if (file.exists()) {
			file.delete();
		}
		file = new File(conf.get("export.core.dataDir") + "/" + chkFile);
		if (file.exists()) {
			file.delete();
		}
		file = new File(logFile);
		if (file.exists()) {
			file.delete();
		}

		prepareDate(conf.tranDate);

		return true;
	}

	/**
	 * �滻���ڱ���
	 * 
	 * @param str
	 * @return
	 */
	private String replaceDate(String str) {
		Properties prop = new Properties();
		prop.put("TRAN_DATE", tranDate);
		prop.put("PREV_DATE", prevDate);
		prop.put("NEXT_DATE", nextDate);

		prop.put("TRANDATE", shortTranDate);
		prop.put("PREVDATE", shortPrevDate);
		prop.put("NEXTDATE", shortNextDate);

		prop.put("PRE30_DAYS", prevDate30);
		prop.put("PRE60_DAYS", prevDate60);
		prop.put("PRE90_DAYS", prevDate90);

		prop.put("PRE30DAYS", shortPrevDate30);
		prop.put("PRE60DAYS", shortPrevDate60);
		prop.put("PRE90DAYS", shortPrevDate90);

		return PubFun.putParamToCommand(prop, str);
	}

	/**
	 * ��ȡSQL�ļ�
	 * 
	 * @throws IOException
	 */
	private void readSqlFile() throws IOException {
		writeFile("��ȡSQL�ļ���" + sqlFile);
		String str = "";
		BufferedReader lstFile = new BufferedReader(new InputStreamReader(
				new FileInputStream(sqlFile)));
		String line = null;
		while ((line = lstFile.readLine()) != null) {
			str = str + line;
		}
		lstFile.close();
		sql = replaceDate(str);
		writeFile("�õ�SQL��䣺" + sql);
	}

	/**
	 * ж������
	 * 
	 * @return
	 * @throws Exception
	 */
	private boolean export() throws Exception {
		String data = "";
		int currentRows = 0;

		readSqlFile();

		DecimalFormat df0 = new DecimalFormat("0");
		DecimalFormat df1 = new DecimalFormat("0.0");
		DecimalFormat df2 = new DecimalFormat("0.00");
		DecimalFormat df3 = new DecimalFormat("0.000");
		DecimalFormat df4 = new DecimalFormat("0.0000");
		DecimalFormat df5 = new DecimalFormat("0.00000");
		DecimalFormat df6 = new DecimalFormat("0.000000");
		DecimalFormat df7 = new DecimalFormat("0.0000000");
		DecimalFormat df8 = new DecimalFormat("0.00000000");
		DecimalFormat df9 = new DecimalFormat("0.000000000");
		DecimalFormat dfA = new DecimalFormat("0.0000000000");

		String strTemp = "";

		rows = 0;

		if (sql.equals("")) {
			writeFile("SQL���Ϊ��");
			return false;
		}

		String[] sqls = sql.split(PubFun.procSplit);

		writeFile("�������ݿ⣺");
		writeFile("��" + sqls.length + "����ѯ");

		Connection connection = JdbcUtil.getConnection(conf);

		for (int idx = 0; idx < sqls.length; idx++) {
			writeFile("ִ�е�����ѯ" + (idx + 1) + "��" + sqls[idx]);
			PreparedStatement statement = connection
					.prepareStatement(sqls[idx]);
			int fetchSize = Integer.parseInt(conf.get("export.core.FetchSize"));
			if (fetchSize > 0) {
				statement.setFetchSize(fetchSize);
			}
			ResultSet resultSet = statement.executeQuery();
			ResultSetMetaData metaData = resultSet.getMetaData();

			int columnCount = metaData.getColumnCount();
			String[] aColName = new String[columnCount];
			int[] aColType = new int[columnCount];
			int[] aColScale = new int[columnCount];

			for (int i = 1; i <= columnCount; ++i) {
				aColName[(i - 1)] = metaData.getColumnName(i).toUpperCase();
				aColType[(i - 1)] = metaData.getColumnType(i);
				aColScale[(i - 1)] = metaData.getScale(i);
			}
			DecimalFormat[] df = new DecimalFormat[11];
			df[0] = df0;
			df[1] = df1;
			df[2] = df2;
			df[3] = df3;
			df[4] = df4;
			df[5] = df5;
			df[6] = df6;
			df[7] = df7;
			df[8] = df8;
			df[9] = df9;
			df[10] = dfA;

			currentRows = 0;
			while (resultSet.next()) {
				rows += 1;
				currentRows++;
				strTemp = "";
				data = "";
				for (int i = 1; i <= columnCount; i++) {
					int type = aColType[(i - 1)];
					switch (type) {
					case 1:
					case 12:
						strTemp = resultSet.getString(i);
						if (strTemp == null) {
							strTemp = "";
						} else {
							strTemp = strTemp.trim();

							char c = '\037';
							if (strTemp.indexOf('\n') > 0) {
								strTemp = strTemp.replace('\n', c);
							}
							if (strTemp.indexOf('\r') > 0)
								strTemp = strTemp.replace('\r', c);
							if (strTemp.indexOf('\032') > 0)
								strTemp = strTemp.replace('\032', c);
							if (strTemp.indexOf(0) > 0) {
								strTemp = strTemp.replace('\000', c);
							}
						}
						data += strTemp;
						break;
					case 91:
						java.sql.Date recDate = resultSet.getDate(i);
						if (recDate != null) {
							data += PubFun.dateHFormat.format(recDate);
						}
						break;
					case 4:
					case 5: {
						Object obj = resultSet.getObject(i);
						if (obj != null) {
							data += Integer.toString(resultSet.getInt(i));
						}
						break;
					}
					case -5: {
						Object obj = resultSet.getObject(i);
						if (obj != null) {
							data += obj.toString();
						}
						break;
					}
					case 6:
					case 7:
					case 8: {
						int scale = aColScale[(i - 1)];
						Object obj = resultSet.getObject(i);
						if (obj != null) {
							double numVal = resultSet.getDouble(i);
							if (scale > 10) {
								throw new Exception("����DECIMAL���;��ȣ�SCALE"
										+ aColScale[(i - 1)]);
							}
							data += df[scale].format(numVal);
						}
						break;
					}
					case 2:
					case 3:
						BigDecimal bigNum = resultSet.getBigDecimal(i);
						if (bigNum == null) {
							strTemp = "";
						} else if (aColScale[(i - 1)] > 10) {
							BigDecimal bigNum10 = bigNum.setScale(10,
									RoundingMode.HALF_UP);
							strTemp = df[10].format(bigNum10.doubleValue());
							bigNum10 = null;
						} else {
							strTemp = bigNum.toPlainString();
						}
						data += strTemp;
						break;
					case 93:
						Timestamp recStamp = resultSet.getTimestamp(i);
						if (recStamp != null) {
							if (conf.get("export.core.TimeUnit").equals(
									"SECOND")) {
								data += PubFun.timeFormat.format(recStamp);
							} else {
								data += recStamp.toString();
							}
						}
						break;
					case 2005:
						writeFile("��֧���������ͣ�" + type);
						break;
					default:
						writeFile("������������ͣ�" + type);
						resultSet.close();
						return false;
					}

					if (i != columnCount) {
						data += PubFun.datSeparator;
					}
				}
				FileUtil.appendln(conf.get("export.core.dataDir") + "/"
						+ datFile, data);

				if (currentRows % 10000 == 0) {
					writeFile("RowCount=" + rows + " CurrentRowCount="
							+ currentRows);
				}
			}
			writeFile("��ѯ" + (idx + 1) + "��ɣ���" + currentRows + "����¼");
			JdbcUtil.close(resultSet, statement, null);
		}
		writeFile("��ѯ��ɣ��ܼ�¼����" + rows);
		createChkFile();
		return true;
	}

	/**
	 * ����chk�ļ���ok�ļ�
	 * 
	 * @throws Exception
	 */
	private void createChkFile() throws Exception {
		File datFile = new File(conf.get("export.core.dataDir") + "/"
				+ this.datFile);

		size = datFile.length();
		if (!datFile.exists()) {
			throw new Exception("Data File Not Found!["
					+ conf.get("export.core.dataDir") + "/" + datFile + "]");
		}
		Calendar fileCal = Calendar.getInstance();
		fileCal.setTimeInMillis(System.currentTimeMillis());
		FileUtil.appendln(
				conf.get("export.core.dataDir") + "/" + this.chkFile,
				this.datFile + PubFun.tabSeparator + rows + PubFun.tabSeparator
						+ size + PubFun.tabSeparator
						+ PubFun.timeFormat.format(fileCal.getTime()));
	}

	/**
	 * ����־д���ļ���
	 * 
	 * @param content
	 *            ��־����
	 * @throws IOException
	 */
	private void writeFile(String content) throws IOException {
		String time = "[" + PubFun.timeFormat.format(new Date()) + "] ";
		FileUtil.appendln(logFile, time + content);
	}
}
