package net.dbinfun.ioc;

import cn.hutool.http.ContentType;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
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
import java.util.stream.Stream;

public class BeanPostProcessor {
    private static final Logger log = LoggerFactory.getLogger(BeanPostProcessor.class);
    public static void todo(){
        add(Component.class, BeanPostProcessor::createBeanByMethod,BeanPostProcessor::autowired);
        add(Service.class,BeanPostProcessor::autowired);
        add(Controller.class,BeanPostProcessor::autowired,BeanPostProcessor::restController);
    }

    private static void add(Class<?> cls, Function<Object,Object> ...function){
        BeanFactory.addPostProcessor(cls,function);
    }
    /**
     * 通过@Component下的@Bean注解创建bean
     * @param object bean
     */
    private static Object createBeanByMethod(Object object) {
        Class<?> cls = object.getClass();
        if(cls.getAnnotation(Component.class)==null) return object;
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

    private static Object restController(Object object){
        Class<?> cls =object.getClass();
        if(cls.getAnnotation(Controller.class)==null) return object;
        String basePath = String.valueOf(BeanUtil.getAnnotationValue(cls.getAnnotation(Controller.class),"path"));
        Method[] methods = cls.getDeclaredMethods();// 获取公共方法。
        for (Method method : methods) {
            Annotation annotation = method.getAnnotation(Request.class);
            if (annotation!=null){
                String[] path = (String[]) BeanUtil.getAnnotationValue(annotation,"path");
                HttpServer.addRequestMapping((req,res)->{
                    try{
                        // 获取method的返回类型
                        Class<?> returnType = method.getReturnType();
                        // 获取method的参数类型
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        Object[] params = new Object[parameterTypes.length];
                        for (int i=0;i<parameterTypes.length;i++){
                            // request
                            if(parameterTypes[i].equals(HttpRequest.class)){
                                params[i] = req;
                            }
                            // response
                            else if(parameterTypes[i].equals(HttpResponse.class)){
                                params[i] = res;
                            }
                            // 其他bean,从容器中获取,如果需要其他如请求体之类的参数可以通过反射判断注解,从request中取
                            else{
                                params[i] = BeanFactory.getBean(parameterTypes[i]);
                                if(params[i] == null){
                                    throw new Exception("参数"+parameterTypes[i]+"未找到");
                                }
                            }
                        }
                        Object result = method.invoke(object,params);
                        if(returnType!=void.class){
                            if (!HttpServer.simpleType.contains(result.getClass())) {
                                res.write(JSONUtil.toJsonStr(result), ContentType.JSON.toString());
                            }else{
                                res.write(String.valueOf(result), ContentType.TEXT_PLAIN.toString());
                            }
                        }
                    }catch (Exception e) {
                        log.error("调用方法失败:{}", e.getMessage());
                    }
                }, Stream.of(path).map(p->basePath+p).toArray(String[]::new));
            }
        }
        return object;
    }
}
