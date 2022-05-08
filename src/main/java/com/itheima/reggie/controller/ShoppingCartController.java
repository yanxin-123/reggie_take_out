package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;


/**
 * 购物车
 */
@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
//        log.info("购物车数据：{}", shoppingCart);
//        //需求:向购物车中添加商品
//        //1.获取用户id
//        Long currentId = BaseContext.getCurrentId();
//
//        //2.存入用户id
//        shoppingCart.setUserId(currentId);
//
//        //3.判断传过来的是菜品信息还是套餐信息
//        //先获取菜品id查询数据看有没有
//        Long dishId = shoppingCart.getDishId();
//
//        //4.创建查询条件
//        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
//
//        queryWrapper.eq(ShoppingCart::getDishId, currentId);
//
//        //判断是菜品还是套餐，
//        if (dishId != null) {
//            //是菜品
//            queryWrapper.eq(ShoppingCart::getDishId, dishId);
//        } else {
//            //是套餐
//            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
//        }
//
//        //查询购物车中包不包含这个菜品或者套餐，有则数量加一，无则保存
//        ShoppingCart shoppingCartOne = shoppingCartService.getOne(queryWrapper);
//
//        if (shoppingCartOne != null) {
//            //表示该菜品存在购物车中，存在数量加1
//            Integer number = shoppingCartOne.getNumber();
//            shoppingCart.setNumber(number + 1);
//            shoppingCartService.updateById(shoppingCartOne);
//        }else{
//            //表示该菜品不存在购物车中，保存一下
//            shoppingCart.setNumber(1);
//            shoppingCart.setCreateTime(LocalDateTime.now());
//            shoppingCartService.save(shoppingCart);
//            shoppingCartOne = shoppingCart;
//        }
//
//        return R.success(shoppingCartOne);
        log.info("购物车数据:{}",shoppingCart);

        //设置用户id，指定当前是哪个用户的购物车数据
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        Long dishId = shoppingCart.getDishId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currentId);

        if(dishId != null){
            //添加到购物车的是菜品
            queryWrapper.eq(ShoppingCart::getDishId,dishId);

        }else{
            //添加到购物车的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        //查询当前菜品或者套餐是否在购物车中
        //SQL:select * from shopping_cart where user_id = ? and dish_id/setmeal_id = ?
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);

        if(cartServiceOne != null){
            //如果已经存在，就在原来数量基础上加一
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number + 1);
            shoppingCartService.updateById(cartServiceOne);
        }else{
            //如果不存在，则添加到购物车，数量默认就是一
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cartServiceOne = shoppingCart;
        }

        return R.success(cartServiceOne);
    }

    /**
     * @Description:查看购物车
     * @data:[]
     * @return: com.itheima.reggie.common.R<java.util.List<com.itheima.reggie.entity.ShoppingCart>>
     * @Author: FEI LONG ZHANG
     * @Date: 2022-05-123 15:23:56
     */

    @GetMapping("list")
    public R<List<ShoppingCart>> list(){
        log.info("查看购物车");

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);

        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);

        return R.success(list);
    }

    /**
     * @Description:清空购物车
     * @data:
     * @return:
     * @Author: FEI LONG ZHANG
     * @Date: 2022-05-123 15:23:48
     */
    @DeleteMapping("clean")
    public R<String> clean(){
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);
        return R.success(null);
    }
}