package com.xuyu;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import com.mysql.jdbc.Connection;
/**
 * �׻��ķ��� ���ݿ����ӳ�ԭ��
 * ���Ĳ���
 * 1.�����̣߳���û�б�ʹ�õ����Ӵ��
 * 2.��̣߳�����ʹ�õ�����
 * ���Ĳ���
 * 1.��ʼ���̳߳أ���ʼ�������̣߳�
 * 2.����getConnection����  ��ȡ����
 * 		1.��ȥ�������̡߳���ȡ��ǰ���ӣ���š���̡߳�����
 * 3.�����ͷ����ӷ��� releaseConnection����Դ����
 * 		1.��ȡ����̡߳����ӣ�ת�Ƶ��������̡߳���������
 * 
 * @author Administrator
 *
 */
public class ConnectionPool implements IConnectionPool{
	//ʹ���̰߳�ȫ�ļ��� �������̡߳� ���� û�б�ʹ�õ����Ӵ��
	private List<Connection> freeConnection=new Vector<Connection>();
	//ʹ���̰߳�ȫ�ļ��� ����̡߳� ���� ����ʹ�õ�����
	private List<Connection> activeConnection=new Vector<Connection>();

	private DbBean dbBean;
	
	private int countConn=0;
	public ConnectionPool(DbBean dbBean) {
		//���������ļ���Ϣ
		this.dbBean=dbBean;
	}
	
	//��ʼ���̳߳ء���ʼ�������̡߳�
	private void init() {
		if(dbBean==null) {
			return;
		}
		//1.��ȡ��ʼ������
		for (int i = 0; i < dbBean.getInitConnections(); i++) {
			//2.����Connection����
			Connection newConnection = newConnection();
			if(newConnection!=null) {
				//3.����ڿ���freeConnection����
				freeConnection.add(newConnection);
			}
		}
	}
	//����Connection����
	public synchronized Connection newConnection() {
		try {
			//���������ļ�
			Class.forName(dbBean.getDriverName());
			//�������ļ��ж�ȡ������Ϣ
			Connection connection = (Connection) DriverManager.getConnection(dbBean.getUrl(),dbBean.getUserName(),dbBean.getPassword());
			countConn++;
			return connection;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	//��ȡ���ӡ��ظ����û��ơ�
	public synchronized Connection getConnection() {
		Connection connection=null;
		try {
			//С����������� 
			if(countConn<dbBean.getMaxActiveConnections()) {
				//1.�жϿ����߳��Ƿ�������
				if(freeConnection.size()>0) {
					//�ڿ����̴߳������� �õ���ɾ��
					connection = freeConnection.remove(0);
				}else {
					//��������
					connection = newConnection();
				}
				//�ж������Ƿ����
				boolean avalived = isAvalived(connection);
				//�����߳̿���
				if(avalived) {
					//����̴߳�
					activeConnection.add(connection);
				}else {
					countConn--;
					//�����̲߳����ԣ��ݹ�������Ի�ȡ����
					connection = getConnection();
				}
			
			}else {
				//������������������еȴ�
				wait(dbBean.getConnTimeOut());
				//����
				connection=getConnection();
			}
			return connection;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}
	//�ж������Ƿ����
	public boolean isAvalived(Connection connection){
		try {
			if(connection==null|| connection.isClosed()) {
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}
	//�ͷ����ӡ��ɻ��ջ��ơ�
	public synchronized void releaseConnection(Connection connection) {
		try {
		//�ж������Ƿ���� ���ӿ���
		if(isAvalived(connection)) {
			//�жϿ����߳��Ƿ�����
			if(freeConnection.size()<dbBean.getMaxConnections()) {
				//�����߳�û���� ��������̳߳�
				freeConnection.add(connection);
			}else {
				//�����߳��Ѿ�����
				connection.close();
				}
				//�ѵ�ǰ�������remove��
				activeConnection.remove(connection);
				countConn--;
				//֪ͨ�����߳�
				notifyAll();
			}
		} catch (SQLException e) {
		e.printStackTrace();
		}
	}
}
