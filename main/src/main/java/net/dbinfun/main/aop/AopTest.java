package net.dbinfun.main.aop;

import net.dbinfun.ioc.AopInfo;
import net.dbinfun.ioc.annotation.aop.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Advice
public class AopTest {
    private static final Logger log = LoggerFactory.getLogger(AopTest.class);
    @PointCut(Log.class)
    public void pointCut() {
    }
    @Before("pointCut")
    public void before(AopInfo info){
        log.info("aop before");
    }
    @After("pointCut")
    public void after(AopInfo info){
        log.info("aop after");
    }
    @Throws("pointCut")
    public void throwsException(AopInfo info){
        log.info("aop throws{}",info.getThrowable().getMessage());
    }
}
