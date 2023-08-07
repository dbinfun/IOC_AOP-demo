package net.dbinfun.ioc;

import net.dbinfun.ioc.annotation.*;
import net.dbinfun.ioc.beans.BeanInfo;
import net.dbinfun.ioc.beans.BeanType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

public class BeanPostProcessor {
    private static final Logger log = LoggerFactory.getLogger(BeanPostProcessor.class);
    public static void todo(){
        add(Component.class, BeanPostProcessor::createBeanByMethod,BeanPostProcessor::autowired);
        add(Service.class,BeanPostProcessor::autowired);
        add(Controller.class,BeanPostProcessor::autowired);
    }

    private static void add(Class<?> cls, Function<Object,Object> ...function){
        for (Function<Object, Object> f : function) {
            BeanFactory.addPostProcessor(cls,f);
        }
    }
    /**
     * 通过@Component下的@Bean注解创建bean
     * @param object bean
     */
    private static Object createBeanByMethod(Object object) {
        Class<?> cls = object.getClass();
        Annotation annotation = cls.getAnnotation(Component.class);
        if (annotation!=null){
            Method[] methods = cls.getDeclaredMethods();
            for (Method method : methods) {
                Annotation a = method.getAnnotation(Bean.class);
                if (a!=null){
                    method.setAccessible(true);
                    String beanName = method.getReturnType().getName();
                    Object[] params = BeanFactory.getParams(method);
                    try {
                        Object  re=  method.invoke(object,params);
                        if (re!=null){
                            BeanInfo beanInfo = new BeanInfo(beanName, BeanType.original,cls,re);
                            BeanFactory.addBean(beanName,beanInfo);
                        }
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
        return object;
    }

    /**
     * 通过@Autowired注解注入bean
     * @param object
     * @return
     */
    private static Object autowired(Object object){
        Class<?> cls = object.getClass();
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            Annotation annotation = field.getAnnotation(Autowired.class);
            if (annotation!=null){
                String methodName = "set"+field.getName().substring(0,1).toUpperCase()+field.getName().substring(1);
                try {
                    Method method = cls.getMethod(methodName,field.getType());
                    method.setAccessible(true);
                    String beanName = String.valueOf(BeanUtil.getAnnotationValue(annotation,"value"));
                    if("".equals(beanName)){
                        beanName = field.getType().getName();
                    }
                    method.invoke(object,BeanFactory.getBean(beanName));
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    log.error("注入失败:{}",e.getMessage());
                }
            }
        }
        return object;
    }
}
