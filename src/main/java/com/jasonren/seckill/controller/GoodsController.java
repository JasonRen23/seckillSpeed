package com.jasonren.seckill.controller;


import com.alibaba.druid.util.StringUtils;
import com.jasonren.seckill.domain.SeckillUser;
import com.jasonren.seckill.redis.GoodsKey;
import com.jasonren.seckill.redis.RedisService;
import com.jasonren.seckill.result.Result;
import com.jasonren.seckill.service.GoodsService;
import com.jasonren.seckill.service.SeckillUserService;
import com.jasonren.seckill.vo.GoodsDetailVo;
import com.jasonren.seckill.vo.GoodsVo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    GoodsService goodsService;

    @Autowired
    SeckillUserService seckillUserService;

    @Autowired
    RedisService redisService;

    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;

    @Autowired
    ApplicationContext applicationContext;


    @RequestMapping(value = "/to_list", produces = "text/html")
    @ResponseBody
    public String toLogin(HttpServletRequest request, HttpServletResponse response, Model model, SeckillUser user) {
        model.addAttribute("user", user);

        //取缓存
        String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
        if (!StringUtils.isEmpty(html)) {
            return html;
        }
        //查询商品列表
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        model.addAttribute("goodsList", goodsList);
        // return "goods_list";

        SpringWebContext ctx = new SpringWebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap(), applicationContext);
        //渲染
        html = thymeleafViewResolver.getTemplateEngine().process("goods_list", ctx);
        if (!StringUtils.isEmpty(html)) {
            redisService.set(GoodsKey.getGoodsList, "", html);
        }
        return html;
    }


    @RequestMapping(value = "/to_detail2/{goodsId}", produces = "text/html")
    @ResponseBody
    public String detail2(HttpServletRequest request, HttpServletResponse response, Model model, SeckillUser user,
                          @PathVariable("goodsId") long goodsId) {

        model.addAttribute("user", user);

        String html = redisService.get(GoodsKey.getGoodsDetail, "" + goodsId, String.class);
        if (!StringUtils.isEmpty(html)) {
            return html;
        }

        GoodsVo goods = goodsService.getGoodsVOById(goodsId);
        model.addAttribute("goods", goods);

        long startAt = goods.getStartTime().getTime();
        long endAt = goods.getEndTime().getTime();
        long now = System.currentTimeMillis();

        int seckillStatus = 0;
        int remainSeconds = 0;

        if (now < startAt) {       // 秒杀还没开始 倒计时
            seckillStatus = 0;
            remainSeconds = (int) ((startAt - now) / 1000);
        } else if (now > endAt) { //秒杀已经结束
            seckillStatus = 2;
            remainSeconds = -1;
        } else {                 //秒杀进行中
            seckillStatus = 1;
            remainSeconds = 0;
        }

        model.addAttribute("seckillStatus", seckillStatus);
        model.addAttribute("remainSeconds", remainSeconds);

        // return "goods_detail";
        SpringWebContext ctx = new SpringWebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap(), applicationContext);
        //渲染
        html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", ctx);
        if (!StringUtils.isEmpty(html)) {
            redisService.set(GoodsKey.getGoodsList, "" + goodsId, html);
        }

        return html;
    }

    @GetMapping(value = "/detail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> detail(HttpServletRequest request, HttpServletResponse response, Model model, SeckillUser user,
                                        @PathVariable("goodsId") long goodsId) {


        GoodsVo goods = goodsService.getGoodsVOById(goodsId);

        long startAt = goods.getStartTime().getTime();
        long endAt = goods.getEndTime().getTime();

        Map<String, Integer> map = checkTime(startAt, endAt);

        GoodsDetailVo goodsVo = new GoodsDetailVo();
        goodsVo.setGoods(goods);
        goodsVo.setSeckillUser(user);
        goodsVo.setRemainSeconds(map.get("remainSeconds"));
        goodsVo.setSeckillStatus(map.get("seckillStatus"));


        return Result.success(goodsVo);
    }

    private Map<String, Integer> checkTime(long startAt, long endAt) {
        Map<String, Integer> map = new HashMap<>();

        long now = System.currentTimeMillis();
        int seckillStatus = 0;
        int remainSeconds = 0;

        if (now < startAt) {
            //秒杀还没开始，倒计时
            seckillStatus = 0;
            remainSeconds = (int) ((startAt - now) / 1000);
        } else if (now > endAt) {
            //秒杀已经结束
            seckillStatus = 2;
            remainSeconds = -1;
        } else {
            //秒杀进行中
            seckillStatus = 1;
            remainSeconds = 0;
        }
        map.put("seckillStatus", seckillStatus);
        map.put("remainSeconds", remainSeconds);
        return map;

    }

}
