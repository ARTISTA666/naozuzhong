package com.stroke.infra.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stroke.domain.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT id, username, password_hash AS password, real_name AS display_name, "
          + "role_name, role_code AS role, department, title, phone, email, status, "
          + "last_login_time AS last_login_time, created_by, created_time, updated_by, updated_time "
          + "FROM sys_user WHERE username = #{username}")
    SysUser findByUsername(String username);
}
