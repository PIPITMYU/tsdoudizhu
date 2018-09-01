/*
 * Powered By [up72-framework]
 * Web Site: http://www.up72.com
 * Since 2006 - 2017
 */

package com.up72.game.service.impl;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.up72.game.dao.UserMapper_login;
import com.up72.game.dao.impl.UserMapper_loginImpl;
import com.up72.game.dto.resp.Player;
import com.up72.game.service.IUserService_login;

/**
 * DAO实现
 * 
 * @author up72
 * @version 1.0
 * @since 1.0
 */
@Service
@Transactional
public class UserService_loginImpl implements IUserService_login {

    private UserMapper_login userMapper_login = new UserMapper_loginImpl();
    
    @Override
    public Player getUserInfoByOpenId(String openId,String cid) {
        return userMapper_login.getUserInfoByOpenId(openId,cid);
    }
}
