package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"all"})
@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 新增菜品，同时保存对应的口味数据
     *
     * @param dishDto
     */


    @Override
    @Transactional//开启事务，要在spring boot启动类上加上注解启动事务
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish
        this.save(dishDto);
        log.info("{}", dishDto);

        //菜品id
        Long dishDtoId = dishDto.getId();

        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();

        //使用stream流进行批量操作
        flavors.stream().map((item) -> {
            //将流水线上每一个元素进行赋值id操作
            item.setDishId(dishDtoId);
            return item;
        }).collect(Collectors.toList());

        //保存菜品口味数据到菜品口味表中dish_flavor
        dishFlavorService.saveBatch(flavors);
    }


    /**
     * 根据id查询菜品信息和对应的口味信息
     *
     * @param id
     * @return
     */
    @Override
    @Transactional
    public DishDto getByIdWithFlavor(Long id) {
        //根据id查出菜品信息
        Dish dish = this.getById(id);

        //根据id查出菜品口味信息--使用查询条件封装成list集合
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();

        //设置条件
        queryWrapper.eq(DishFlavor::getDishId, dishFlavorService.getById(id));

        //根据条件查询菜品口味信息
        List<DishFlavor> dishFlavor = dishFlavorService.list(queryWrapper);

        //将菜品口味信息和菜品信息封装到一起
        DishDto dishDto = new DishDto();

        //封装数据到Dto中--面向界面UI的数据传输层
        //封装菜品数据
        BeanUtils.copyProperties(dish, dishDto);
        BeanUtils.copyProperties(dishFlavor, dishDto);
        return dishDto;
    }

    @Override
    @Transactional//开启事务
    public void updateWithFlavor(DishDto dishDto) {
        //修改菜品信息、修改菜品对应的口味信息
        //1。更新菜品信息
        this.updateById(dishDto);

        //修改菜品口味信息可以直接删除该信息，再添加该信息
        //查询条件
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(DishFlavor::getId, dishDto.getId());

        //删除该数据
        dishFlavorService.remove(queryWrapper);

        //添加数据到数据库--由于DishFlavor集合上没有id，所有封装id\
        //1.获取id
        Long dishDtoId = dishDto.getId();

        //2.封装id到DishFlavor上--使用stream流
        List<DishFlavor> dishFlavorList = dishDto.getFlavors().stream().map((item) -> {
            //将流水线上每一个元素进行赋值id操作
            item.setDishId(dishDtoId);
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(dishFlavorList);
    }


}
