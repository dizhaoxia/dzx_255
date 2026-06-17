package com.interview.evaluation.controller;

import com.interview.evaluation.common.Result;
import com.interview.evaluation.entity.SysUser;
import com.interview.evaluation.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private SysUserService sysUserService;

    @GetMapping("/userinfo")
    public Result<Map<String, Object>> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        SysUser user = sysUserService.getByUsername(username);
        if (user == null) {
            return Result.error("用户不存在");
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        userInfo.put("role", user.getRole());
        userInfo.put("email", user.getEmail());
        userInfo.put("phone", user.getPhone());

        return Result.success(userInfo);
    }
}
