package net.dbinfun.main;

import net.dbinfun.ioc.annotation.Service;
import net.dbinfun.ioc.beans.BeanType;

@Service(type = BeanType.multivariate,value = "d1sssss")
public class D1 {
    public static int count = 0;
    public int count2;
    private final D2 d2;
    public D1(D2 d2){
        this.count2 = ++count;
        this.d2 = d2;
    }
    public void print(){
        System.out.println("D1:count" + count2);
        d2.print();
    }
}
