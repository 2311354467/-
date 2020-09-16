package com.lz.vhr.service;

import com.lz.vhr.mapper.MenuMapper;
import com.lz.vhr.model.Hr;
import com.lz.vhr.model.Menu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuService {

    @Autowired
    MenuMapper menuMapper;

    public List<Menu> getMenuByHrId() {

        return menuMapper.getMenuByHrId(((Hr) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId());
    }

    public List<Menu> getMenusAndRoles() {
        return menuMapper.getMenusAndRoles();
    }
}
