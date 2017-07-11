package com.sojson.common.controller;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sojson.common.utils.ClassUtil;

public class ScanControll {
	private CuratorFramework client = null;
	
	//被扫描的包名
	private String scanPath = "";
	//连接ip地址端口
	private String connectString = "";
	//服务名
	private String webServiceCenter = "sampleService";
	//应用名
	private String bizCode = "sampleweb";

	public ScanControll(String path, String connectString, String webServiceCenter ,String bizcode) {
		this.scanPath = path;
		this.connectString = connectString;
		this.webServiceCenter = webServiceCenter;
		this.bizCode = bizcode;
		System.out.println(scanPath + "--" + this.connectString + "---" + this.webServiceCenter + "---" + this.bizCode);
	}

	private void buildZKclient() {
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
		this.client = CuratorFrameworkFactory.builder().connectString(this.connectString)
		.sessionTimeoutMs(10000).retryPolicy(retryPolicy)
		.namespace(this.webServiceCenter).build();
		this.client.start();
	}


	public void findAndAddClassesInPackageByFile(String packageName,
			String packagePath, final boolean recursive, Set<Class<?>> classes) {
		// 获取此包的目录 建立一个File
		File dir = new File(packagePath);
		// 如果不存在或者 也不是目录就直接返回
		if (!dir.exists() || !dir.isDirectory()) {
			// log.warn("用户定义包名 " + packageName + " 下没有任何文件");
			return;
		}
		// 如果存在 就获取包下的所有文件 包括目录
		File[] dirfiles = dir.listFiles(new FileFilter() {
			// 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
			public boolean accept(File file) {
				return (recursive && file.isDirectory())
				|| (file.getName().endsWith(".class"));
			}
		});
		// 循环所有文件
		for (File file : dirfiles) {
			// 如果是目录 则继续扫描
			if (file.isDirectory()) {
				findAndAddClassesInPackageByFile(
						packageName + "." + file.getName(),
						file.getAbsolutePath(), recursive, classes);
			} else {
				// 如果是java类文件 去掉后面的.class 只留下类名
				String className = file.getName().substring(0,
						file.getName().length() - 6);
				try {
					// 添加到集合中去
					// classes.add(Class.forName(packageName + '.' +
					// className));
					// 经过回复同学的提醒，这里用forName有一些不好，会触发static方法，没有使用classLoader的load干净
					classes.add(Thread.currentThread().getContextClassLoader()
							.loadClass(packageName + '.' + className));
				} catch (ClassNotFoundException e) {
					// log.error("添加用户自定义视图类错误 找不到此类的.class文件");
					e.printStackTrace();
				}
			}
		}
	}

	public void init() {
		try {
			System.out.println("扫描初始化------");
			//初始化zk客户端
			buildZKclient();
			registBiz();

			//扫描所有action类和方法
			Set<Class<?>> classes = ClassUtil.getClassSet(scanPath);

			//通过注解得到服务地址
			List<String> services = ClassUtil.getServicePath(classes);
			
			for (String s : services)
				System.out.println("service=" + s);
			System.out.println("------------------size=");
			//把服务注册到zk
			registBizServices(services);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void registBiz() {
		try {
			if (client.checkExists().forPath("/" + bizCode) == null) {
				client.create().creatingParentsIfNeeded()
				.withMode(CreateMode.PERSISTENT)
				.withACL(Ids.OPEN_ACL_UNSAFE)
//				.forPath("/" + bizCode, new String((bizCode + "提供的服务列表")).getBytes("GBK"))
				.forPath("/" + bizCode, new String(bizCode + "提供的服务列表").getBytes("ISO-8859-1"));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void registBizServices(List<String> services) {
		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
			String ip = addr.getHostAddress().toString();
			for (String s : services) {
				String temp = s.replace("/", ".");
				if(temp.startsWith("."))
					temp = temp.substring(1);
				if (client.checkExists().forPath("/" + bizCode +"/"+ temp) == null) {
					client.create().creatingParentsIfNeeded()
					.withMode(CreateMode.PERSISTENT)
					.withACL(Ids.OPEN_ACL_UNSAFE)
					.forPath("/" + bizCode +"/"+ temp, ("1").getBytes());
				}
				client.create()
				.creatingParentsIfNeeded()
				.withMode(CreateMode.EPHEMERAL)
				.withACL(Ids.OPEN_ACL_UNSAFE)
				.forPath("/" + bizCode +"/"+ temp + "/" + ip, ("1").getBytes());

			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private String getRequestMappingPath(Annotation ann) {
		if (ann == null)
			return null;
		else {
			RequestMapping rma = (RequestMapping) ann;
			String[] paths = rma.value();
			if (paths != null && paths.length > 0)
				return paths[0];
			else
				return null;
		}
	}
	
}
