package com.jasonren.seckill.controller;


import com.jasonren.seckill.domain.SeckillUser;
import com.jasonren.seckill.redis.RedisService;
import com.jasonren.seckill.service.SeckillUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/goods")
public class GoodsController {
    @Autowired
    SeckillUserService seckillUserService;

    @Autowired
    RedisService redisService;

    @RequestMapping("/to_list")
    public String toLogin(Model model, SeckillUser user) {
        model.addAttribute("user", user);
        return "goods_list";
    }


}
