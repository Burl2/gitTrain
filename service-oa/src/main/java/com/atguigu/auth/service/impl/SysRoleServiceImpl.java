package com.atguigu.auth.service.impl;

import com.atguigu.auth.mapper.SysRoleMapper;
import com.atguigu.auth.mapper.SysUserRoleMapper;
import com.atguigu.auth.service.SysRoleService;
import com.atguigu.auth.service.SysUserRoleService;
import com.atguigu.model.system.SysRole;
import com.atguigu.model.system.SysUserRole;
import com.atguigu.vo.system.AssginRoleVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {
    @Autowired
    private SysUserRoleService sysUserRoleService;


//    @Autowired
//    private SysUserRoleMapper sysUserRoleMapper;


    @Override
    public Map<String, Object> findRoleByAdminId(Long userId) {

        List<SysRole> allRoleList = baseMapper.selectList(null);

        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId,userId);
        List<SysUserRole> existUserRoleList = sysUserRoleService.list(wrapper);
        List<Long> existRoleIdList = existUserRoleList.stream()
                .map(c -> c.getRoleId()).collect(Collectors.toList());

        ArrayList<SysRole> assginRoleList = new ArrayList<>();
        for (SysRole sysRole : allRoleList) {
            if (existRoleIdList.contains(sysRole.getId())) {
                assginRoleList.add(sysRole);
            }
        }

        HashMap<String, Object> roleMap = new HashMap<>();
        roleMap.put("assginRoleList", assginRoleList);
        roleMap.put("allRolesList", allRoleList);
        return roleMap;

    }

//    @Override
//    public Map<String, Object> findRoleByAdminId(Long userId) {
//        //查询所有的角色
//        List<SysRole> allRolesList = this.list();
//
//        //拥有的角色id
//        List<SysUserRole> existUserRoleList = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId).select(SysUserRole::getRoleId));
//        List<Long> existRoleIdList = existUserRoleList.stream().map(c->c.getRoleId()).collect(Collectors.toList());
//
//        //对角色进行分类
//        List<SysRole> assginRoleList = new ArrayList<>();
//        for (SysRole role : allRolesList) {
//            //已分配
//            if(existRoleIdList.contains(role.getId())) {
//                assginRoleList.add(role);
//            }
//        }
//
//        Map<String, Object> roleMap = new HashMap<>();
//        roleMap.put("assginRoleList", assginRoleList);
//        roleMap.put("allRolesList", allRolesList);
//        return roleMap;
//    }

    @Override
    public void doAssign(AssginRoleVo assginRoleVo) {

        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId,assginRoleVo.getUserId());
        sysUserRoleService.remove(wrapper);

        List<Long> roleIdList = assginRoleVo.getRoleIdList();
        for (Long roleId : roleIdList) {

            if (StringUtils.isEmpty(roleId)) {
                continue;
            }

            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setRoleId(roleId);
            sysUserRole.setUserId(assginRoleVo.getUserId());
            sysUserRoleService.save(sysUserRole);
        }
    }
}
