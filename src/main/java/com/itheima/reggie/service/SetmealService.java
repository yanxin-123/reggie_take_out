package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

@SuppressWarnings({"all"})
public interface SetmealService extends IService<Setmeal> {
    /**
     * @Description: 新增套餐，同时需要保存套餐和菜品的关联关系
     * @data:
     * @return:
     * @Author: FEI LONG ZHANG
     * @Date: 2022-04-120 22:28:01
     */
    public void saveWithDsih(SetmealDto setmealDto);

    /**
     * @Description: 删除套餐
     * @data:
     * @return:
     * @Author: FEI LONG ZHANG
     * @Date: 2022-05-121 21:58:21
     */
    public void removeWithDsih(List<Long> ids);
}
