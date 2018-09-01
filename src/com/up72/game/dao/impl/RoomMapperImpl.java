package com.up72.game.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import com.up72.game.dao.RoomMapper;
import com.up72.server.mina.utils.MyBatisUtils;
import com.up72.server.mina.utils.MyLog;

/**
 * Created by admin on 2017/6/23.
 */
public class RoomMapperImpl implements RoomMapper {
	private static final MyLog log = MyLog.getLogger(RoomMapperImpl.class);
    @Override
    public void insert(Map<String,String> entity) {
        log.I("保存房间信息");
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null) {
                String sqlName = RoomMapper.class.getName() + ".insert";
                session.insert(sqlName, entity);
                session.commit();
//                MyBatisUtils.closeSessionAndCommit();
            }
        } catch (Exception e) {
        	log.E("insert room数据库操作出错！");
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    @Override
    public void updateRoomState(Integer id,Integer xiaoJuNum) {
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null) {
                String sqlName = RoomMapper.class.getName() + ".updateRoomState";
                Map<Object, Object> map =new HashMap<>();
                map.put("id",id);
                map.put("xiaoJuNum", xiaoJuNum);
                session.update(sqlName,map);
                session.commit();
//                MyBatisUtils.closeSessionAndCommit();
            }
        } catch (Exception e) {
        	log.E("数据库操作出错！");
        } finally {
            session.close();
        }
    }

    @Override
    public List<Map<String, Object>> getMyCreateRoom(Long userId,Integer start,Integer limit,Integer roomType) {
    	 List<Map<String, Object>> result = new ArrayList<>();
         SqlSession session = MyBatisUtils.getSession();
         try {
             if (session != null){
                 String sqlName = RoomMapper.class.getName()+".getMyCreateRoom";
                 log.I("sql name ==>>" + sqlName);
                 Map<Object, Object> map =new HashMap<>();
                 map.put("userId",userId);
                 map.put("start",start);
                 map.put("limit",limit);
                 map.put("roomType",roomType);
                 result = session.selectList(sqlName,map);
                 session.close();
             }
         }catch (Exception e){
        	 log.E("getMyCreateRoom数据库操作出错！");
             e.printStackTrace();
         }finally {
             session.close();
         }
         return result;
    }
    
    @Override
    public Integer getMyCreateRoomTotal(Long userId, Integer start,
    		Integer limit, Integer roomType) {
    	Integer result = 0;
    	SqlSession session = MyBatisUtils.getSession();
            try {
                if (session != null){
                    String sqlName = RoomMapper.class.getName()+".getMyCreateRoomTotal";
                    log.I("sql name ==>>" + sqlName);
                    Map<Object, Object> map =new HashMap<>();
                    map.put("userId",userId);
                    map.put("start",start);
                    map.put("limit",limit);
                    map.put("roomType",roomType);
                    result = session.selectOne(sqlName,map);
                    session.close();
                }
            }catch (Exception e){
            	log.E("getMyCreateRoomTotal数据库操作出错！");
                e.printStackTrace();
            }finally {
                session.close();
            }
            return result;
    }

}
