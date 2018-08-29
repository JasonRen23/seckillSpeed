package com.jasonren.seckill.vo;

import com.jasonren.seckill.domain.OrderInfo;
import lombok.Data;

@Data
public class OrderDetailVo {
    private GoodsVo goods;
    private OrderInfo order;

}
