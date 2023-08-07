package net.dbinfun.main;

import net.dbinfun.ioc.annotation.Service;

@Service
public class D2 {
    public static int count = 0;
    public int count2;
    public D2(){
        this.count2 = ++count;
    }
    public void print(){
        System.out.println("D2:count" + count2);
    }
}
