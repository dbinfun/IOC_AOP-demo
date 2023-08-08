package net.dbinfun.ioc;

import net.dbinfun.ioc.annotation.Autowired;
import net.dbinfun.ioc.annotation.Service;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
@Service
public class ApplicationTest {
    @BeforeAll
    public static void before() {
        MyIOCApplication.start(ApplicationTest.class,null);
    }
    @Test
    public void test(String t) {
        Assertions.assertEquals(t,"a");
    }
}
