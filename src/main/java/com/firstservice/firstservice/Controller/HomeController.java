package com.firstservice.firstservice.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @Value("${app.version}")
    private String appVersion;

    @RequestMapping("/")
    public String  getStatus() {return appVersion;}
}
