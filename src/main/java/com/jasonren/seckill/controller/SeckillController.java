package com.jasonren.seckill.controller;

import com.jasonren.seckill.domain.OrderInfo;
import com.jasonren.seckill.domain.SeckillOrder;
import com.jasonren.seckill.domain.SeckillUser;
import com.jasonren.seckill.redis.RedisService;
import com.jasonren.seckill.result.CodeMsg;
import com.jasonren.seckill.result.Result;
import com.jasonren.seckill.service.GoodsService;
import com.jasonren.seckill.service.OrderService;
import com.jasonren.seckill.service.SeckillService;
import com.jasonren.seckill.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/seckill")
public class SeckillController {

    private Logger logger = LoggerFactory.getLogger(SeckillOrder.class);

    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    SeckillService seckillService;

    @RequestMapping(value = "/do_seckill", method = RequestMethod.POST)
    @ResponseBody
    public Result<OrderInfo> list(Model model, SeckillUser user,
                                  @RequestParam("goodsId") long goodsId) {
        model.addAttribute("user", user);
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        //判断库存
        GoodsVo goods = goodsService.getGoodsVOById(goodsId);
        int stock = goods.getStockCount();
        if (stock <= 0) {
            return Result.error(CodeMsg.SECKILL_OVER);
        }

        //判断是否秒杀到了
        SeckillOrder order = orderService.getSeckillOrderById(user.getId(), goodsId);
        if (order != null) {
            return Result.error(CodeMsg.REPEATE_SECKILL);
        }
        //减库存 下订单 写入秒杀订单
        OrderInfo orderInfo = seckillService.seckill(user, goods);


        return Result.success(orderInfo);
    }
}
