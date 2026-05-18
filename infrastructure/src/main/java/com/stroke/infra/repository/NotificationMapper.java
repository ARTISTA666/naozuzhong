package com.stroke.infra.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stroke.infra.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知记录 Mapper
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}
