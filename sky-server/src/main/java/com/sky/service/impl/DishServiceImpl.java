package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;


    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        //添加菜品数据
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);

        Long dishId = dish.getId();

        //添加菜品口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();

        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(flavor ->
                    flavor.setDishId(dishId));

            dishFlavorMapper.insertBatch(flavors);
        }


    }

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */

    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 根据id删除菜品
     *
     * @param ids
     * @return
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //售卖状态
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //是否关联套餐
        List<Long> setmealIds = setmealDishMapper.getSetmealIdByDishIds(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

//        //删除菜品
//        for (Long id : ids){
//            dishMapper.deleteById(id);
//
//            //删除菜品口味
//            dishFlavorMapper.deleteByDishId(id);
//        }
        //批量删除菜品
        dishMapper.deleteByIds(ids);
        //批量删除菜品口味
        dishFlavorMapper.deleteByDishIds(ids);
    }


    public DishVO getByIdWithFlavor(Long id) {
        //根据id查询菜品
        Dish dish = dishMapper.getById(id);
        //根据id查询菜品口味
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }

    /**
     * 根据id更新菜品和口味
     *
     * @param dishDTO
     */
    public void updateWithFlavor(DishDTO dishDTO) {
        //更新菜品
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);
        //删除菜品口味
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        //添加菜品口味
        List<DishFlavor> flavors = dishDTO.getFlavors();

        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(flavor ->
                    flavor.setDishId(dishDTO.getId()));

            dishFlavorMapper.insertBatch(flavors);

        }


    }
}