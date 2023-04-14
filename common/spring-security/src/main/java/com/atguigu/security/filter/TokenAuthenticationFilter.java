package com.atguigu.security.filter;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.jwt.JwtHelper;
import com.atguigu.common.result.ResponseUtil;
import com.atguigu.common.result.Result;
import com.atguigu.common.result.ResultCodeEnum;
import com.atguigu.security.custom.LoginUserInfoHelper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private RedisTemplate redisTemplate;
    public TokenAuthenticationFilter(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        logger.info("uri:"+request.getRequestURI());
        //如果是登录接口，直接放行
        if("/admin/system/index/login".equals(request.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
        if(null != authentication) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            chain.doFilter(request, response);
        } else {
            ResponseUtil.out(response, Result.build(null, ResultCodeEnum.PERMISSION));
        }
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {

        String token = request.getHeader("token");
        System.out.println(token);
        if (!StringUtils.isEmpty(token)) {
            String username = JwtHelper.getUsername(token);
            if (!StringUtils.isEmpty(username)) {

                LoginUserInfoHelper.setUserId(JwtHelper.getUserId(token));
                LoginUserInfoHelper.setUsername(username);
                String authString = (String) redisTemplate.opsForValue().get(username);

                if (!StringUtils.isEmpty(authString)) {
                    List<Map> maps = JSON.parseArray(authString, Map.class);
                    ArrayList<SimpleGrantedAuthority> authList = new ArrayList<>();
                    for (Map map : maps) {
                        String authority = (String) map.get("authority");
                        authList.add(new SimpleGrantedAuthority(authority));
                    }
                    return new UsernamePasswordAuthenticationToken(username,null, authList);
                }
                else {
                    return new UsernamePasswordAuthenticationToken(username,null, new ArrayList<>());
                }
            }
        }
        return null;

        // token置于header里
//        String token = request.getHeader("token");
//        logger.info("token:"+token);
//        if (!StringUtils.isEmpty(token)) {
//            String username = JwtHelper.getUsername(token);
//            logger.info("useruame:"+username);
//            if (!StringUtils.isEmpty(username)) {
//                String authoritiesString = (String) redisTemplate.opsForValue().get(username);
//                List<Map> mapList = JSON.parseArray(authoritiesString, Map.class);
//                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
//                for (Map map : mapList) {
//                    authorities.add(new SimpleGrantedAuthority((String)map.get("authority")));
//                }
//                return new UsernamePasswordAuthenticationToken(username, null, authorities);
//            } else {
//                return new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());
//            }
//        }
//
//        return null;

    }
}
