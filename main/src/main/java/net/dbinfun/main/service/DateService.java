package net.dbinfun.main.service;

import net.dbinfun.ioc.annotation.Service;

import java.util.Date;

@Service
public class DateService {
    public String getDate(){
        return new Date().toString();
    }
}
