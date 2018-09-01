/*
 * Powered By [up72-framework]
 * Web Site: http://www.up72.com
 * Since 2006 - 2017
 */

package com.up72.game.service.impl;

import java.util.*;

import com.up72.game.dao.impl.RoomMapperImpl;
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

import com.up72.game.service.IRoomService;
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
public class RoomServiceImpl implements IRoomService {


    private RoomMapper roomMapper =new RoomMapperImpl();

    public void save(Map<String,String> room) {
        roomMapper.insert(room);
    }

    @Override
    public void updateRoomState(Integer id,Integer xiaoJuNum) {
        roomMapper.updateRoomState(id,xiaoJuNum);
    }
    
    @Override
    public List<Map<String, Object>> getMyCreateRoom(Long userId,
    		Integer start, Integer limit,Integer roomType) {
    	return roomMapper.getMyCreateRoom(userId, start, limit,roomType);
    }
    
    @Override
    public Integer getMyCreateRoomTotal(Long userId, Integer start,
    		Integer limit, Integer roomType) {
    	return roomMapper.getMyCreateRoomTotal(userId, start, limit, roomType);
    }
    
}
