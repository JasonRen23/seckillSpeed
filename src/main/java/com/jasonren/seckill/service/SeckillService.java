package com.jasonren.seckill.service;


import com.jasonren.seckill.domain.OrderInfo;
import com.jasonren.seckill.domain.SeckillOrder;
import com.jasonren.seckill.domain.SeckillUser;
import com.jasonren.seckill.redis.RedisService;
import com.jasonren.seckill.redis.SeckillKey;
import com.jasonren.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SeckillService {

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    RedisService redisService;

    @Transactional
    public OrderInfo seckill(SeckillUser user, GoodsVo goods) {
        //减库存 下订单 写入秒杀订单
        boolean success = goodsService.reduceStock(goods);

        if (success) {
            return orderService.createOrder(user, goods);
        } else {
            setGoodsOver(goods.getId());
        }
        //order_info seckill_order
        return null;
    }



    public long getSeckillResult(Long userId, long goodsId) {
        SeckillOrder order = orderService.getSeckillOrderById(userId, goodsId);
        if (order != null) { //秒杀成功
            return order.getOrderId();
        } else {
            boolean isOver = getGoodsOver(goodsId);
            if (isOver) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    private boolean getGoodsOver(final long goodsId) {
        return redisService.exists(SeckillKey.isGoodsOver, "" + goodsId);
    }

    private void setGoodsOver(final Long goodsId) {
        redisService.set(SeckillKey.isGoodsOver, "" + goodsId, true);
    }

    public void reset(final List<GoodsVo> goodsList) {
        goodsService.resetStock(goodsList);
        orderService.deleteOrders();
    }
}
