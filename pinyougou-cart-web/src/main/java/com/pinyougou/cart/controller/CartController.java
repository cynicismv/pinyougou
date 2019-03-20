package com.pinyougou.cart.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;

import com.pinyougou.entity.Result;
import util.CookieUtil;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Reference(timeout = 6000)
    private CartService cartService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;
    /**
     * 购物车列表
     * @param
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(){
        //当前登录人账号
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录人:"+username);
        String cartListString = util.CookieUtil.getCookieValue(request, "cartList","UTF-8");
        if(cartListString==null || cartListString.equals("")){
            cartListString="[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
        if(username.equals("anonymousUser")){//如果未登录
            //从cookie中提取购物车
            System.out.println("从cookie中提取购物车");

            return cartList_cookie;
        }else {//如果已登录
            //获取redis购物车
            List<Cart> cartList_redis = cartService.findCartListFromRedis(username);
            //判断当本地购物车中存在数据
            if(cartList_cookie.size()>0){
                //得到合并后的购物车
                List<Cart> cartList = cartService.mergeCartList(cartList_cookie, cartList_redis);
                //将合并后的购物车存入redis
                cartService.saveCartListToRedis(username,cartList);
                //本地购物车清除
                CookieUtil.deleteCookie(request,response,"cartList");
                System.out.println("执行了合并购物车的逻辑");
                return cartList;
            }
            return cartList_redis;
        }

    }
    /**
     * 添加商品到购物车
     * @param
     * @param itemId
     * @param num
     * @return
     */
    @RequestMapping("/addGoodsToCartList")
    public Result addGoodsToCartList(Long itemId,Integer num){
        //当前登录人账号
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录人:"+username);
        try {
            List<Cart> cartList =findCartList();//获取购物车列表
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            if (username.equals("anonymousUser")){//如果是未登录保存到cookie
                util.CookieUtil.setCookie(request, response, "cartList",
                        JSON.toJSONString(cartList),3600*24,"UTF-8");
                System.out.println("向cookie中存入数据");
            }else {//如果已登录保存到redis
                cartService.saveCartListToRedis(username,cartList);
                System.out.println("向redis中存入数据");
            }
            return new Result(true, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败");

        }
    }
}

