package com.xuyu;

import com.mysql.jdbc.Connection;

//�����̳߳�
public class ConnectionPoolManager {

	private static  DbBean dbBean=new DbBean();
	private static ConnectionPool connectionPool=new ConnectionPool(dbBean);
	
	//��ȡ����
	public static Connection getConnection() {
		return  connectionPool.getConnection();
	}
	//�ͷ�����
	public static  void releaseConnection(Connection connection) {
		connectionPool.releaseConnection(connection);
	}
}
