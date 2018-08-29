package com.jasonren.seckill.vo;

import com.jasonren.seckill.domain.SeckillUser;
import lombok.Data;

@Data
public class GoodsDetailVo {

    private int seckillStatus = 0;
    private int remainSeconds = 0;
    private GoodsVo goods;
    private SeckillUser seckillUser;

}
