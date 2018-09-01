/*
 * Powered By [up72-framework]
 * Web Site: http://www.up72.com
 * Since 2006 - 2017
 */

package com.up72.game.service.impl;

import java.util.*;

import com.up72.game.dao.impl.UserMapperImpl;
import com.up72.game.dao.impl.UserMapper_loginImpl;
import com.up72.game.dto.resp.Feedback;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.PlayerRecord;
import com.up72.game.model.*;
import com.up72.game.dao.*;
import com.up72.game.service.*;
import com.up72.framework.util.page.PageBounds;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.up72.game.service.IUserService;
import com.up72.framework.util.page.PageList;
import com.up72.framework.util.page.Page;

/**
 * DAO实现
 * 
 * @author up72
 * @version 1.0
 * @since 1.0
 */
@Service
@Transactional
public class UserServiceImpl implements IUserService {


    private UserMapper userMapper = new UserMapperImpl();

    public void save(Player user) {
        userMapper.insert(user);
    }

    public void update(User user) {
        userMapper.update(user);
    }

    @Transactional(readOnly = true)
    public Player getById(java.lang.Long id) {
        return userMapper.findById(id);
    }

//    @Override
//    public Player getByOpenId(String openId,String cid) {
//        return userMapper.findByOpenId(openId,cid);
//    }
    @Override
    public Player getByOpenId(String openId,String cid) {
        return userMapper.findByOpenId(openId,cid);
    }

    
    
    
    @Override
	public boolean updateUserId(Long id, Long userId) {
    	return userMapper.updateUserId(id,userId);
	}

	@Override
    public List<PlayerRecord> findPlayerRecordByUserId(Long userId,Integer start,Integer limit) {
        return userMapper.findPlayerRecordByUserId(userId,start,limit);
    }

    @Override
    public void userFeedback(Feedback feedback) {
        userMapper.userFeedback(feedback);
    }

    @Override
    public void updateMoney(Integer money, String userId,Integer cid) {
        userMapper.updateMoney(money,userId,cid);
    }

    @Override
    public Integer isExistUserId(String userId) {
        return userMapper.isExistUserId(userId);
    }

    @Override
    public void updateUserAgree(Long userId) {
        userMapper.updateUserAgree(userId);
    }

    @Override
    public String getNotice(String cid) {
        return userMapper.getNotice(cid);
    }

    @Override
    public List<SystemMessage> getSystemMessage(Long userId, Integer start, Integer limit) {
        return userMapper.getSystemMessage(userId,start,limit);
    }

    @Override
    public void insertPlayRecord(Map<String,String> playRecord) {
        userMapper.insertPlayRecord(playRecord);
    }
    
    @Override
    public void insertPlayerMoneyRecord(PlayerMoneyRecord mr) {
    	userMapper.insertPlayerMoneyRecord(mr);
    	
    }
    
    @Override
    public String getConectUs() {
    	return userMapper.getConectUs();
    }
    @Override
    public Integer getUserMoneyByUserId(Long userId,Integer cid) {
    	return userMapper.getUserMoneyByUserId(userId,cid);
    }

	@Override
	public void updateIpAndLastTime(String openId,String ip) {
		userMapper.updateIpAndLastTime(openId,ip);
	}

	@Override
	public String findIpByUserId(Long userId) {
		return userMapper.findIpByUserId(userId);
	}

	@Override
	public Integer findTotalGameNum(Long userId) {
		return userMapper.findTotalGameNum(userId);
	}
	
	@Override
	public Integer getMoneyInit(String cId) {
		return userMapper.getMoneyInit(cId);
	}
    
    
}
