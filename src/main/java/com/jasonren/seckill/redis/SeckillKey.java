package com.jasonren.seckill.redis;

public class SeckillKey extends BasePrefix{


    private SeckillKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static SeckillKey isGoodsOver = new SeckillKey(0, "go");

}