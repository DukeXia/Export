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
package com.petasoft.export.ftp;

import java.util.Map;
import java.util.Properties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * @author ���� 2015-05-15
 */
public class SFTPChannel {
	Session session = null;
	Channel channel = null;

	public ChannelSftp getChannel(Map<String, String> sftp, int timeout)
			throws JSchException {

		String ftpHost = sftp.get(SFTPConstants.SFTP_REQ_HOST);
		String ftpPort = sftp.get(SFTPConstants.SFTP_REQ_PORT);
		String ftpUser = sftp.get(SFTPConstants.SFTP_REQ_USER);
		String ftpPass = sftp.get(SFTPConstants.SFTP_REQ_PASS);

		int defPort = SFTPConstants.SFTP_DEFAULT_PORT;

		if (ftpPort != null && !ftpPort.equals("")) {
			defPort = Integer.valueOf(ftpPort);
		}

		// ����JSch����
		JSch jsch = new JSch();

		// �����û���������ip���˿ڻ�ȡһ��Session����
		session = jsch.getSession(ftpUser, ftpHost, defPort);
		if (ftpPass != null) {
			// ��������
			session.setPassword(ftpPass);
		}
		Properties config = new Properties();
		config.put("StrictHostKeyChecking", "no");
		// ΪSession��������properties
		session.setConfig(config);
		// ����timeoutʱ��
		session.setTimeout(timeout);
		// ͨ��Session��������
		session.connect();
		// ��SFTPͨ��
		channel = session.openChannel("sftp");
		// ����SFTPͨ��������
		channel.connect();
		return (ChannelSftp) channel;
	}

	public void closeChannel() throws Exception {
		if (channel != null) {
			channel.disconnect();
		}
		if (session != null) {
			session.disconnect();
		}
	}
}
