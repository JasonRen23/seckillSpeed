package com.jasonren.seckill.service;

import com.jasonren.seckill.dao.SeckillUserDao;
import com.jasonren.seckill.domain.SeckillUser;
import com.jasonren.seckill.exception.GlobalException;
import com.jasonren.seckill.redis.RedisService;
import com.jasonren.seckill.redis.SeckillUserKey;
import com.jasonren.seckill.result.CodeMsg;
import com.jasonren.seckill.util.MD5Util;
import com.jasonren.seckill.util.UUIDUtil;
import com.jasonren.seckill.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class SeckillUserService {

    public static final String COOKIE_NAME_TOKEN = "token";

    @Autowired
    RedisService redisService;

    @Autowired
    SeckillUserDao seckillUserDao;


    public SeckillUser getById(long id) {
        return seckillUserDao.getById(id);
    }

    public boolean login(HttpServletResponse response, LoginVo loginVo) {
        if (loginVo == null) {
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }

        String mobile = loginVo.getMobile();
        String formPass = loginVo.getPassword();

        //判断手机号是否存在
        SeckillUser user = getById(Long.parseLong(mobile));
        if (user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }

        //验证密码
        String dbPass = user.getPassword();
        String saltDB = user.getSalt();
        String calPass = MD5Util.formPassToDBPass(formPass, saltDB);
        if (!calPass.equals(dbPass)) {
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }

        String token = UUIDUtil.uuid();
        addCookie(response, token, user);
        return true;

    }

    public SeckillUser getByToken(HttpServletResponse response, String token) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        SeckillUser seckillUser = redisService.get(SeckillUserKey.token, token, SeckillUser.class);
        if (seckillUser != null) {
            addCookie(response, token, seckillUser);
        }
        return seckillUser;
    }

    private void addCookie(HttpServletResponse response, String token, SeckillUser user) {
        //生成Cookie
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN, token);
        redisService.set(SeckillUserKey.token, token, user);
        cookie.setMaxAge(SeckillUserKey.token.expireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);
    }

}
