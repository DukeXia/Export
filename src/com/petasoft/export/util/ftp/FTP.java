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
package com.petasoft.export.util.ftp;

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.net.ftp.FTPClient;

/**
 * @author ���� 2015-05-15
 */
public class FTP {

	/**
	 * FTP�����ļ�
	 * 
	 * @param host
	 *            FTP����
	 * @param port
	 *            FTP�˿�
	 * @param user
	 *            FTP�û�
	 * @param pass
	 *            FTP����
	 * @param path
	 *            FTP·��
	 * @param file
	 *            FTP�ļ�
	 * @param code
	 *            FTP����
	 * @param mode
	 *            FTPģʽ
	 * @return
	 * @throws Exception
	 */
	public static boolean ftpFile(String host, String port, String user,
			String pass, String path, String file, String code, String mode)
			throws Exception {
		File srcFile = new File(file);
		FTPClient ftpClient = new FTPClient();
		FileInputStream fis = null;

		fis = new FileInputStream(srcFile);
		// ��������
		ftpClient.connect(host);
		// ��½
		ftpClient.login(user, pass);
		// �л�·��
		ftpClient.changeWorkingDirectory(path);
		// ���û���
		ftpClient.setBufferSize(1024);
		// ���ñ���
		ftpClient.setControlEncoding(code);
		// �����ļ����ͣ������ƣ�
		ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
		if (mode.equals("Passive")) {
			ftpClient.enterLocalPassiveMode();
		}
		// �ϴ��ļ�
		ftpClient.storeFile(srcFile.getName(), fis);

		fis.close();
		ftpClient.disconnect();
		return true;
	}
}
