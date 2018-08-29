package com.jasonren.seckill.controller;

import com.jasonren.seckill.domain.SeckillUser;
import com.jasonren.seckill.result.Result;
import groovy.util.logging.Slf4j;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/user")
@Slf4j
public class UserController {

    @ApiOperation(value = "获取用户信息", notes = "获取用户信息")
    @RequestMapping("/info")
    @ResponseBody
    public Result<SeckillUser> info(SeckillUser user) {
        return Result.success(user);
    }

}
