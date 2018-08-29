package com.jasonren.seckill.controller;


import com.jasonren.seckill.domain.OrderInfo;
import com.jasonren.seckill.domain.SeckillUser;
import com.jasonren.seckill.result.CodeMsg;
import com.jasonren.seckill.result.Result;
import com.jasonren.seckill.service.GoodsService;
import com.jasonren.seckill.service.OrderService;
import com.jasonren.seckill.vo.GoodsVo;
import com.jasonren.seckill.vo.OrderDetailVo;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("/order")
@Slf4j
public class OrderController {

    @Autowired
    OrderService orderService;

    @Autowired
    GoodsService goodsService;

    @ApiOperation("订单详情接口")
    @ApiImplicitParam(name = "orderId", value = "订单ID", required = true, dataType = "Long")
    @RequestMapping("/detail")
    @ResponseBody
    public Result<OrderDetailVo> info(SeckillUser user,
    @RequestParam("orderId") long orderId) {
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        OrderInfo order = orderService.getOrderById(orderId);
        if (order == null) {
            return Result.error(CodeMsg.ORDER_NOT_EXIST);
        }
        long goodsId = order.getGoodsId();
        GoodsVo goods = goodsService.getGoodsVOById(goodsId);
        OrderDetailVo vo = new OrderDetailVo();
        vo.setOrder(order);
        vo.setGoods(goods);

        return Result.success(vo);
    }

}
