package net.dbinfun.ioc;

import cn.hutool.http.HttpUtil;
import cn.hutool.http.server.SimpleServer;
import cn.hutool.http.server.action.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class HttpServer {
    private static final Logger log = LoggerFactory.getLogger(HttpServer.class);
    private static SimpleServer server=null;
    private static boolean isStarted = false;

    public static final Set<Class<?>> simpleType = new HashSet<>(Arrays.asList(String.class,Character.class,Boolean.class,Integer.class,Long.class,Float.class,Double.class));
    private HttpServer(){}

    public static void init(int port){
        if(isStarted) {
            log.warn("HttpServer has been started");
            return;
        }
        server = HttpUtil.createServer(8080);
    }
    public static void start(){
        if(server==null){
            log.error("HttpServer has not been initialized");
            return;
        }
        isStarted = true;
        server.start();
    }

    public static void addRequestMapping(Action action, String... path){
        if (path.length==0){
            log.error("path is empty");
            return;
        }
        for(String p:path){
            server.addAction(p,action);
        }
    }
}
