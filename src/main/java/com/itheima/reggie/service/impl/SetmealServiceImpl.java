package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"all"})
@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * @Description:新增套餐
     * @data:
     * @return:
     * @Author: FEI LONG ZHANG
     * @Date: 2022-04-120 22:24:30
     */

    @Override
    @Transactional
    public void saveWithDsih(SetmealDto setmealDto) {
        log.info("{}", setmealDto);

        //保存套餐表信息
        this.save(setmealDto);

        //保存套餐和菜品表关联信息
        //1.获取数据传输对象中的id
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        List<SetmealDish> list = setmealDishes.stream().map((item) -> {
            Long id = setmealDto.getId();
            item.setSetmealId(id);
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(list);
    }

    /**
     * @Description: 删除套餐
     * @data:
     * @return:
     * @Author: FEI LONG ZHANG
     * @Date: 2022-05-121 21:58:21
     */
    @Override
    @Transactional
    public void removeWithDsih(List<Long> ids) {
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.in(Setmeal::getId, ids).eq(Setmeal::getStatus, 1);

        int count = this.count(lqw);

        //判断count如果大于零表示又数据，包含在售上商品，不能删除--抛异常
        if (count > 0){
            throw new CustomException("套餐正在售卖中，不能删除");
        }

        this.removeByIds(ids);

        //删除套餐和菜品关系表
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.in(SetmealDish::getSetmealId,ids);

        setmealDishService.remove(queryWrapper);
    }
}
