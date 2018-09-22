package com.xuyu;

import java.util.ArrayList;
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
public class Test001 {


	public static void main(String[] args) {
		ThreadConnection threadConnection = new ThreadConnection();
		for (int i = 0; i < 3; i++) {
			Thread thread = new Thread(threadConnection,"线程i:"+i);
			thread.start();
		}
	}
	
}
class ThreadConnection implements Runnable{

	public void run() {
		for (int i = 0; i < 10; i++) {
			//创建连接
			Connection connection = ConnectionPoolManager.getConnection();
			System.out.println(Thread.currentThread().getName()+",connection："+connection);
			ConnectionPoolManager.releaseConnection(connection);
		}
	}
	
}
