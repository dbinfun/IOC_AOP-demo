package net.dbinfun.ioc;

import net.dbinfun.ioc.annotation.Component;
import net.dbinfun.ioc.annotation.Controller;
import net.dbinfun.ioc.annotation.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MyIOCApplication {
    public static final Logger log = LoggerFactory.getLogger(MyIOCApplication.class);
    public static void start(Class<?> cls, String[] args) {
        if(cls==null) {
            log.error("No application class provided");
            return;
        }
        log.info("Application starting...");
        log.info("Application init...");
        List<Class<?>> classes = scanner(cls);

        BeanFactory.addBeanAnnotation(Service.class, Controller.class, Component.class);// 设置要扫描的bean
        BeanPostProcessor.todo();
        BeanFactory.createBean(classes); // 创建bean
        log.info("Application init success");
        log.info("Application started");
    }

    /**
     * 获取指定包下的所有类
     * @param pkgName 指定包名
     * @return 指定包下的所有包名或者类名
     */
    private static List<String> getPackageOrClass(String pkgName){
        List<String> list = new LinkedList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String packagePath = pkgName.replace('.', '/');

        // 在类路径下查找指定包的资源文件
        File packageDir = new File(classLoader.getResource(packagePath).getFile());
        if (packageDir.exists()) {
            // 获取目录下的所有文件（包括子目录）
            File[] files = packageDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    // 将文件名转换为类名
                    String fileName = file.getName();
                    // 如果是类，则直接加入列表
                    if (fileName.endsWith(".class")) {
                        String className = pkgName + '.' + fileName.substring(0, fileName.length() - 6);
                        list.add(className);
                    }
                    else if(file.isDirectory()){
                        String subPkgName = pkgName+"."+fileName;
                        list.addAll(getPackageOrClass(subPkgName));
                    }
                }
            }
        }
        return list;
    }

    /**
     * 扫描启动类同级包下的所有类
     * @param cls 启动类
     */
    private static List<Class<?>> scanner(Class<?> cls){
        Package pkg = cls.getPackage();
        String pkgName = pkg.getName();
        log.info("start scanning package...");
        List<String> list = getPackageOrClass(pkgName);
        List<Class<?>> classes = list.stream().map(s->{
            try {
                return Class.forName(s);
            } catch (ClassNotFoundException e) {
                log.error("class not found: "+s);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
        log.info("scanning package finished");
        return classes;
    }
}
