package com.jasonren.seckill.service;

import com.jasonren.seckill.dao.OrderDao;
import com.jasonren.seckill.domain.OrderInfo;
import com.jasonren.seckill.domain.SeckillOrder;
import com.jasonren.seckill.domain.SeckillUser;
import com.jasonren.seckill.redis.OrderKey;
import com.jasonren.seckill.redis.RedisService;
import com.jasonren.seckill.vo.GoodsVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class OrderService {

    @Autowired
    OrderDao orderDao;

    @Autowired
    RedisService redisService;

    public SeckillOrder getSeckillOrderById(@Param("userId") long userId,  @Param("goodsId") long goodsId) {
        return redisService.get(OrderKey.getSeckillOrderByUidGid, "" + userId + "_" + goodsId, SeckillOrder.class);
        // return orderDao.getSeckillOrderById(userId, goodsId);
    }

    public OrderInfo getOrderById(final long orderId) {
        return orderDao.getOrderById(orderId);
    }


    @Transactional
    public OrderInfo createOrder(SeckillUser user, GoodsVo goods) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCreateTime(new Date());
        orderInfo.setDeliveryAddrId(0L);
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsId(goods.getId());
        orderInfo.setGoodsName(goods.getGoodsName());
        orderInfo.setGoodsPrice(goods.getSeckillPrice());
        orderInfo.setOrderChannel(1);
        orderInfo.setStatus(0);
        orderInfo.setUserId(user.getId());

        orderDao.insert(orderInfo);
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setGoodsId(goods.getId());
        seckillOrder.setOrderId(orderInfo.getId());
        seckillOrder.setUserId(user.getId());

        orderDao.insertSeckillOrder(seckillOrder);

        redisService.set(OrderKey.getSeckillOrderByUidGid, "" + user.getId() + "_" + goods.getId(), seckillOrder);

        return orderInfo;
    }


    public void deleteOrders() {
        orderDao.deleteOrders();
        orderDao.deleteSeckillOrders();
    }
}
