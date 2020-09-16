package com.lz.vhr.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("hello")
    public String test() {
        return "hello";
    }

    @RequestMapping("/employee/basic/hello")
    public String test2(){
        return "/employee/basic/hello";
    }

    @RequestMapping("/employee/advanced/hello")
    public String test3(){
        return "/employee/advanced/hello";
    }


}
