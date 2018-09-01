package com.up72.game.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.session.SqlSession;

import com.up72.game.constant.Cnst;
import com.up72.game.dao.UserMapper;
import com.up72.game.dto.resp.Feedback;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.PlayerRecord;
import com.up72.game.model.PlayerMoneyRecord;
import com.up72.game.model.SystemMessage;
import com.up72.game.model.User;
import com.up72.server.mina.utils.MyBatisUtils;
import com.up72.server.mina.utils.MyLog;

/**
 * Created by admin on 2017/6/22.
 */
public class UserMapperImpl implements UserMapper {
	private static final MyLog log = MyLog.getLogger(UserMapperImpl.class);
    @Override
    public void insert(Player entity) {
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null) {
                String sqlName = UserMapper.class.getName() + ".insert";
                session.insert(sqlName, entity);
                session.commit();
//                MyBatisUtils.closeSessionAndCommit();
            }
        } catch (Exception e) {
        	log.E(e+"插入用户出错");
        } finally {
            session.close();
        }
    }

    @Override
    public void update(User entity) {

    }

    @Override
    public void updateMoney(Integer money, String userId,Integer cid) {
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null) {
                String sqlName = UserMapper.class.getName() + ".updateMoney";
                Map<Object, Object> map =new HashMap<>();
                map.put("money",money);
                map.put("userId",userId);
                map.put("cid", cid);
                session.update(sqlName,map);
                session.commit();
//                MyBatisUtils.closeSessionAndCommit();
            }
        } catch (Exception e) {
        	log.E(e+"更新money出错");
        } finally {
            session.close();
        }
    }

    @Override
    public Player findById(Long id) {
        Player result = null;
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null) {
                String sqlName =  UserMapper.class.getName()+".findById";
                log.I("sql name ==>> " + sqlName);
                result = session.selectOne(sqlName, id);
                session.close();
            }
        } catch (Exception e) {
        	log.E("数据库操作出错！");
            e.printStackTrace();
        } finally {
            session.close();
        }
        return result;
    }

    @Override
    public Player findByOpenId(String openId,String cid){
        Player result = null;
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = UserMapper.class.getName()+".findByOpenId";
                log.I("sql name ==>>" + sqlName);
                Map<Object, Object> map =new HashMap<>();
                map.put("openId",openId);
                map.put("cid",cid);
                result = session.selectOne(sqlName, map);
                session.close();
            }
        }catch (Exception e){
        	log.E("findByOpenId数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return result;
    }
    
    
    

    @Override
	public boolean updateUserId(Long id, Long userId) {
    	 return false;
	}

	@Override
    public List<PlayerRecord> findPlayerRecordByUserId(Long userId,Integer start,Integer limit) {
        List<PlayerRecord> result = new ArrayList<>();
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = UserMapper.class.getName()+".findPlayerRecordByUserId";
                log.I("sql name ==>>" + sqlName);
                Map<Object, Object> map =new HashMap<>();
                map.put("userId",userId);
                map.put("start",start);
                map.put("limit",limit);
                result = session.selectList(sqlName,map);
                session.close();
            }
        }catch (Exception e){
        	log.E("findPlayerRecordByUserId数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return result;
    }

    @Override
    public void userFeedback(Feedback feedback) {
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = UserMapper.class.getName()+".userFeedback";
                log.I("sql name ==>>" + sqlName);
                Map<Object, Object> map =new HashMap<>();
                map.put("userId",feedback.getUserId());
                map.put("tel",feedback.getTel());
                map.put("content",feedback.getContent());
                map.put("createTime",feedback.getCreateTime());
                session.insert(sqlName,map);
                session.commit();
//                MyBatisUtils.closeSessionAndCommit();
            }
        }catch (Exception e){
            e.printStackTrace();
            log.E("userFeedback数据库操作出错！");
        }finally {
            session.close();
        }
    }

    @Override
    public Integer isExistUserId(String userId) {
        Integer result = null;
        log.I("isExistUserId openId" + userId);
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = UserMapper.class.getName()+".isExistUserId";
                log.I("sql name ==>>" + sqlName);
                Map map =new HashMap<>();
                map.put("userId",userId);
                result = session.selectOne(sqlName, map);
                session.close();
            }
        }catch (Exception e){
        	log.E("isExistUserId数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return result;
    }

    @Override
    public void updateUserAgree(Long userId) {
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null) {
                String sqlName = UserMapper.class.getName() + ".updateUserAgree";
                Map<Object, Object> map =new HashMap<>();
                map.put("userId",userId);
                session.update(sqlName,map);
                session.commit();
//                MyBatisUtils.closeSessionAndCommit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    @Override
    public String getNotice(String cid) {
        String result = "";
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = UserMapper.class.getName()+".getNotice";
                log.I("sql name ==>>" + sqlName);
                
                Map map =new HashMap<>();
                map.put("cid",cid);
                result = session.selectOne(sqlName, map);
                session.close();
            }
        }catch (Exception e){
        	log.E("getNotice数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return result;
    }
    
    @Override
    public String getConectUs() {
        String result = "";
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = UserMapper.class.getName()+".getConectUs";
                log.I("sql name ==>>" + sqlName);
                result = session.selectOne(sqlName);
                session.close();
            }
        }catch (Exception e){
        	log.E("getConectUs数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return result;
    }



    @Override
    public List<SystemMessage> getSystemMessage(Long userId, Integer start, Integer limit) {
        List<SystemMessage> result = new ArrayList<>();
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = UserMapper.class.getName()+".getSystemMessage";
                log.I("sql name ==>>" + sqlName);
                Map<Object, Object> map =new HashMap<Object, Object>();
                map.put("userId",userId);
                map.put("start",start);
                map.put("limit",limit);
                result = session.selectList(sqlName,map);
                session.close();
            }
        }catch (Exception e){
        	log.E("getSystemMessage！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return result;
    }

    @Override
    public void insertPlayRecord(Map<String,String> playRecord) {
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null) {
                String sqlName = UserMapper.class.getName() + ".insertPlayRecord";
                session.insert(sqlName, playRecord);
                session.commit();
//                MyBatisUtils.closeSessionAndCommit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
    
    
    
    
    @Override
    public void insertPlayerMoneyRecord(PlayerMoneyRecord mr) {
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null) {
                String sqlName = UserMapper.class.getName() + ".insertPlayerMoneyRecord";
                session.insert(sqlName, mr);
                session.commit();
//                MyBatisUtils.closeSessionAndCommit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
    
    
    @Override
    public Integer getUserMoneyByUserId(Long userId,Integer cid) {
        Integer result = null;
        log.I("getUserMoneyByUserId userId -> " + userId);
        log.I("getUserMoneyByUserId cid -> " + cid);
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null) {
            	 Map<Object, Object> map =new HashMap<>();
            	 map.put("userId", userId);
            	 map.put("cid", cid);
                String sqlName =  UserMapper.class.getName()+".getUserMoneyByUserId";
                log.I("sql name ==>> " + sqlName);
                result = session.selectOne(sqlName, map);
                session.close();
            }
        } catch (Exception e) {
        	log.I("数据库操作出错！getUserMoneyByUserId");
            e.printStackTrace();
        } finally {
            session.close();
        }
        return result;
    }

	@Override
	public void updateIpAndLastTime(String openId,String ip) {
		 SqlSession session = MyBatisUtils.getSession();
	        try {
	            if (session != null) {
	                String sqlName = UserMapper.class.getName() + ".updateIpAndLastTime";
	                Map<Object, Object> map =new HashMap<>();
	                map.put("openId", openId);
	                map.put("lastLoginTime",System.currentTimeMillis());
	                map.put("ip",ip);
	                session.update(sqlName,map);
	                session.commit();
//	                MyBatisUtils.closeSessionAndCommit();
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            session.close();
	        }
	}

	@Override
	public String findIpByUserId(Long userId) {
			String result = null;
	        SqlSession session = MyBatisUtils.getSession();
	        try {
	            if (session != null){
	                String sqlName = UserMapper.class.getName()+".findIpByUserId";
	                log.I("sql name ==>>" + sqlName);
	                Map<Object,Object> map =new HashMap<>();
	                map.put("userId",userId);
	                result = session.selectOne(sqlName, map);
	                session.close();
	            }
	        }catch (Exception e){
	        	log.I("findIpByUserId数据库操作出错！");
	            e.printStackTrace();
	        }finally {
	            session.close();
	        }
	        return result;
	}

	@Override
	public Integer findTotalGameNum(Long userId) {
		Integer result = null;
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = UserMapper.class.getName()+".findTotalGameNum";
                log.I("sql name ==>>" + sqlName);
                Map<Object,Object> map =new HashMap<>();
                map.put("userId",userId);
                result = session.selectOne(sqlName, map);
                session.close();
            }
        }catch (Exception e){
        	log.E("findIpByUserId数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return result;
	}
	
	@Override
	public Integer getMoneyInit(String cId) {
		Integer result = null;
        SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = UserMapper.class.getName()+".getMoneyInit";
                Map<Object,Object> map =new HashMap<>();
                map.put("cid",cId);
                result = session.selectOne(sqlName, map);
                session.close();
            }
        }catch (Exception e){
        	log.E("getMoneyInit数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
            if (result==null) {
            	result = Cnst.MONEY_INIT;
			}
        }
        return result;
	}
    
    
}
