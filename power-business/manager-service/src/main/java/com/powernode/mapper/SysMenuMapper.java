package com.powernode.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.powernode.domain.SysMenu;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;
public interface SysMenuMapper extends BaseMapper<SysMenu> {
    Set<SysMenu> selectUserMenuListByUserId(Long loginUserId);
}