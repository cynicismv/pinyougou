package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;
import com.sun.org.apache.regexp.internal.RE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Service
public class CartServiceImp implements CartService {
    @Autowired
    private TbItemMapper itemMapper;
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1.根据商品 SKU ID 查询 SKU 商品信息
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item==null){
            throw new RuntimeException("商品不存在");
        }
        if (!item.getStatus().equals("1")){
            throw new RuntimeException("商品状态不合法");
        }
        //2.获取商家 ID
        String sellerId = item.getSellerId();
        //3.根据商家 ID 判断购物车列表中是否存在该商家的购物车
        Cart cart = searchCartBySellerId(cartList, sellerId);
       if (cart==null) {//4.如果购物车列表中不存在该商家的购物车
           //4.1 新建购物车对象
           cart=new Cart();
           cart.setSellerId(sellerId);//商家id
           cart.setSellerName(item.getSeller());//商家名称
           List<TbOrderItem> orderItemList=new ArrayList<>();//创建购物车明细列表
            TbOrderItem orderItem=createOrderItem(item,num);
           orderItemList.add(orderItem);
           cart.setOrderItemList(orderItemList);
           //4.2 将新建的购物车对象添加到购物车列表
           cartList.add(cart);
       }else {
           //5.如果购物车列表中存在该商家的购物车
           // 查询购物车明细列表中是否存在该商品
           TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);
           if(orderItem==null) {
               //5.1. 如果没有，新增购物车明细
               orderItem=createOrderItem(item,num);
               cart.getOrderItemList().add(orderItem);
           }else {
               //5.2. 如果有，在原购物车明细上添加数量，更改金额
                orderItem.setNum(orderItem.getNum()+num);//更改数量
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));//金额
               //如果数量操作后小于等于 0，则移除
               if (orderItem.getNum()<=0){
                   cart.getOrderItemList().remove(orderItem);//移除购物车明细
               }
               //如果移除后 cart 的明细数量为 0，则将 cart 移除
                if(cart.getOrderItemList().size()==0){
                   cartList.remove(cart);
                }
           }
       }
       return cartList;
    }
    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("从购物车中提取购物车数据"+username);
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if(cartList==null){
            cartList=new ArrayList<>();
        }
        return cartList;
    }

    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("从购物车中存储购物车数据"+username);
        redisTemplate.boundHashOps("cartList").put(username,cartList);
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        for (Cart cart : cartList2) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                cartList1=addGoodsToCartList(cartList1,orderItem.getItemId(),orderItem.getNum());
            }
        }
        return cartList1;
    }

    //根据商家 ID 查询购物车对象
    private Cart searchCartBySellerId(List<Cart> cartList,String sellerId){
        for (Cart cart : cartList) {
            if (cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return null;
    }
    //根据商品明细 ID 查询
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList ,Long itemId ) {
        for (TbOrderItem orderItem : orderItemList) {
            if(orderItem.getItemId().longValue()==itemId.longValue()){
                return orderItem;
            }
        }
        return null;
    }
        //创建订单明细
    private TbOrderItem createOrderItem(TbItem item,Integer num){
        //创建新的购物车明细对象
        TbOrderItem orderItem=new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
        return orderItem;
    }
}
