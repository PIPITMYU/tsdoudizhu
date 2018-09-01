/*
 * Powered By [up72-framework]
 * Web Site: http://www.up72.com
 * Since 2006 - 2017
 */

package com.up72.game.service;

import java.util.Map;

import com.up72.game.dto.resp.Player;


/**
 * 接口
 * 
 * @author up72
 * @version 1.0
 * @since 1.0
 */
public interface IUserService_login {

   Player getUserInfoByOpenId(String openId,String cid);
}
