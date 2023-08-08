package net.dbinfun.ioc;

import net.dbinfun.ioc.annotation.aop.*;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AopMannager {
    private static final Logger log = LoggerFactory.getLogger(AopMannager.class);
    private static Map<Class<?>,Boolean> isProxy = new ConcurrentHashMap<>();
    public static void init(){
        // 扫描所有的切面
        BeanFactory.addPreprocessing((cls)-> (Arrays.stream(cls.getAnnotations()).map(Annotation::annotationType)
                        .collect(Collectors.toList())
                        .contains(Advice.class)),
                (cls)->{
                    Method[] methods = cls.getDeclaredMethods();
                    for (Method method : methods) {
                        // 找到切点
                        Annotation annotation = method.getAnnotation(PointCut.class);
                        if (annotation!=null){
                            // 注解值
                            Class<?>[] clist = (Class<?>[])BeanUtil.getAnnotationValue(annotation,"value");
                            Method before = null;
                            Method after = null;
                            Method throwsException = null;
                            for(Method me:methods){
                                if(me.getAnnotation(Before.class)!=null&&me.getParameterTypes().length==1&&me.getParameterTypes()[0]==AopInfo.class&&method.getName().equals(BeanUtil.getAnnotationValue(me.getAnnotation(Before.class),"value"))){
                                    before = me;
                                }else if(me.getAnnotation(After.class)!=null&&me.getParameterTypes().length==1&&me.getParameterTypes()[0]==AopInfo.class&&method.getName().equals(BeanUtil.getAnnotationValue(me.getAnnotation(After.class),"value"))){
                                    after = me;
                                }else if(me.getAnnotation(Throws.class)!=null&&me.getParameterTypes().length==1&&me.getParameterTypes()[0]==AopInfo.class&&method.getName().equals(BeanUtil.getAnnotationValue(me.getAnnotation(Throws.class),"value"))){
                                    throwsException = me;
                                }
                            }
                            // 添加后置处理器
                            for(Class<?> c:clist){
                                addPostProcessor(c,cls,before,after,throwsException);
                            }
                        }
                    }
                    return null;
                });
    }

    /**
     * 添加后置处理器
     * @param pointCut 注解的class
     */
    private static void addPostProcessor(Class pointCut,Class<?> adviceClass,Method before,Method after,Method throwsException){
        Predicate<Class<?>> predicate = (cls)->{
            Boolean is = isProxy.get(cls);
            if(is!=null){
                return is;
            }
            Method[] methods = cls.getMethods();// 获取方法
            for (Method method : methods) {
                Annotation annotation;
                annotation = method.getAnnotation(pointCut);// 获取对应的注解
                if (annotation!=null){
                    isProxy.put(cls,true);
                    return true;
                }
            }
            isProxy.put(cls,false);
            return false;
        };
        Function<Object,Object> function = (object)->{
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(object.getClass());
            enhancer.setCallback((MethodInterceptor) (o, method, objects, methodProxy) -> {
                Object re = null;
                AopInfo info = new AopInfo(method.getDeclaringClass(),method.getName(),method.getParameterTypes(),objects,method.getReturnType(),null);
                if(before!=null){before.invoke(BeanFactory.getBean(adviceClass),info);}
                if(throwsException==null){
                    re =  methodProxy.invokeSuper(o,objects);
                    info.setResult(re);
                    if(after!=null){
                        after.invoke(BeanFactory.getBean(adviceClass),info);
                    }
                }
                else{
                    try {
                        re =  methodProxy.invokeSuper(o,objects);
                        info.setResult(re);
                        if(after!=null){
                            after.invoke(BeanFactory.getBean(adviceClass),info);
                        }
                    }catch (Throwable e) {
                        info.setThrowable(e);
                        throwsException.invoke(BeanFactory.getBean(adviceClass), info);
                        return null;
                    }
                }
                return re;
            });
            Class<?>[] exceptTypes = object.getClass().getConstructors()[0].getExceptionTypes();
            Object[] params = BeanFactory.getParams(object.getClass().getConstructors()[0]);
            return enhancer.create(exceptTypes,params);
        };
        BeanFactory.addProxyProcessing(predicate,function);
    }
}
