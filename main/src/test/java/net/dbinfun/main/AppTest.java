package net.dbinfun.main;


import net.dbinfun.ioc.MyIOCApplication;
import net.dbinfun.main.bean.D2;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class AppTest {
    @BeforeAll
    public static void before() {
        MyIOCApplication.start(Main.class,null);
    }
    @Test
    public void test() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(D2.class);
        enhancer.setCallback(new Interceptor());
        D2 d2 = (D2) enhancer.create();
        d2.print();
    }
}
class Interceptor implements MethodInterceptor{

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        System.out.println("hello");
        Object result = methodProxy.invokeSuper(o,objects);
        System.out.println("world");
        return result;
    }
}
