package com.jasonren.seckill.rabbitmq;

import com.jasonren.seckill.domain.SeckillUser;
import lombok.Data;

@Data
public class SeckillMessage {
    private SeckillUser seckillUser;
    private long goodsId;

    @Override
    public String toString() {
        return "SeckillMessage{" +
            "seckillUser=" + seckillUser +
            ", goodsId=" + goodsId +
            '}';
    }
}
