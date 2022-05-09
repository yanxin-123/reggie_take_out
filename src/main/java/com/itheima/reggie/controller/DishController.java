package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.experimental.PackagePrivate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
@SuppressWarnings({"all"})
@RestController//控制反转+响应体
@RequestMapping("/dish")//设置响应路径
@Slf4j//日志输出
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * @Description:新增菜品
     * @data:
     * @return:
     * @Author: FEI LONG ZHANG
     * @Date: 2022-04-118 16:03:44
     */
    @PostMapping                   //DishDto数据传输对象
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    /**
     * 菜品信息分页查询
     *
     * @param page
     * @param pagesize
     * @param name
     * @return
     */
    @GetMapping("page")
    public R<Page> page(int page, int pageSize, String name) {

        //钩爪分页构造器对象
        Page<Dish> pageinfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤器条件---添加排序条件
        queryWrapper.like(name != null, Dish::getName, name)
                .orderByDesc(Dish::getUpdateTime);

        //执行分页查询
        dishService.page(pageinfo, queryWrapper);

        //对象拷贝---排除records集合，后面操作
        BeanUtils.copyProperties(pageinfo, dishDtoPage, "records");

        List<Dish> records = pageinfo.getRecords();
        List<DishDto> list = records.stream().map((item) -> {
            //创建dishdto对象
            DishDto dishDto = new DishDto();

            //对象拷贝--将数据拷贝到dishdto中
            BeanUtils.copyProperties(item, dishDto);

            //获取分类id，根据分类id获取菜品分类信息
            Long categoryId = item.getCategoryId();

            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            //保留返回结果
            return dishDto;
        }).collect(Collectors.toList());
        //将数据交给Records
        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }

    /**
     * @Description: 根据id查询菜品信息和对应的口味信息
     * @data:
     * @return:
     * @Author: FEI LONG ZHANG
     * @Date: 2022-04-119 18:00:42
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {
        log.info("根据id查询菜品信息和对应的口味信息");

        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }

    /**
     * @Description:修改菜品信息
     * @data:
     * @return:
     * @Author: FEI LONG ZHANG
     * @Date: 2022-04-119 19:24:16
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        log.info("修改菜品信息：{}", dishDto);
        dishService.updateWithFlavor(dishDto);

        //修改菜品信息的时候，微信端页面缓存的数据也要清除
        //1.清除所有菜品缓存数据
        //2.清理某一个分类下面的菜品缓存数据
        //动态设置key
        String key = "dish_" +dishDto.getCategoryId()+ "_1";
        redisTemplate.delete(key);
        return R.success("修改菜品成功");
    }

    /**
     * @Description: 根据id新增套餐功能
     * @data:
     * @return:
     * @Author: FEI LONG ZHANG
     * @Date: 2022-04-120 22:01:10
     */
    @GetMapping("list")
    public R<List<DishDto>> list(Dish dish) {
        List<DishDto> dishDtoList = null;

        //代码优化--添加缓存
        //先从redis中缓存数据--先动态构造key
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        //如果存在直接返回，无需查询数据库
        if (dishDtoList != null) {
            return R.success(dishDtoList);
        }

        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        //添加条件，查询状态为1（起售状态）的菜品
        queryWrapper.eq(Dish::getStatus, 1);

        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item, dishDto);

            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            //当前菜品的id
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId, dishId);
            //SQL:select * from dish_flavor where dish_id = ?
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());

        //如果不存在就要查询数据库--并且添加缓存
        redisTemplate.opsForValue().set(key,dishDtoList);

        return R.success(dishDtoList);
    }
}
