package com.community.medical_service.controller;

import com.community.medical_service.mapper.DbDebugMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DbDebugController {

    private final DbDebugMapper dbDebugMapper;

    public DbDebugController(DbDebugMapper dbDebugMapper) {
        this.dbDebugMapper = dbDebugMapper;
    }

    @GetMapping("/debug/ping")
    public Integer ping() {
        return dbDebugMapper.ping();
    }

    @GetMapping("/debug/count-hospital")
    public Long countHospital() {
        return dbDebugMapper.countHospital();
    }

    @GetMapping("/debug/count-sys")
    public Long countSys() {
        return dbDebugMapper.countSys();
    }
}