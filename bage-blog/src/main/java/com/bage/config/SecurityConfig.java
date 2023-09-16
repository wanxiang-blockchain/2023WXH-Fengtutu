package com.bage.config;
/*
* 重写authenticationManagerBean，并使用它来进行用户认证
* */

import com.bage.filter.JwtAuthenticationTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    @Bean //这一段配置用于登录时认证，只有使用了这个配置才能自动注入authenticationManagerBean，并使用它来进行用户认证
    public AuthenticationManager authenticationManagerBean() throws Exception {//重写方法，暴露authenticationManager到容器中
        return super.authenticationManagerBean();
    }

    @Autowired
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
    @Autowired
    AuthenticationEntryPoint authenticationEntryPoint;//自定义的认证失败处理器
    @Autowired
    AccessDeniedHandler accessDeniedHandler;//自定义的授权失败处理器

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            //关闭csrf
            .csrf().disable()
            //不通过Session获取SecurityContext
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            // 对于登录接口 允许匿名访问
            .antMatchers("/login").anonymous()
            //jwt过滤器测试用，如果测试没有问题吧这里删除了
//            .antMatchers("/link/getAllLink").authenticated()
            //注销接口需要认证才能访问
            .antMatchers("/logout").authenticated()
            .antMatchers("/user/userInfo").authenticated()
//                .antMatchers("/upload").authenticated()
            // 除上面外的所有请求全部不需要认证即可访问
            .anyRequest().permitAll();

        //配置异常处理器
        http.exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint)//将自定义的认证失败处理器配置给SpringSecurity，传入的是对应的实现类对象
                .accessDeniedHandler(accessDeniedHandler);//将自定义的授权失败处理器配置给SpringSecurity，传入的是对应的实现类对象
        //关闭默认的注销功能，配置我们的退出登录接口需要认证才能访问
        http.logout().disable();
        //把jwtAuthenticationTokenFilter添加到SpringSecurity的过滤器链中
        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);//将过滤器写到UsernamePasswordAuthenticationFilter之前
        //允许跨域
        http.cors();
    }

    //使用BCryptPasswordEncoder进行密码加密
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
