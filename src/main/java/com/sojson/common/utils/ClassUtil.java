package com.sojson.common.utils;
/*
 * Copyright (c) 2016 http://www.feiniu.com/. All Rights Reserved.
 */

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * <P>Title: ClassUtil.java</P>
 * <P>Description: 类操作工具类</P>
 * @author wb
 * @version V1.0
 * @date 2016-12-29 17:10
 */
public class ClassUtil {

	private static final Logger logger = LoggerFactory.getLogger(ClassUtil.class);

	/**
	 * 获取类加载器
	 * @return
	 */
	public  static ClassLoader getClassLodader(){
		return  Thread.currentThread().getContextClassLoader();
	}

	/**
	 * 加载类
	 * @param className
	 * @param isInitialized
	 * @return
	 */
	public  static  Class<?> loadClass(String className,Boolean isInitialized){
		Class<?> cls;
		try {
			cls = Class.forName(className,isInitialized,getClassLodader());
		} catch (ClassNotFoundException e) {
			logger.error("load class failure",e);
			throw  new RuntimeException(e);
		}
		return  cls;
	}

	/**
	 * 获取指定包名下的所有类
	 * @param packageName
	 * @return
	 */
	public static Set<Class<?>> getClassSet(String packageName){
		Set<Class<?>> classSet = new HashSet<Class<?>>();
		//包名转换成文件目录地址 举例com.alibaba.taobao 转换为 com/alibaba/taobao
		try {
			/** java application */
			//			Enumeration<URL> urls = getClassLoader().getResources(packageName.replace(".", "/"));
			/** on server */
			Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(packageName.replace(".", "/"));
			while (urls.hasMoreElements()){
				URL url =  urls.nextElement();
				if(null != url){
					String  protocol = url.getProtocol();
					if(protocol.equals("file")){
						String packagePath = url.getPath().replaceAll("%20","");//去空格
						addClass(classSet,packagePath,packageName);
					}else if(protocol.equals("jar")){
						JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
						if(null != jarURLConnection){
							JarFile jarFile = jarURLConnection.getJarFile();
							Enumeration<JarEntry> jarEntries = jarFile.entries();
							while (jarEntries.hasMoreElements()){
								JarEntry jarEntry = jarEntries.nextElement();
								String jarEntryName = jarEntry.getName();
								if(jarEntryName.endsWith(".class")){
									String className = jarEntryName.substring(0, jarEntryName.lastIndexOf(".")).replaceAll("/", ".");
									doAddClass(classSet, className);
								}
							}
						}
					}
				}
			}
		} catch (IOException e) {
			logger.error("get class set failure",e);
		}
		return classSet;
	}

	private static void addClass(Set<Class<?>> classSet, String packagePath, String packageName) {

		final File[] files = new File(packagePath).listFiles(new FileFilter() {
			public boolean accept(File file) {
				return (file.isFile() && file.getName().endsWith(".class") || file.isDirectory());
			}
		});
		for(File f : files){//遍历当前的目录的所有文件如果是class文件则直接放入set中，否则继续遍历
			String name = f.getName();
			if (f.isFile()){
				String className = name.substring(0,name.lastIndexOf("."));
				if(StringUtils.isNotEmpty(packageName)){
					className = packageName + "." + className;
				}
				doAddClass(classSet, className);
			}else {//递归调用一直到文件目录最后一级
				String subPackagePath = name;
				if(StringUtils.isNotEmpty(subPackagePath)){
					subPackagePath = packagePath + "/" + subPackagePath;
				}
				String subPackageName = name;
				if (StringUtils.isNotEmpty(packageName)) {
					subPackageName = packageName + "." + subPackageName;
				}
				addClass(classSet, subPackagePath, subPackageName);
			}
		}
	}

	private static void doAddClass(Set<Class<?>> classSet, String className) {
		Class<?> cls = loadClass(className, false);
		classSet.add(cls);
	}

	public static List<String> getServicePath(Set<Class<?>> classes) {
		List<String> services = new ArrayList<String>();
		//HashMap<String, Map<String, String>> serviceParams = new HashMap<String, Map<String, String>>();
		StringBuffer sb = null;
		Controller classContorllA = null;
		RequestMapping classRequestmA = null;
		RequestMapping methodRequestmA = null;
		Annotation ann = null;
		for (Class cls : classes) {
			ann = cls.getAnnotation(Controller.class);
			if (ann == null)
				continue;
			else
				classContorllA = (Controller) ann;

			ann = cls.getAnnotation(RequestMapping.class);
			String basePath = getRequestMappingPath(ann);

			if (basePath != null){
				if (!basePath.startsWith("/"))
					basePath = "/" + basePath;
			}else
				continue;

			Method ms[] = cls.getMethods();
			if (ms == null || ms.length == 0)
				continue;
			for (Method m : ms) {
				ann = m.getAnnotation(RequestMapping.class);

				String path = getRequestMappingPath(ann);
				if (path != null) {
					sb = new StringBuffer();
					if (basePath != null)
						sb.append(basePath);
					if (path.startsWith("/"))
						sb.append(path);
					else
						sb.append("/" + path);
				} else
					continue;
				if (sb != null) {
					services.add(sb.toString());
					//					Class paramC[] = m.getParameterTypes();
					//					TypeVariable tvs[] = m.getTypeParameters();
					//					// int pl = paramC.length;
					//					if (paramC.length > 0) {
					//						for (int pl = 0; pl < paramC.length; pl++) {
					//							System.out.println("params=" + tvs[pl].getName()
					//									+ "");
					//							System.out.println("params type="
					//									+ paramC[pl].getName() + "");
					//						}
					//					}
				}
				sb = null;
			}
			// sb.append(classA.)
		}
		return services;
	}

	private static String getRequestMappingPath(Annotation ann) {
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

	public static void main(String[] args) {

		Set<Class<?>> classSet = ClassUtil.getClassSet("com.sojson");

		List<String> servicePath = ClassUtil.getServicePath(classSet);

		for(String ser : servicePath){
			System.out.println(ser);
		}

	}
}