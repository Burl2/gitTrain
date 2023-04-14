package com.atguigu.auth;

import com.atguigu.auth.mapper.SysRoleMapper;
import com.atguigu.model.system.SysRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
public class TestMpDemo1 {

    @Resource
    private SysRoleMapper mapper;

    @Test
    public void getAll() {

        List<SysRole> sysRolesList = mapper.selectList(null);
        System.out.println(sysRolesList);
    }
}

