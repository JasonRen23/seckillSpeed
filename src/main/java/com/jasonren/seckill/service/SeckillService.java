package com.jasonren.seckill.service;


import com.jasonren.seckill.domain.OrderInfo;
import com.jasonren.seckill.domain.SeckillUser;
import com.jasonren.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeckillService {

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Transactional
    public OrderInfo seckill(SeckillUser user, GoodsVo goods) {
        //减库存 下订单 写入秒杀订单
        boolean success = goodsService.reduceStock(goods);

        if (success) {
            return orderService.createOrder(user, goods);
        }
        //order_info seckill_order
        return null;
    }
}
