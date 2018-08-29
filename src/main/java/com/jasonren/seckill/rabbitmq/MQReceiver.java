package com.jasonren.seckill.rabbitmq;

import com.jasonren.seckill.domain.Goods;
import com.jasonren.seckill.domain.SeckillOrder;
import com.jasonren.seckill.domain.SeckillUser;
import com.jasonren.seckill.redis.RedisService;
import com.jasonren.seckill.service.GoodsService;
import com.jasonren.seckill.service.OrderService;
import com.jasonren.seckill.service.SeckillService;
import com.jasonren.seckill.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQReceiver {

    private static Logger logger = LoggerFactory.getLogger(MQReceiver.class);

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    SeckillService seckillService;


    @RabbitListener(queues = MQConfig.SECKILL_QUEUE)
    public void receive (String message) {
        logger.info("receive message: " + message);
        SeckillMessage seckillMessage = RedisService.stringToBean(message, SeckillMessage.class);
        SeckillUser user = seckillMessage.getSeckillUser();
        long goodsId = seckillMessage.getGoodsId();

        //判断库存
        GoodsVo goods = goodsService.getGoodsVOById(goodsId);
        int stock = goods.getStockCount();
        if (stock < 1) {
            return;
        }

        //判断是否已经秒杀到了
        SeckillOrder order = orderService.getSeckillOrderById(user.getId(), goodsId);
        if (order != null) {
            return;
        }
        //减库存 下订单 写入秒杀订单
        seckillService.seckill(user, goods);
    }


    // /**
    //  * Direct模式 交换机
    //  * @param message
    //  */
    // @RabbitListener(queues = MQConfig.QUEUE)
    // public void receive (String message) {
    //     logger.info("receive message: " + message);
    // }
    //
    //
    // @RabbitListener(queues = MQConfig.TOPIC_QUEUE1)
    // public void receiveTopic1 (String message) {
    //     logger.info("topic1 message: " + message);
    // }
    //
    // @RabbitListener(queues = MQConfig.TOPIC_QUEUE2)
    // public void receiveTopic2 (String message) {
    //     logger.info("topic2 message: " + message);
    // }
    //
    // @RabbitListener(queues = MQConfig.HEADER_QUEUE)
    // public void receiveHeaderQueue (byte[] message) {
    //     logger.info("header queue message: " + new String(message));
    // }



}
