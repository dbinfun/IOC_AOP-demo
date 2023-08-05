package net.dbinfun.ioc;

import net.dbinfun.ioc.annotation.Bean;
import net.dbinfun.ioc.annotation.Service;
import net.dbinfun.ioc.beans.BeanInfo;
import net.dbinfun.ioc.beans.BeanType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BeanFactory {
    public static final Logger log = LoggerFactory.getLogger(BeanFactory.class);

    private static final Map<String, BeanInfo> beanMap = new ConcurrentHashMap<>(); // 所有bean

    // private final Map<String, BeanInfo> initedBean; // 已经初始化ok的bean

    private final Map<String, BeanInfo> alreadyInstantiatedBean; // 已经实例化的bean

    private final Map<String, BeanInfo> instantiatingBean; // 正在实例化的bean

    private static final List<Class<?>> beanAnnotation = new ArrayList<>(); // 需要扫描为bean的注解

    public static void addBeanAnnotation(Class<?>... clz){
        beanAnnotation.addAll(Arrays.asList(clz));
    }

    private BeanFactory() {
        // initedBean = new ConcurrentHashMap<>();
        alreadyInstantiatedBean = new ConcurrentHashMap<>();
        instantiatingBean = new ConcurrentHashMap<>();
    }

    public static void createBean(List<Class<?>> clz) {
        for(Class<?> c : clz){
            new BeanFactory().createBean(c);
        }
    }

    public static Object getBean(String name) {
        BeanInfo beanInfo = beanMap.get(name);
        return beanInfo == null
                ? null
                : beanInfo.getType() == BeanType.original
                ? beanInfo.getObject()
                : new BeanFactory().createBean(beanInfo.getCls());
    }

    public static <T> T getBean(Class<T> clz) {
        Object o =getBean(getBeanName(clz));
        if (clz.isInstance(o)){
            return (T) o;
        }
        return null;
    }

    public static List<Object> getBeans(){
        return new ArrayList<>(beanMap.values());
    }

    /**
     * 获取注解的值
     *
     * @param annotation   注解
     * @param propertyName 属性名
     * @return 属性值
     */
    private static Object getAnnotationValue(Annotation annotation, String propertyName) {
        try {
            return annotation.getClass().getMethod(propertyName).invoke(annotation);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取bean名字
     *
     * @param clz 类
     * @return bean名字
     */
    private static String getBeanName(Class<?> clz) {
        Annotation[] annotation = clz.getDeclaredAnnotations();
        String name = null;
        for (Annotation a : annotation) {
            if (beanAnnotation.contains(a.annotationType())) {
                // 获取a的value
                name = (String) getAnnotationValue(a, "value");
                if (name == null) {
                    log.error("获取bean名字失败，{}注解{}的value为空", clz.getName(), a.getClass().getName());
                }
                break;
            }
        }
        if ("".equals(name)) {
            name = clz.getName();
        }
        return name;
    }

    private static BeanType getBeanType(Class<?> clz) {
        Annotation[] annotation = clz.getAnnotations();
        BeanType beanType = null;
        for (Annotation a : annotation) {
            if (beanAnnotation.contains(a.annotationType())) {
                // 获取a的value
                beanType = (BeanType) getAnnotationValue(a, "type");
                if (beanType == null) {
                    log.error("获取bean名字失败，{}注解{}的type为空", clz.getName(), a.getClass().getName());
                }
                break;
            }
        }
        return beanType;
    }


    /**
     * 获取需要依赖
     *
     * @return 依赖
     */
    private Object getDependenceBean(Class<?> clz) {
        String name = getBeanName(clz);// 获取bean名字
        BeanType beanType = getBeanType(clz);
        BeanInfo beanInfo = beanMap.get(name); // 所有bean中是否有这个bean
        // 如果有这个bean且是单例bean则返回
        if (beanInfo != null && BeanType.original == beanType) {
            return beanInfo.getObject();
        }
        // 否则都要去创建bean
        else {
            return createBean(clz);
        }
    }

    private Object createBean(Class<?> cls) {
        String beanName = getBeanName(cls); // 获取bean名字
        BeanType beanType = getBeanType(cls);// 获取bean类型
        if(beanType==null|| beanName==null){
            // log.error("没有找到依赖:{}",cls.getName());
            return null;
        }
        // 已经实例化了的bean单例直接返回
        if (alreadyInstantiatedBean.containsKey(beanName)&&BeanType.original==beanType){
            return alreadyInstantiatedBean.get(beanName).getObject();
        }
        // 创建的bean正在实例化中，说明出现了循环依赖
        if (instantiatingBean.containsKey(beanName)) {
            BeanInfo t = instantiatingBean.get(beanName);
            log.error("出现无法解决的循环依赖: {} and {}", cls.getName(), t.getCls().getName());
            throw new RuntimeException();
        }
        // 接下来创建bean
        Object bean;
        BeanInfo beanInfo = new BeanInfo(beanName, beanType, cls, null);
        instantiatingBean.put(beanName, beanInfo); // 放入正在实例化的bean
        beanMap.put(beanName, beanInfo); // 放入所有bean
        try {
            // 获取构造器
            Constructor<?>[] constructor = cls.getConstructors();
            if (constructor.length != 1) {
                throw new NoSuchMethodException();
            }
            // 获取构造器参数
            Class<?>[] parameterTypes = constructor[0].getParameterTypes();
            Object[] objects = new Object[parameterTypes.length];
            // 生成参数值
            for (int i = 0; i < parameterTypes.length; i++) {
                Object object = getDependenceBean(parameterTypes[i]);
                if (object == null) {
                    log.error("创建bean失败: {},找不到依赖: {}", beanName, parameterTypes[i].getName());
                    throw new RuntimeException();
                }
                objects[i] = object;
            }
            bean = constructor[0].newInstance(objects);
            BeanInfo info = new BeanInfo(beanName, beanType, cls, bean);
            alreadyInstantiatedBean.put(info.getName(), info);// 放入已经实例化的bean
            if(beanType==BeanType.original) {
                beanMap.put(info.getName(), info);// 放入所有bean
            }
            instantiatingBean.remove(info.getName());// 从正在实例化的bean中移除
            return bean;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            log.error("创建bean失败: {}",beanName);
            e.printStackTrace();
            throw new RuntimeException();
        } catch (NoSuchMethodException e) {
            log.error("创建bean失败: {},无法找到唯一的构造方法",beanName);
            throw new RuntimeException();
        }
    }
}
