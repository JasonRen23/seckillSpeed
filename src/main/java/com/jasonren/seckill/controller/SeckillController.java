package com.jasonren.seckill.controller;

import com.jasonren.seckill.domain.SeckillOrder;
import com.jasonren.seckill.domain.SeckillUser;
import com.jasonren.seckill.rabbitmq.MQSender;
import com.jasonren.seckill.rabbitmq.SeckillMessage;
import com.jasonren.seckill.redis.GoodsKey;
import com.jasonren.seckill.redis.OrderKey;
import com.jasonren.seckill.redis.RedisService;
import com.jasonren.seckill.redis.SeckillKey;
import com.jasonren.seckill.result.CodeMsg;
import com.jasonren.seckill.result.Result;
import com.jasonren.seckill.service.GoodsService;
import com.jasonren.seckill.service.OrderService;
import com.jasonren.seckill.service.SeckillService;
import com.jasonren.seckill.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/seckill")
public class SeckillController implements InitializingBean {

    private Logger logger = LoggerFactory.getLogger(SeckillOrder.class);

    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    SeckillService seckillService;

    @Autowired
    MQSender mqSender;


    private Map<Long, Boolean> localOverMap = new HashMap<>();

    /**
     * 系统初始化，将商品信息加载到redis和本地内存
     */
    @Override
    public void afterPropertiesSet() {
        List<GoodsVo> goodsVoList = goodsService.listGoodsVo();
        if (goodsVoList == null) {
            return;
        }

        for (GoodsVo goodsVo : goodsVoList) {
            redisService.set(GoodsKey.getSeckillGoodsStock, "" + goodsVo.getId(), goodsVo.getStockCount());
            localOverMap.put(goodsVo.getId(), false);
        }

    }

    @RequestMapping(value = "/do_seckill", method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> seckill(Model model, SeckillUser user,
                                   @RequestParam("goodsId") long goodsId) {

        model.addAttribute("user", user);
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        //内存标记，减少redis访问
        boolean over = localOverMap.get(goodsId);
        if (over) {
            return Result.error(CodeMsg.SECKILL_OVER);
        }


        //预减库存
        long stock = redisService.decr(GoodsKey.getSeckillGoodsStock, "" + goodsId);
        if (stock < 0) {
            afterPropertiesSet();
            long stock2 = redisService.decr(GoodsKey.getSeckillGoodsStock, "" + goodsId);
            if (stock2 < 0) {
                localOverMap.put(goodsId, true);
                return Result.error(CodeMsg.SECKILL_OVER);
            }
        }


        // //判断库存
        // GoodsVo goods = goodsService.getGoodsVOById(goodsId);
        // int stock = goods.getStockCount();
        // if (stock <= 0) {
        //     return Result.error(CodeMsg.SECKILL_OVER);
        // }

        //判断是否秒杀到了
        SeckillOrder order = orderService.getSeckillOrderById(user.getId(), goodsId);
        if (order != null) {
            return Result.error(CodeMsg.REPEAT_SECKILL);
        }

        //入队
        SeckillMessage seckillMessage = new SeckillMessage();
        seckillMessage.setSeckillUser(user);
        seckillMessage.setGoodsId(goodsId);
        mqSender.sendSeckillMessage(seckillMessage);

        return Result.success(0);
    }

    /**
     * orderId:成功
     * -1：秒杀失败
     * 0： 排队中
     */
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> seckillResult(Model model, SeckillUser user,
                                      @RequestParam("goodsId") long goodsId) {
        model.addAttribute("user", user);
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        long result = seckillService.getSeckillResult(user.getId(), goodsId);
        return Result.success(result);
    }

    @RequestMapping(value = "/reset", method = RequestMethod.GET)
    @ResponseBody
    public Result<Boolean> reset(Model model) {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        for (GoodsVo goods : goodsList) {
            goods.setStockCount(10);
            redisService.set(GoodsKey.getSeckillGoodsStock, "" + goods.getId(), 10);
            localOverMap.put(goods.getId(), false);
        }
        redisService.delete(OrderKey.getSeckillOrderByUidGid);
        redisService.delete(SeckillKey.isGoodsOver);
        seckillService.reset(goodsList);
        return Result.success(true);

    }
}
