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
	
	//��ɨ��İ���
	private String scanPath = "";
	//����ip��ַ�˿�
	private String connectString = "";
	//������
	private String webServiceCenter = "sampleService";
	//Ӧ����
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
		// ��ȡ�˰���Ŀ¼ ����һ��File
		File dir = new File(packagePath);
		// ��������ڻ��� Ҳ����Ŀ¼��ֱ�ӷ���
		if (!dir.exists() || !dir.isDirectory()) {
			// log.warn("�û�������� " + packageName + " ��û���κ��ļ�");
			return;
		}
		// ������� �ͻ�ȡ���µ������ļ� ����Ŀ¼
		File[] dirfiles = dir.listFiles(new FileFilter() {
			// �Զ�����˹��� �������ѭ��(������Ŀ¼) ��������.class��β���ļ�(����õ�java���ļ�)
			public boolean accept(File file) {
				return (recursive && file.isDirectory())
				|| (file.getName().endsWith(".class"));
			}
		});
		// ѭ�������ļ�
		for (File file : dirfiles) {
			// �����Ŀ¼ �����ɨ��
			if (file.isDirectory()) {
				findAndAddClassesInPackageByFile(
						packageName + "." + file.getName(),
						file.getAbsolutePath(), recursive, classes);
			} else {
				// �����java���ļ� ȥ�������.class ֻ��������
				String className = file.getName().substring(0,
						file.getName().length() - 6);
				try {
					// ��ӵ�������ȥ
					// classes.add(Class.forName(packageName + '.' +
					// className));
					// �����ظ�ͬѧ�����ѣ�������forName��һЩ���ã��ᴥ��static������û��ʹ��classLoader��load�ɾ�
					classes.add(Thread.currentThread().getContextClassLoader()
							.loadClass(packageName + '.' + className));
				} catch (ClassNotFoundException e) {
					// log.error("����û��Զ�����ͼ����� �Ҳ��������.class�ļ�");
					e.printStackTrace();
				}
			}
		}
	}

	public void init() {
		try {
			System.out.println("ɨ���ʼ��------");
			//��ʼ��zk�ͻ���
			buildZKclient();
			registBiz();

			//ɨ������action��ͷ���
			Set<Class<?>> classes = ClassUtil.getClassSet(scanPath);

			//ͨ��ע��õ������ַ
			List<String> services = ClassUtil.getServicePath(classes);
			
			for (String s : services)
				System.out.println("service=" + s);
			System.out.println("------------------size=");
			//�ѷ���ע�ᵽzk
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
//				.forPath("/" + bizCode, new String((bizCode + "�ṩ�ķ����б�")).getBytes("GBK"))
				.forPath("/" + bizCode, new String(bizCode + "�ṩ�ķ����б�").getBytes("ISO-8859-1"));
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
