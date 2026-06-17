package com.interview.evaluation.config;

import com.alibaba.fastjson.JSON;
import com.interview.evaluation.common.Result;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");

        String message = "登录失败";
        if (exception instanceof BadCredentialsException) {
            message = "用户名或密码错误";
        } else if (exception instanceof DisabledException) {
            message = "账户已被禁用";
        }

        response.getWriter().write(JSON.toJSONString(Result.error(401, message)));
    }
}
