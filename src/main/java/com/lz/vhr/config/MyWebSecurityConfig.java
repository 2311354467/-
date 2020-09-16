package com.lz.vhr.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lz.vhr.model.Hr;
import com.lz.vhr.model.RespBean;
import com.lz.vhr.service.HrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class MyWebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    HrService hrService;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(hrService);
    }

    @Autowired
    private MyFilterInvocationSecurityMetadataSource filterInvocationSecurityMetadataSource;

    @Autowired
    private MyAccessDecisionManager accessDecisionManager;


    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/login");
    }

    /**
     * 我们设置完成之后发现现在两个浏览器都无法登录了，
     * 之所以提示  Maximum sessions of 1 for this principal exceeded
     * 是因为我们虽然退出登录的，但是登录的信息其实还在服务端，所以导致无法登录，原因如下：
     * <p>
     * 因为用户注销需要一个广播事件来广播出去，默认情况下用的是session的广播事件，
     * 但是这个事件spring容器是无法感知到的，所以spring还保存着我们一开始登录的数据没有清除掉，
     * 解决这种情况也很简单。只需要提供一个事件的监听即可
     */
    //监听
    @Bean
    HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .withObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>() {
                    @Override
                    public <O extends FilterSecurityInterceptor> O postProcess(O object) {

                        object.setAccessDecisionManager(accessDecisionManager);
                        object.setSecurityMetadataSource(filterInvocationSecurityMetadataSource);
                        return object;
                    }
                }).and()
                .formLogin()
                .usernameParameter("username")
                .passwordParameter("password")
                .loginProcessingUrl("/doLogin")
                .loginPage("/login")
                .successHandler(new AuthenticationSuccessHandler() {
                    @Override
                    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                        response.setContentType("application/json;charset=utf-8");
                        PrintWriter writer = response.getWriter();

                        //将登陆成功之后的用户信息返回出去这个getPrincipal就是Hr对象
                        Hr hr = (Hr) authentication.getPrincipal();
                        //不要将密码返回
                        hr.setPassword(null);
                        RespBean ok = RespBean.ok("登陆成功", hr);

                        writer.write(new ObjectMapper().writeValueAsString(ok));

                        writer.flush();
                        writer.close();

                    }
                })
                .failureHandler(new AuthenticationFailureHandler() {
                    @Override
                    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
                        response.setContentType("application/json;charset=utf-8");
                        PrintWriter writer = response.getWriter();

                        RespBean error = RespBean.error("登陆失败");

                        if (exception instanceof LockedException) {
                            error.setMsg("账户被锁定，请联系管理员");
                        } else if (exception instanceof CredentialsExpiredException) {
                            error.setMsg("密码过期，请联系管理员");
                        } else if (exception instanceof DisabledException) {
                            error.setMsg("账户被禁用，请联系管理员");
                        } else if (exception instanceof AccountExpiredException) {
                            error.setMsg("账户已过期，请联系管理员");
                        } else if (exception instanceof BadCredentialsException) {
                            error.setMsg("用户名密码错误");
                        }

                        writer.write(new ObjectMapper().writeValueAsString(error));

                        writer.flush();
                        writer.close();
                    }
                }).permitAll()
                .and()
                .logout()
                .logoutSuccessHandler(new LogoutSuccessHandler() {
                    @Override
                    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

                        response.setContentType("application/json;charset=utf-8");
                        PrintWriter writer = response.getWriter();

                        RespBean bean = RespBean.ok("注销成功，欢迎下次光临");

                        writer.write(new ObjectMapper().writeValueAsString(bean));


                        writer.flush();
                        writer.close();

                    }
                })
                .permitAll()
                .and().csrf().disable()

                //用一时间段只能有一个用户登录，不然换个浏览器再次用相同的用户还是能登录的，
                // 为了避免这种情况需要配置一下
                //下面三个方法就说：同一个用户只能登录一个，但是，这次是从数据库里面获取用户名来进行设置的，
                // 所以需要重写实体类中的username的hashcode方法
                .sessionManagement()
                .maximumSessions(1)
                //表示用户登录上去之后，别的浏览器再次登录是不会把已经登录的用户踢出下线的，如果不加的话就会基础下线
                .maxSessionsPreventsLogin(true)


        ;
    }
}
