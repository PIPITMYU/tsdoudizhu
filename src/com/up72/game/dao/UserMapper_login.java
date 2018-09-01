package com.up72.game.dao;


import org.springframework.stereotype.Repository;

import com.up72.game.dto.resp.Player;

/**
 * DAO
 * 
 * @author up72
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface UserMapper_login {


    Player getUserInfoByOpenId(String openId,String cid);

}
