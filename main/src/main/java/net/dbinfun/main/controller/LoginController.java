package net.dbinfun.main.controller;

import net.dbinfun.ioc.annotation.Controller;
import net.dbinfun.ioc.annotation.Request;
import net.dbinfun.main.service.DateService;

import java.util.HashMap;
import java.util.Map;

@Controller(path = "/user")
public class LoginController {
    @Request(path = "/login")
    public Object login(DateService dateService){
        Map<String,String> map = new HashMap<>();
        map.put("name","dbinfun");
        map.put("age","18");
        map.put("sex","man");
        map.put("address","china");
        map.put("date",dateService.getDate());
        return map;
    }
}
