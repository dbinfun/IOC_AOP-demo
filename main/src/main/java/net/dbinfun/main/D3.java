package net.dbinfun.main;

import net.dbinfun.ioc.annotation.Autowired;
import net.dbinfun.ioc.annotation.Service;

@Service
public class D3 {
    @Autowired
    private D2 d2;
    @Autowired(value = "d1sssss")
    private D1 d1;
    public D3(){

    }

    public void print(){
        System.out.println("D3");
        d1.print();
        d2.print();

    }

    public void setD2(D2 d2) {
        this.d2 = d2;
    }

    public void setD1(D1 d1) {
        this.d1 = d1;
    }
}
