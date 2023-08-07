package net.dbinfun.main;

import net.dbinfun.ioc.annotation.Bean;
import net.dbinfun.ioc.annotation.Component;

import java.util.Date;

@Component
public class B1 {
    @Bean
    public Date data(){
        return new Date();
    }
}
