package com.up72.game.dao;


import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.up72.game.model.Room;

/**
 * DAO
 * 
 * @author up72
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface RoomMapper {

    void insert(Map<String,String> entity);

    void updateRoomState(Integer roomId,Integer xiaoJuNum);

    List<Map<String,Object>> getMyCreateRoom(Long userId,Integer start,Integer limit,Integer roomType);
    
    Integer getMyCreateRoomTotal(Long userId,Integer start,Integer limit,Integer roomType);
    
    
    


}
