package com.xuyu;

import com.mysql.jdbc.Connection;

//�������ݿ��
public interface IConnectionPool {

	//��ȡ���ӡ��ظ����û��ơ�
	public Connection getConnection();
	
	//�ͷ����ӡ��ɻ��ջ��ơ�
	public void  releaseConnection(Connection connection);
	
	//�������� ��Connection��
	public Connection newConnection();
}
