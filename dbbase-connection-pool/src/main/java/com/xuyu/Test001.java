package com.xuyu;

import java.util.ArrayList;
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
public class Test001 {


	public static void main(String[] args) {
		ThreadConnection threadConnection = new ThreadConnection();
		for (int i = 0; i < 3; i++) {
			Thread thread = new Thread(threadConnection,"�߳�i:"+i);
			thread.start();
		}
	}
	
}
class ThreadConnection implements Runnable{

	public void run() {
		for (int i = 0; i < 10; i++) {
			//��������
			Connection connection = ConnectionPoolManager.getConnection();
			System.out.println(Thread.currentThread().getName()+",connection��"+connection);
			ConnectionPoolManager.releaseConnection(connection);
		}
	}
	
}
