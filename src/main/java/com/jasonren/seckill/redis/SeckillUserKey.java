package com.jasonren.seckill.redis;

public class SeckillUserKey extends BasePrefix{

    public static final int TOKEN_EXPRIRE = 3600 * 24 * 2;

    private SeckillUserKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static SeckillUserKey token = new SeckillUserKey(TOKEN_EXPRIRE, "tk");
    public static SeckillUserKey getById = new SeckillUserKey(0, "id");

}
