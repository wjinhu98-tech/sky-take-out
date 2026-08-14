package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    /**
     * 员工分页查询
     * @param employeePageQueryDTO
     * @return
     */
    Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 根据 id 修改员工状态
     * @param employee
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(Employee employee);

    /**
     * 插入员工数据（带自动填充）
     * @param employee
     */
    @Insert("insert into employee(username, name, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user)" +
            " VALUES" +
            " (#{username}, #{name}, #{password}, #{phone}, #{sex}, #{idNumber}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    @AutoFill(value = OperationType.INSERT)
    int insert(Employee employee);

    @Select("select * from employee where id = #{id}")
    Employee getById(Long id);
}
