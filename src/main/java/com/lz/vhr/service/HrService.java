package com.lz.vhr.service;

import com.lz.vhr.mapper.HrMapper;
import com.lz.vhr.model.Hr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class HrService implements UserDetailsService {

    @Autowired
    private HrMapper hrMapper;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Hr hr = hrMapper.loadUserByUsername(username);

        if (hr == null) {
            throw new UsernameNotFoundException("用户名密码错误");
        }

        hr.setRoles(hrMapper.getByIdRoles(hr.getId()));

        return hr;
    }
}
