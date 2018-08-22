package com.jasonren.seckill.controller;


import com.jasonren.seckill.domain.User;
import com.jasonren.seckill.redis.RedisService;
import com.jasonren.seckill.redis.UserKey;
import com.jasonren.seckill.result.Result;
import com.jasonren.seckill.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/demo")
public class SampleController {

    @Autowired
    UserService userService;

    @Autowired
    RedisService redisService;

    @RequestMapping("/thymeleaf")
    public String thymeleaf(Model model) {
        model.addAttribute("name", "Joshua");
        return "hello";
    }

    @ApiOperation("db测试接口")
    @RequestMapping("/db")
    @ResponseBody
    public Result<User> dbGet() {
        User user = userService.getById(0);
        return Result.success(user);
    }

    @ApiOperation("测试db事务")
    @RequestMapping("/db/tx")
    @ResponseBody
    public Result<Boolean> dbTx() {
        boolean b =userService.tx();
        return Result.success(b);
    }


    @ApiOperation("测试db事务")
    @RequestMapping("/redis/get")
    @ResponseBody
    public Result<User> redisGet() {
        User v1 = redisService.get(UserKey.getById, "" + 1, User.class);
        return Result.success(v1);
    }

    @ApiOperation("测试db事务")
    @RequestMapping("/redis/set")
    @ResponseBody
    public Result<Boolean> redisSet() {
        User user = new User(1, "11111");
        redisService.set(UserKey.getById, "" + 1, user);
        return Result.success(true);
    }

}
