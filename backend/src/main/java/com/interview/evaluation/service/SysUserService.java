package com.interview.evaluation.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.interview.evaluation.entity.SysUser;
import com.interview.evaluation.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    public SysUser getByUsername(String username) {
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return sysUserMapper.selectOne(queryWrapper);
    }

    public SysUser getById(Long id) {
        return sysUserMapper.selectById(id);
    }
}
