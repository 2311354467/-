package com.lz.vhr.config;

import com.lz.vhr.model.Menu;
import com.lz.vhr.model.Role;
import com.lz.vhr.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.Collection;
import java.util.List;

//接收前端传来的 url 判断该 url 所需要的角色
@Component
public class MyFilterInvocationSecurityMetadataSource implements FilterInvocationSecurityMetadataSource {

    //spring 基于 Ant 的路径匹配
    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Autowired
    private MenuService menuService;

    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
        //这个 getAttributes(Object object)就是当前用户

        //获取当前用户请求的 url
        String requestUrl = ((FilterInvocation) object).getRequestUrl();

        List<Menu> menus = menuService.getMenusAndRoles();

        for (Menu menu : menus) {
            if (antPathMatcher.match(menu.getUrl(), requestUrl)) {
                List<Role> roles = menu.getRoles();

                String[] rolesStr = new String[roles.size()];

                for (int i = 0; i < roles.size(); i++) {
                    rolesStr[i] = roles.get(i).getName();
                }

                return SecurityConfig.createList(rolesStr);

            }
        }


        return SecurityConfig.createList("ROLE_login");
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    //打开
    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }
}
