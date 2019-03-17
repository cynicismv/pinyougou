package cn.itcast.demo;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController

public class UserController {
    @RequestMapping("/findLoginUser")
    public void findLoginUser(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录名:"+name);
    }
}
