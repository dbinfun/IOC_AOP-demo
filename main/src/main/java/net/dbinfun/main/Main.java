package net.dbinfun.main;

import net.dbinfun.ioc.BeanFactory;
import net.dbinfun.ioc.MyIOCApplication;

import javax.xml.crypto.Data;
import java.util.Date;

public class Main {
    public static void main(String[] args) {
        MyIOCApplication.start(Main.class, args);
    }
}
