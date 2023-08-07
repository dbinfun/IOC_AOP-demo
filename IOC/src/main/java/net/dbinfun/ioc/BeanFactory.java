package net.dbinfun.ioc;

import net.dbinfun.ioc.beans.BeanInfo;
import net.dbinfun.ioc.beans.BeanType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class BeanFactory {
    private static final Logger log = LoggerFactory.getLogger(BeanFactory.class);

    private static final Map<String, BeanInfo> beanMap = new ConcurrentHashMap<>(); // 所有bean

    private static final Map<String, BeanInfo> initedBean=new ConcurrentHashMap<>(); // 已经初始化ok的bean

    private static final Map<String, BeanInfo> alreadyInstantiatedBean = new ConcurrentHashMap<>(); // 已经实例化的bean

    private static final Map<String, BeanInfo> instantiatingBean = new ConcurrentHashMap<>(); // 正在实例化的bean

    public static final List<Class<?>> beanAnnotation = new ArrayList<>(); // 需要扫描为bean的注解

    private static final Map<Class<?>, List<Function<Object,Object>>> beanMethod = new ConcurrentHashMap<>(); // bean方法,创建bean后调用

    public static void addBeanAnnotation(Class<?>... clz){
        beanAnnotation.addAll(Arrays.asList(clz));
    }

    public static void createBean(List<Class<?>> clz) {
        for(Class<?> c : clz){
            createBean(c);
        }
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
            Object[] objects = getParams(constructor[0]);
            bean = constructor[0].newInstance(objects);// 实例化
            BeanInfo info = new BeanInfo(beanName, beanType, cls, bean);
            if (!alreadyInstantiatedBean.containsKey(info.getName()))alreadyInstantiatedBean.put(info.getName(), info);// 放入已经实例化的bean
            if(beanType==BeanType.original) {
                beanMap.put(info.getName(), info);// 放入所有bean
            }
            instantiatingBean.remove(info.getName());// 从正在实例化的bean中移除
            Annotation[] annotations = cls.getAnnotations();
            for (Annotation annotation : annotations) {
                List<Function<Object,Object>> funs = beanMethod.get(annotation.annotationType());
                if (funs!=null) {
                    for (Function<Object,Object> fun:funs){
                        bean = fun.apply(bean);
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
    public static void addPostProcessor(Class<?> cls,Function<Object,Object> fun){
        List<Function<Object,Object>> funs = beanMethod.get(cls);
        if(funs==null){
            funs = new ArrayList<>();
            funs.add(fun);
            beanMethod.put(cls,funs);
        }
    }
}
