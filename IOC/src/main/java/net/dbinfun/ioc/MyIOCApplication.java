package net.dbinfun.ioc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyIOCApplication {
    public static final Logger log = LoggerFactory.getLogger(MyIOCApplication.class);
    public static void start(Class<?> cls, String[] args) {
        if(cls==null) {
            log.error("No application class provided");
            return;
        }
        System.out.println("Application started");
    }
}
