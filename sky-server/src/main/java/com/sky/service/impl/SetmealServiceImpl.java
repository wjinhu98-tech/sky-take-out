package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


/**
 * 套餐业务实现类
 */
@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private DishMapper dishMapper;


    /**
     * 新增套餐
     * 同时保存套餐和菜品之间的关系
     *
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {

        // ============================
        // 1. 保存套餐
        // ============================

        Setmeal setmeal = new Setmeal();

        BeanUtils.copyProperties(setmealDTO, setmeal);

        // 插入 setmeal 表
        setmealMapper.insert(setmeal);


        // ============================
        // 2. 获取新增套餐生成的主键id
        // ============================

        Long setmealId = setmeal.getId();


        // ============================
        // 3. 获取套餐对应的菜品
        // ============================

        List<SetmealDish> setmealDishes =
                setmealDTO.getSetmealDishes();


        // ============================
        // 4. 给每一个套餐菜品设置 setmealId
        // ============================

        setmealDishes.forEach(setmealDish -> {

            setmealDish.setSetmealId(setmealId);

        });


        // ============================
        // 5. 保存套餐和菜品的关联关系
        // ============================

        if (setmealDishes != null &&
                setmealDishes.size() > 0) {

            setmealDishMapper.insertBatch(setmealDishes);
        }
    }


    /**
     * 套餐分页查询
     *
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(
            SetmealPageQueryDTO setmealPageQueryDTO) {

        // 获取当前页
        int pageNum =
                setmealPageQueryDTO.getPage();

        // 获取每页显示条数
        int pageSize =
                setmealPageQueryDTO.getPageSize();


        // 开启分页
        PageHelper.startPage(pageNum, pageSize);


        // 执行分页查询
        Page<SetmealVO> page =
                setmealMapper.pageQuery(setmealPageQueryDTO);


        // 封装分页结果
        return new PageResult(
                page.getTotal(),
                page.getResult()
        );
    }


    /**
     * 批量删除套餐
     *
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {

        // ============================
        // 1. 判断套餐是否正在起售
        // ============================

        ids.forEach(id -> {

            Setmeal setmeal =
                    setmealMapper.getById(id);

            // 起售中的套餐不能删除
            if (StatusConstant.ENABLE ==
                    setmeal.getStatus()) {

                throw new DeletionNotAllowedException(
                        MessageConstant.SETMEAL_ON_SALE
                );
            }
        });


        // ============================
        // 2. 删除套餐
        // ============================

        ids.forEach(setmealId -> {

            // 删除 setmeal 套餐表
            setmealMapper.deleteById(setmealId);

            // 删除 setmeal_dish 套餐菜品关系表
            setmealDishMapper
                    .deleteBySetmealId(setmealId);
        });
    }


    /**
     * 根据id查询套餐和套餐关联的菜品
     *
     * @param id
     * @return
     */
    @Override
    public SetmealVO getByIdWithDish(Long id) {

        // ============================
        // 1. 查询套餐基本信息
        // ============================

        Setmeal setmeal =
                setmealMapper.getById(id);


        // ============================
        // 2. 查询套餐对应的菜品
        // ============================

        List<SetmealDish> setmealDishes =
                setmealDishMapper.getBySetmealId(id);


        // ============================
        // 3. 封装成 SetmealVO
        // ============================

        SetmealVO setmealVO =
                new SetmealVO();

        BeanUtils.copyProperties(
                setmeal,
                setmealVO
        );


        // 设置套餐里面的菜品
        setmealVO.setSetmealDishes(setmealDishes);


        return setmealVO;
    }


    /**
     * 修改套餐
     *
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {

        // ============================
        // 1. 修改套餐基本信息
        // ============================

        Setmeal setmeal =
                new Setmeal();

        BeanUtils.copyProperties(
                setmealDTO,
                setmeal
        );

        setmealMapper.update(setmeal);


        // ============================
        // 2. 获取套餐id
        // ============================

        Long setmealId =
                setmealDTO.getId();


        // ============================
        // 3. 删除原来的套餐菜品关系
        // ============================

        setmealDishMapper
                .deleteBySetmealId(setmealId);


        // ============================
        // 4. 获取修改后的菜品
        // ============================

        List<SetmealDish> setmealDishes =
                setmealDTO.getSetmealDishes();


        // ============================
        // 5. 给菜品设置套餐id
        // ============================

        setmealDishes.forEach(setmealDish -> {

            setmealDish
                    .setSetmealId(setmealId);

        });


        // ============================
        // 6. 重新保存套餐和菜品关系
        // ============================

        if (setmealDishes != null &&
                setmealDishes.size() > 0) {

            setmealDishMapper
                    .insertBatch(setmealDishes);
        }
    }


    /**
     * 套餐起售、停售
     *
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(
            Integer status,
            Long id) {

        // ============================
        // 如果是起售套餐
        // ============================

        if (status == StatusConstant.ENABLE) {

            /*
             * 查询套餐里面所有的菜品
             *
             * select a.*
             * from dish a
             * left join setmeal_dish b
             * on a.id = b.dish_id
             * where b.setmeal_id = ?
             */

            List<Dish> dishList =
                    dishMapper.getBySetmealId(id);


            // 判断套餐里面是否包含停售菜品
            if (dishList != null &&
                    dishList.size() > 0) {

                dishList.forEach(dish -> {

                    if (StatusConstant.DISABLE ==
                            dish.getStatus()) {

                        throw new SetmealEnableFailedException(
                                MessageConstant.SETMEAL_ENABLE_FAILED
                        );
                    }

                });
            }
        }


        // ============================
        // 修改套餐状态
        // ============================

        Setmeal setmeal =
                Setmeal.builder()
                        .id(id)
                        .status(status)
                        .build();


        setmealMapper.update(setmeal);
    }


    /**
     * 动态条件查询套餐
     *
     * @param setmeal
     * @return
     */
    @Override
    public List<Setmeal> list(Setmeal setmeal) {

        return setmealMapper.list(setmeal);
    }


    /**
     * 根据套餐id查询包含的菜品列表
     *
     * @param id
     * @return
     */
    @Override
    public List<DishItemVO> getDishItemById(Long id) {

        List<DishItemVO> dishItemVOList =
                new ArrayList<>();


        // ============================
        // 1. 查询套餐和菜品的关联关系
        // ============================

        List<SetmealDish> setmealDishes =
                setmealDishMapper.getBySetmealId(id);


        if (setmealDishes != null &&
                setmealDishes.size() > 0) {


            // ============================
            // 2. 根据菜品id查询菜品信息
            // ============================

            setmealDishes.forEach(setmealDish -> {

                Dish dish =
                        dishMapper.getById(setmealDish.getDishId());


                DishItemVO dishItemVO =
                        DishItemVO.builder()
                                .name(dish.getName())
                                .copies(setmealDish.getCopies())
                                .image(dish.getImage())
                                .description(dish.getDescription())
                                .build();

                dishItemVOList.add(dishItemVO);
            });
        }


        return dishItemVOList;
    }
}