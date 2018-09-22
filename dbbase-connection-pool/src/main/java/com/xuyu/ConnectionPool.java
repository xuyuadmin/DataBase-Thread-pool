package com.xuyu;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import com.mysql.jdbc.Connection;
/**
 * 白话文翻译 数据库连接池原理
 * 核心参数
 * 1.空闲线程：，没有被使用的连接存放
 * 2.活动线程：正在使用的连接
 * 核心步骤
 * 1.初始化线程池（初始化空闲线程）
 * 2.调用getConnection方法  获取连接
 * 		1.先去【空闲线程】获取当前连接，存放【活动线程】容器
 * 3.调用释放连接方法 releaseConnection，资源利用
 * 		1.获取【活动线程】连接，转移到【空闲线程】连接容器
 * 
 * @author Administrator
 *
 */
public class ConnectionPool implements IConnectionPool{
	//使用线程安全的集合 【空闲线程】 容器 没有被使用的连接存放
	private List<Connection> freeConnection=new Vector<Connection>();
	//使用线程安全的集合 【活动线程】 容器 正在使用的连接
	private List<Connection> activeConnection=new Vector<Connection>();

	private DbBean dbBean;
	
	private int countConn=0;
	public ConnectionPool(DbBean dbBean) {
		//传入配置文件信息
		this.dbBean=dbBean;
	}
	
	//初始化线程池【初始化空闲线程】
	private void init() {
		if(dbBean==null) {
			return;
		}
		//1.获取初始化连接
		for (int i = 0; i < dbBean.getInitConnections(); i++) {
			//2.创建Connection连接
			Connection newConnection = newConnection();
			if(newConnection!=null) {
				//3.存放在空闲freeConnection集合
				freeConnection.add(newConnection);
			}
		}
	}
	//创建Connection连接
	public synchronized Connection newConnection() {
		try {
			//加载配置文件
			Class.forName(dbBean.getDriverName());
			//从配置文件中读取连接信息
			Connection connection = (Connection) DriverManager.getConnection(dbBean.getUrl(),dbBean.getUserName(),dbBean.getPassword());
			countConn++;
			return connection;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	//获取连接【重复利用机制】
	public synchronized Connection getConnection() {
		Connection connection=null;
		try {
			//小于最大活动连接数 
			if(countConn<dbBean.getMaxActiveConnections()) {
				//1.判断空闲线程是否有连接
				if(freeConnection.size()>0) {
					//在空闲线程存在连接 拿到再删除
					connection = freeConnection.remove(0);
				}else {
					//创建连接
					connection = newConnection();
				}
				//判断连接是否可用
				boolean avalived = isAvalived(connection);
				//连接线程可用
				if(avalived) {
					//往活动线程存
					activeConnection.add(connection);
				}else {
					countConn--;
					//连接线程不可以，递归调用重试获取连接
					connection = getConnection();
				}
			
			}else {
				//大于最大活动连接数，进行等待
				wait(dbBean.getConnTimeOut());
				//重试
				connection=getConnection();
			}
			return connection;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}
	//判断连接是否可用
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
	//释放连接【可回收机制】
	public synchronized void releaseConnection(Connection connection) {
		try {
		//判断连接是否可用 连接可用
		if(isAvalived(connection)) {
			//判断空闲线程是否满了
			if(freeConnection.size()<dbBean.getMaxConnections()) {
				//空闲线程没有满 存入空闲线程池
				freeConnection.add(connection);
			}else {
				//空闲线程已经满了
				connection.close();
				}
				//把当前活动的连接remove掉
				activeConnection.remove(connection);
				countConn--;
				//通知其他线程
				notifyAll();
			}
		} catch (SQLException e) {
		e.printStackTrace();
		}
	}
}
