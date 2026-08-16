package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("admin/shop")
@Api(tags = "店铺相关接口")
@Slf4j
public class shopController {

    @Autowired
    private RedisTemplate redisTemplate;

    private final static String KEY="SHOP_STATUS";

    @PutMapping("/{status}")
    @ApiOperation("修改店铺状态")
    public Result setShoptatus(@PathVariable Integer status){
        log.info("店铺状态修改：{}", status==1?"营业中":"休息中");

        redisTemplate.opsForValue().set(KEY, status);
        return Result.success();
    }

    @ApiOperation("获取店铺状态"  )
    @GetMapping("/status")
    public Result<Integer> getShopStatus(){

        Integer shopStatus = (Integer) redisTemplate.opsForValue().get(KEY);
        // Redis 中无店铺状态时 shopStatus 为 null，与 1 比较会拆箱报 NPE，默认营业中
        shopStatus = shopStatus == null ? 1 : shopStatus;
        log.info("获取店铺状态：{}", shopStatus==1?"营业中":"休息中");

        return Result.success(shopStatus);
    }
}
