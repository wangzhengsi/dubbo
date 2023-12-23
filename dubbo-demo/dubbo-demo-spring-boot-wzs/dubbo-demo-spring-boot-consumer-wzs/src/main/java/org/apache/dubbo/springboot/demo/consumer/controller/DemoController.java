package org.apache.dubbo.springboot.demo.consumer.controller;

import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.springboot.demo.TempuraService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wangzhengsi
 * @since 2023/12/23 15:36
 */
@RestController
@RequestMapping("/test")
public class DemoController {

    @DubboReference(check = false)
    private TempuraService tempuraService;

    @GetMapping("/hello")
    public Object hello(@RequestParam(value = "msg", required = false) String msg) {
        return tempuraService.send(msg);
    }
}
