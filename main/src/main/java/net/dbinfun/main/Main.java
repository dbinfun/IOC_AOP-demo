package net.dbinfun.main;

import net.dbinfun.ioc.BeanFactory;
import net.dbinfun.ioc.MyIOCApplication;

import javax.xml.crypto.Data;
import java.util.Date;

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
        D3 d3 = BeanFactory.getBean(D3.class);
        d3.print();
        Object b = BeanFactory.getBean(Date.class);
        System.out.println(b);
        b = BeanFactory.getBean(Date.class);
        System.out.println(b);

    }
}
