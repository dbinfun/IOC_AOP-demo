package net.dbinfun.ioc;

import net.dbinfun.ioc.beans.BeanType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.List;

public class BeanUtil {
    private static final Logger log = LoggerFactory.getLogger(BeanUtil.class);
    private static final List<Class<?>> beanAnnotation = BeanFactory.beanAnnotation;
    /**
     * 获取注解的值
     *
     * @param annotation   注解
     * @param propertyName 属性名
     * @return 属性值
     */
    public static Object getAnnotationValue(Annotation annotation, String propertyName) {
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
    public static String getBeanName(Class<?> clz) {
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
        if (name==null||"".equals(name)) {
            name = clz.getName();
        }
        return name;
    }

    public static BeanType getBeanType(Class<?> clz) {
        Annotation[] annotation = clz.getAnnotations();
        BeanType beanType = null;
        for (Annotation a : annotation) {
            if (beanAnnotation.contains(a.annotationType())) {
                // 获取a的value
                beanType = (BeanType) getAnnotationValue(a, "type");
                if (beanType == null) {
                    beanType=BeanType.original;
                }
                break;
            }
        }
        return beanType;
    }
}
