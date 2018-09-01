/*
 * Powered By [up72-framework]
 * Web Site: http://www.up72.com
 * Since 2006 - 2017
 */

package com.up72.game.dao;


import com.up72.game.dto.resp.Feedback;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.PlayerRecord;
import com.up72.game.model.PlayerMoneyRecord;
import com.up72.game.model.SystemMessage;
import com.up72.game.model.User;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * DAO
 * 
 * @author up72
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface UserMapper {

    void insert(Player entity);

    void update(User entity);

    Player findById(java.lang.Long id);

//   Player findByOpenId(String openId,String cid);
    
    Player findByOpenId(String openId,String cid);
    
    boolean updateUserId(Long id, Long userId);

    List<PlayerRecord> findPlayerRecordByUserId(java.lang.Long userId,Integer start,Integer limit);

    void userFeedback(Feedback feedback);

    void updateMoney(Integer money,String userId ,Integer cid);

    Integer isExistUserId(String userId);

    void updateUserAgree(Long userId);

    String getNotice(String cid);

    List<SystemMessage> getSystemMessage(java.lang.Long userId,Integer start,Integer limit);

    void insertPlayRecord(Map<String,String> playRecord);

    void insertPlayerMoneyRecord(PlayerMoneyRecord mr);
    
    String getConectUs();
    
    Integer getUserMoneyByUserId(Long userId,Integer cid);

	void updateIpAndLastTime(String openId,String ip);

	String findIpByUserId(Long userId);
	
	Integer findTotalGameNum(Long userId);

	Integer getMoneyInit(String cId);
	
}
