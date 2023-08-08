package net.dbinfun.main.aop;

import net.dbinfun.ioc.annotation.aop.Advice;
import net.dbinfun.ioc.annotation.aop.PointCut;

@Advice
public class AopTest {
    @PointCut(Log.class)
    public void pointCut() {
    }
}
