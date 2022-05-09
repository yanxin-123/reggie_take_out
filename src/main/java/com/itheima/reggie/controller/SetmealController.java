package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"all"})
@RestController//控制反转+响应体
@RequestMapping("setmeal")//设置请求路径
@Slf4j//log日志打印
public class SetmealController {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SetmealService setmealService;

    /**
     * @Description:新增套餐
     * @data:
     * @return:
     * @Author: FEI LONG ZHANG
     * @Date: 2022-04-120 22:24:30
     */
    @PostMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info("套餐信息：{}", setmealDto);
        setmealService.saveWithDsih(setmealDto);
        return R.success("添加套餐信息成功");
    }

    /**
     * @Description:分页查询
     * @data:
     * @return:
     * @Author: FEI LONG ZHANG
     * @Date: 2022-05-121 12:58:25
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        //1.创建分页集合
        Page<Setmeal> pageinfo = new Page<>();
        Page<SetmealDto> dtoPage = new Page<>();

        //2.创建查询对象--根据name查询
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();

        //3.由于前端传过来的是不完全的值，所以使用模糊查询,根据修改时间降序排列
        //Setmeal::getName第二个参数放置数据库表列名，但是这样写，动态获取
        lqw.like(name != null, Setmeal::getName, name);
        lqw.orderByDesc(Setmeal::getUpdateTime);

        setmealService.page(pageinfo, lqw);

        //将pageinfo中的数据封装到dtopage中,
        //4.使用对象拷贝--排除封装的表单数据
        BeanUtils.copyProperties(pageinfo, dtoPage, "records");

        //5.使用stream流对数据进行处理
        List<SetmealDto> list = pageinfo.getRecords().stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            //对象拷贝
            BeanUtils.copyProperties(item, setmealDto);
            //分类id
            Long categoryId = item.getCategoryId();
            //根据分类id查询分类对象
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                //分类名称
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(list);
        //由于查询出来的数据中
        return R.success(dtoPage);
    }

    /**
     * @Description:删除套餐
     * @data:
     * @return:
     * @Author: FEI LONG ZHANG
     * @Date: 2022-05-121 22:09:31
     */
    @DeleteMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> delete(@RequestParam List<Long> ids) {
        log.info("ids:{}", ids);

        setmealService.removeWithDsih(ids);
        return R.success("套餐数据删除成功");
    }

    /**
     * @Description:根据条件查询套餐数据
     * @data:
     * @return:
     * @Author: FEI LONG ZHANG
     * @Date: 2022-05-122 17:27:18
     */

    @GetMapping("list")
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId+'_'+ #setmeal.status")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getId() != null, Setmeal::getId, setmeal.getId())
                    .eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus())
                    .orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }
}
