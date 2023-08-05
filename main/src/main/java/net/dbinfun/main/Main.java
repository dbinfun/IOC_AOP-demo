package net.dbinfun.main;

import net.dbinfun.ioc.BeanFactory;
import net.dbinfun.ioc.MyIOCApplication;
public class Main {
    public static void main(String[] args) {
        MyIOCApplication.start(Main.class, args);
        D1 d1 = BeanFactory.getBean(D1.class);
        d1.print();
        d1 = BeanFactory.getBean(D1.class);
        d1.print();
        d1 = BeanFactory.getBean(D1.class);
        d1.print();
        d1 = BeanFactory.getBean(D1.class);
        d1.print();
        d1 = BeanFactory.getBean(D1.class);
        d1.print();
        d1 = BeanFactory.getBean(D1.class);
        d1.print();

    }
}
