package com.powernode.service;

import com.powernode.domain.SysMenu;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Set;

public interface SysMenuService extends IService<SysMenu>{


    Set<SysMenu> queryUserMenuListByUserId(Long loginUserId);
}
