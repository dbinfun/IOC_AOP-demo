package net.dbinfun.ioc;

import net.dbinfun.ioc.annotation.aop.Advice;
import net.dbinfun.ioc.annotation.aop.PointCut;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AopMannager {
    private static final Logger log = LoggerFactory.getLogger(AopMannager.class);
    public static void init(){
        // 扫描所有的切面
        BeanFactory.addPreprocessing((cls)-> (Arrays.stream(cls.getAnnotations()).map(Annotation::annotationType)
                        .collect(Collectors.toList())
                        .contains(Advice.class)),
                (cls)->{
                    Method[] methods = cls.getMethods();
                    for (Method method : methods) {
                        // 找到切点
                        Annotation annotation = method.getAnnotation(PointCut.class);
                        if (annotation!=null){
                            // 注解值
                            Class<?>[] clist = (Class<?>[])BeanUtil.getAnnotationValue(annotation,"value");
                            // 添加后置处理器
                            for(Class<?> c:clist){
                                addPostProcessor(c);
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
    private static void addPostProcessor(Class pointCut){
        Predicate<Class<?>> predicate = (cls)->{
            Method[] methods = cls.getMethods();// 获取方法
            for (Method method : methods) {
                Annotation annotation=null;
                annotation = method.getAnnotation(pointCut);// 获取对应的注解
                if (annotation!=null){
                    return true;
                }
            }
            return false;
        };
        Function<Object,Object> function = (object)->{
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(object.getClass());
            enhancer.setCallback((MethodInterceptor) (o, method, objects, methodProxy) -> {
                log.info("before");
                Object re =  methodProxy.invokeSuper(o,objects);
                log.info("after");
                return re;
            });
            Class<?>[] exceptTypes = object.getClass().getConstructors()[0].getExceptionTypes();
            Object[] params = BeanFactory.getParams(object.getClass().getConstructors()[0]);
            return enhancer.create(exceptTypes,params);
        };
        BeanFactory.addProxyProcessing(predicate,function);
    }
}
