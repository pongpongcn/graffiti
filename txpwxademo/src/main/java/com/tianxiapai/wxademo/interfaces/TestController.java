package com.tianxiapai.wxademo.interfaces;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
	@RequestMapping("/ping")
    public String ping() {
        return "Pong!";
    }
}
