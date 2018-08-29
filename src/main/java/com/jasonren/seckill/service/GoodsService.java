package com.jasonren.seckill.service;

import com.jasonren.seckill.dao.GoodsDao;
import com.jasonren.seckill.domain.SeckillGoods;
import com.jasonren.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsService {

    @Autowired
    GoodsDao goodsDao;

    public List<GoodsVo> listGoodsVo() {
        return goodsDao.listGoodsVO();
    }


    public GoodsVo getGoodsVOById(final long goodsId) {
        return goodsDao.getGoodsVOByGoodsId(goodsId);
    }

    public boolean reduceStock(final GoodsVo goods) {
        SeckillGoods g = new SeckillGoods();
        g.setGoodsId(goods.getId());
        int ret = goodsDao.reduceStock(g);
        return ret > 0;
    }
}
