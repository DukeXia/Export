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
import java.util.Properties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

/**
 * @author ���� 2015-05-15
 */
public class SFTP {
	/**
	 * SFTP�����ļ�
	 * 
	 * @param host
	 *            SFTP����
	 * @param port
	 *            SFTP�˿�
	 * @param user
	 *            SFTP�û�
	 * @param pass
	 *            SFTP����
	 * @param path
	 *            SFTP·��
	 * @param file
	 *            SFTP�ļ�
	 * @param cset
	 *            SFTP����
	 * @param time
	 *            SFTP��ʱ
	 * @return
	 * @throws Exception
	 */
	public static boolean ftpFile(String host, String port, String user,
			String pass, String path, String file, String cset, String time)
			throws Exception {
		File srcFile = new File(file);
		Session session = null;
		Channel channel = null;
		ChannelSftp sftp = null;

		// ����JSch����
		JSch jsch = new JSch();

		// �����û�����������ַ���˿ڻ�ȡһ��Session����
		session = jsch.getSession(user, host, Integer.parseInt(port));
		if (pass != null) {
			// ��������
			session.setPassword(pass);
		}
		Properties config = new Properties();
		config.put("StrictHostKeyChecking", "no");
		// ΪSession��������properties
		session.setConfig(config);
		// ����timeoutʱ��
		session.setTimeout(Integer.valueOf(time) * 1000);
		// ͨ��Session��������
		session.connect();
		// ��SFTPͨ��
		channel = session.openChannel("sftp");
		// ����SFTPͨ��������
		channel.connect();
		sftp = (ChannelSftp) channel;

		sftp.cd(path);
		sftp.put(new FileInputStream(file), srcFile.getName(),
				ChannelSftp.OVERWRITE);

		sftp.quit();
		sftp.disconnect();
		session.disconnect();
		return true;
	}
}
