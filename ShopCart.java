package com.qhit.common;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qhit.goodsInfo.dao.IGoodsInfoDao;
import com.qhit.goodsInfo.pojo.GoodsInfo;
import com.qhit.goodsInfo.service.IGoodsInfoService;
import com.qhit.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * redis实现购物车的增删改查
 * Created by 17194 on 2019/4/10.
 */
@Component
public class ShopCart {

    @Autowired
    RedisUtils redisUtils;
    @Resource
    IGoodsInfoDao goodsInfoDao;

    /**
     * 增加或修改购物车商品信息
     * @param goodsInfo1
     * @param userId
     * @return
     */
    public void addOrUpdateShopCart(GoodsInfo goodsInfo1, Integer userId) {
        Gson gson = new Gson();
        List<GoodsInfo> list = goodsInfoDao.findById(goodsInfo1.getGid());
        GoodsInfo goodsInfo = null;
        if(null!=list && list.size()>0){
            goodsInfo = list.get(0);
        }else {
            return;
        }
        goodsInfo.setCount(goodsInfo1.getCount());
        List<GoodsInfo> list1 = new LinkedList<>();//返回的集合
        //存放商品的map
        Map<String,String> shopCartMap = new HashMap<>();
        Object r = redisUtils.hget("shopCart", ""+userId);
        if (r==null){
            //此用户还没有购物车，要先创建一个存放商品的购物车，购物车里放这次传过来的商品
            //商品转json
            String goodsInfoJson = gson.toJson(goodsInfo);
            //商品map增加商品
            shopCartMap.put(""+goodsInfo.getGid(),goodsInfoJson);
            //商品map转为json
            String goodsInfoMapJson = gson.toJson(shopCartMap);
            //存入redis
            redisUtils.hset("shopCart", ""+userId,goodsInfoMapJson);
        }else {
            //用户已经有了购物车
            shopCartMap = gson.fromJson((String) r,new TypeToken<Map<String,String>>() {
            }.getType());
            //
            String isHaveGoods = shopCartMap.get(""+goodsInfo.getGid());
            //购物车还没有此商品 直接添加
            String shopGoodsInfoJson = gson.toJson(goodsInfo);
            shopCartMap.put(""+goodsInfo.getGid(),shopGoodsInfoJson);
            String shopMapJson = gson.toJson(shopCartMap);
            redisUtils.hset("shopCart",""+userId,shopMapJson);
        }
    }

    /**
     *删除购物车
     * @param gid
     * @param uid
     * @return
     */
    public void removeShopCart(String gid, Integer uid){
        Gson gson = new Gson();
        List<GoodsInfo> list1 = new LinkedList<>();
        Object r = redisUtils.hget("shopCart", ""+uid);
        Map<String,String> shopMap = new HashMap<>();
        if (r!=null){
            //用户已经有了购物车
            shopMap = gson.fromJson((String) r,new TypeToken<Map<String,String>>() {
            }.getType());
            String isHaveGoods = shopMap.get(""+gid);
            //购物车中有此商品
            if (isHaveGoods!=null){
                //获取购物车中商品信息
                String shopGoodsInfoJson = shopMap.remove("" + gid);
                String shopMapJson = gson.toJson(shopMap);
                redisUtils.hset("shopCart",""+uid,shopMapJson);
            }
        }

    }

    /**
     * 获取购物车所有商品信息
     * @param uid
     * @return
     */
    public List<GoodsInfo> getShopCart(Integer uid){
        Gson gson = new Gson();
        Object r = redisUtils.hget("shopCart", ""+uid);
        if (r!=null){
            //用户已经有了购物车
            //存放商品的map list
            List<GoodsInfo> list1 = new LinkedList<>();//返回的集合
            Map<String,String> shopCartMap = gson.fromJson((String) r,new TypeToken<Map<String,String>>() {
            }.getType());
            //遍历返回
            for(String s:shopCartMap.keySet()){
                String s1 = shopCartMap.get(s);
                GoodsInfo goodsInfo2 = gson.fromJson(s1,new TypeToken<GoodsInfo>() {
                }.getType());
                list1.add(goodsInfo2);
            }
            return list1;
        }
        return null;
    }

    /**
     * 清空购物车
     * @param userId
     */
    public void clearShopCart(Integer userId) {
        Object r = redisUtils.hget("shopCart", ""+userId);

        if (r!=null){
            //清空此用户购物车
            redisUtils.hdel("shopCart",""+userId);
        }
    }



}
