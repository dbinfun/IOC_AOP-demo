package net.dbinfun.ioc;

import net.dbinfun.ioc.beans.BeanInfo;
import net.dbinfun.ioc.beans.BeanType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public class BeanFactory {
    private static final Logger log = LoggerFactory.getLogger(BeanFactory.class);

    private static final Map<String, BeanInfo> beanMap = new ConcurrentHashMap<>(); // 所有bean

    private static final Map<String, BeanInfo> initedBean=new ConcurrentHashMap<>(); // 已经初始化ok的bean

    private static final Map<String, BeanInfo> alreadyInstantiatedBean = new ConcurrentHashMap<>(); // 已经实例化的bean

    private static final Map<String, BeanInfo> instantiatingBean = new ConcurrentHashMap<>(); // 正在实例化的bean

    public static final List<Class<?>> beanAnnotation = new ArrayList<>(); // 需要扫描为bean的注解

    private static final Map<Class<?>, List<Function<BeanInfo,Object>>> postProcessor = new ConcurrentHashMap<>(); // 后置处理器,bean创建后做的工作
    private static final Map<Predicate<Class<?>>, List<Function<Object,Object>>> proxyProcessor = new ConcurrentHashMap<>(); // 代理处理器,满足条件的bean创建代理对象
    private static final Map<Predicate<Class<?>>, List<Function<Class<?>,Object>>> preProcessor = new ConcurrentHashMap<>(); // 前置处理器,再创建所有bean前做的工作

    public static void addBeanAnnotation(Class<?>... clz){
        beanAnnotation.addAll(Arrays.asList(clz));
    }
    private static Boolean isCreatedOver = null;

    public static void createBean(List<Class<?>> clz) {
        clz.forEach(cls-> {
            preProcessor.forEach((k, v) -> {
                if (k.test(cls)) {
                    v.forEach(f -> f.apply(cls));
                }
            });
        });
        isCreatedOver = false;
        for(Class<?> c : clz){
            createBean(c);
        }
        isCreatedOver = true;
    }

    public static Object getBean(String name) {
        if(name==null) return null;
        BeanInfo beanInfo = beanMap.get(name);
        return beanInfo == null
                ? null
                : beanInfo.getType() == BeanType.original
                ? beanInfo.getObject()
                : new BeanFactory().createBean(beanInfo.getCls());
    }

    public static <T> T getBean(Class<T> clz) {
        Object o =getBean(BeanUtil.getBeanName(clz));
        if (clz.isInstance(o)){
            return (T) o;
        }
        return null;
    }

    public static List<Object> getBeans(){
        return new ArrayList<>(beanMap.values());
    }

    public static void addBean(String name,BeanInfo beanInfo){
        beanMap.put(name,beanInfo);
    }

    protected static Object[] getParams(Executable exe){
        Class<?>[] parames = exe.getParameterTypes();
        Object[] objects = new Object[parames.length];
        for (int i = 0; i < parames.length; i++) {
            Object object = createBean(parames[i]);
            if (object == null) {
                log.error("创建bean失败: 找不到依赖: {}",parames[i].getName());
                throw new RuntimeException();
            }
            objects[i] = object;
        }
        return objects;
    }

    private static Object createBean(Class<?> cls) {
        String beanName = BeanUtil.getBeanName(cls); // 获取bean名字
        BeanType beanType = BeanUtil.getBeanType(cls);// 获取bean类型
        if(beanType==null){
            // log.error("没有找到依赖:{}",cls.getName());
            return null;
        }
        // 已经实例化了的bean单例直接返回
        if (alreadyInstantiatedBean.containsKey(beanName)){
            if(BeanType.original==beanType){
                return alreadyInstantiatedBean.get(beanName).getObject();
            }

        }
        // 创建的bean正在实例化中，说明出现了循环依赖
        if (instantiatingBean.containsKey(beanName)) {
            BeanInfo t = instantiatingBean.get(beanName);
            log.error("出现无法解决的循环依赖: {} and {}", cls.getName(), t.getCls().getName());
            throw new RuntimeException();
        }
        // 接下来创建bean
        Object bean=null;
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
            Object[] objects = getParams(constructor[0]);
            bean = constructor[0].newInstance(objects);// 实例化
            // 代理对象处理
            for(Predicate<Class<?>> pre:proxyProcessor.keySet()){
                if(pre.test(cls)){
                    for(Function<Object,Object> fun:proxyProcessor.get(pre)){
                        bean = fun.apply(bean);
                    }
                }
            }
            BeanInfo info = new BeanInfo(beanName, beanType, cls, bean);
            if (!alreadyInstantiatedBean.containsKey(info.getName()))alreadyInstantiatedBean.put(info.getName(), info);// 放入已经实例化的bean
            if(beanType==BeanType.original) {
                beanMap.put(info.getName(), info);// 放入所有bean
            }
            instantiatingBean.remove(info.getName());// 从正在实例化的bean中移除
            Annotation[] annotations = cls.getAnnotations();
            for (Annotation annotation : annotations) {
                List<Function<BeanInfo,Object>> funs = postProcessor.get(annotation.annotationType());
                if (funs!=null) {
                    for (Function<BeanInfo,Object> fun:funs){
                        bean = fun.apply(new BeanInfo(beanName, beanType, cls, bean));
                    }
                }
            }
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

    /**
     * 后置处理器
     * @param cls 注解类
     * @param fun 处理方法
     */
    public static void addPostProcessor(Class<?> cls,Function<BeanInfo,Object> ...fun){
        if(isCreatedOver!=null){
            log.error("后置处理器必须在创建bean之前添加");
            return;
        }
        List<Function<BeanInfo,Object>> list = new LinkedList<>(Arrays.asList(fun));
        List<Function<BeanInfo,Object>> funs = postProcessor.get(cls);
        if(funs==null){
            funs = list;
            postProcessor.put(cls,funs);
        }else{
            funs.addAll(list);
        }
    }

    /**
     * 前置处理器
     * @param predicate 匹配器
     * @param fun 处理方法
     */
    public static void addPreprocessing(Predicate<Class<?>> predicate, Function<Class<?>,Object> ...fun){
        if(isCreatedOver!=null){
            log.error("前置处理器必须在创建bean之前添加");
            return;
        }
        List<Function<Class<?>,Object>> list = new LinkedList<>(Arrays.asList(fun));
        List<Function<Class<?>,Object>> predicates = preProcessor.get(predicate);
        if(predicates==null) {
            predicates = list;
            preProcessor.put(predicate, predicates);
        }else{
            predicates.addAll(list);
        }
    }

    /**
     * 后置处理器
     * @param predicate
     * @param fun
     */
    public static void addProxyProcessing(Predicate<Class<?>> predicate, Function<Object,Object> ...fun){
        if(isCreatedOver!=null){
            log.error("后置处理器必须在创建bean之前添加");
            return;
        }
        List<Function<Object,Object>> list = new LinkedList<>(Arrays.asList(fun));
        List<Function<Object,Object>> predicates = proxyProcessor.get(predicate);
        if(predicates==null) {
            predicates = list;
            proxyProcessor.put(predicate, predicates);
        }else{
            predicates.addAll(list);
        }
    }
}
