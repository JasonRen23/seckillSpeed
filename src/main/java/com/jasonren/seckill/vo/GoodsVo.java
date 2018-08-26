package com.jasonren.seckill.vo;

import com.jasonren.seckill.domain.Goods;
import lombok.Data;

import java.util.Date;

@Data
public class GoodsVo extends Goods {
    private Double seckillPrice;
    private Integer stockCount;
    private Date startTime;
    private Date endTime;

}
