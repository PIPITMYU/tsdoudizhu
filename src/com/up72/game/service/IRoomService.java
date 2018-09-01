/*
 * Powered By [up72-framework]
 * Web Site: http://www.up72.com
 * Since 2006 - 2017
 */

package com.up72.game.service;

import com.up72.game.model.Room;
import com.up72.framework.util.page.PageBounds;

import java.util.List;
import java.util.Map;

import com.up72.framework.util.page.Page;


/**
 * 接口
 * 
 * @author up72
 * @version 1.0
 * @since 1.0
 */
public interface IRoomService {

    void save(Map<String,String> room);

    void updateRoomState(Integer roomId,Integer xiaoJuNum);

    List<Map<String,Object>> getMyCreateRoom(Long userId,Integer start,Integer limit,Integer roomType);
    
    Integer getMyCreateRoomTotal(Long userId,Integer start,Integer limit,Integer roomType);

}
