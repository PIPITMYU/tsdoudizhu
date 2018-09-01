package com.up72.game.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import com.up72.game.dto.resp.ClubInfo;
import com.up72.game.dto.resp.ClubUser;
import com.up72.game.dto.resp.ClubUserUse;
import com.up72.server.mina.utils.MyBatisUtils;
import com.up72.server.mina.utils.MyLog;
import com.up72.server.mina.utils.StringUtils;

public class ClubMapper {
	
	
	private static final MyLog log = MyLog.getLogger(ClubMapper.class);
	public static ClubInfo selectByClubId(Integer clubId, String cid) {
		ClubInfo result = null;
		SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = ClubMapper.class.getName()+".selectByClubId";
                log.I("sql name ==>>" + sqlName);
                Map<Object, Object> map =new HashMap<>();
                map.put("clubId",clubId);
                map.put("cid",cid);
                List<ClubInfo> selectList =session.selectList(sqlName, map);
                if(!selectList.isEmpty())
					result = selectList.get(0);
                session.close();
            }
        }catch (Exception e){
        	log.E("selectByClubId数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return result;
	}
	
	public static String selectCreateName(Integer userId ,String cid) {
		String result = null;
		SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = ClubMapper.class.getName()+".selectCreateName";
                log.I("sql name ==>>" + sqlName);
                Map<Object, Object> map =new HashMap<>();
                map.put("userId",userId);
                map.put("cid",cid);
                List<String> selectList =session.selectList(sqlName, map);
                if(!selectList.isEmpty())
					result = selectList.get(0);
                session.close();
            }
        }catch (Exception e){
        	log.E("selectByClubId数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return result;
	}
	
	public static Integer allUsers(Integer clubId, String cid) {
		Integer result = 0;
		SqlSession session = MyBatisUtils.getSession();
        try {
            if (session != null){
                String sqlName = ClubMapper.class.getName()+".allUsers";
                log.I("sql name ==>>" + sqlName);
				Map<String, Object> map =new HashMap<String, Object>();
				map.put("clubId",clubId);
				map.put("cid",cid);
                List<Integer> selectList =session.selectList(sqlName, map);
                if(!selectList.isEmpty())
					result = selectList.get(0);
                session.close();
            }
        }catch (Exception e){
        	log.E("allUsers数据库操作出错！");
            e.printStackTrace();
        }finally {
            session.close();
        }
        return result;
	}
	
		public static List<ClubUser> selectClubByUserId(Long userId, String cid) {
		
			List<ClubUser> list = null;
			SqlSession session = MyBatisUtils.getSession();
			try {
				if (session != null){
					String sqlName = ClubMapper.class.getName()+".selectClubByUserId";
					log.I("sql name ==>>" + sqlName);
					Map<String, Object> map =new HashMap<String, Object>();
					map.put("userId",userId);
					map.put("cid",cid);
					list = session.selectList(sqlName, map);
					session.close();
				}
			}catch (Exception e){
				log.E("selectClubByUserId数据库操作出错！");
				e.printStackTrace();
			}finally {
				session.close();
			}
			return list;
		}
		
		public static ClubUser selectUserByUserIdAndClubId(Long userId, Integer clubId, String cid) {

			ClubUser result = null;
			SqlSession session = MyBatisUtils.getSession();
			try {
	            if (session != null){
	                String sqlName = ClubMapper.class.getName()+".selectUserByUserIdAndClubId";
	                log.I("sql name ==>>" + sqlName);
	                Map<String, Object> map =new HashMap<String, Object>();
	                map.put("userId",userId);
	                map.put("clubId",clubId);
	                map.put("cid",cid);
	                List<ClubUser> selectList =session.selectList(sqlName, map);
	                if(!selectList.isEmpty())
						result = selectList.get(0);
	                session.close();
	            }
	        }catch (Exception e){
	            log.E("selectUserByUserIdAndClubId数据库操作出错！");
	            e.printStackTrace();
	        }finally {
	            session.close();
	        }
	        return result;
		}
		
		public static Integer countByClubId(Integer clubId, Integer status) {
			
			Integer num = null;
			SqlSession session = MyBatisUtils.getSession();
	        try {
	            if (session != null){
	                String sqlName = ClubMapper.class.getName()+".countByClubId";
	                log.I("sql name ==>>" + sqlName);
	                Map<String, Object> map =new HashMap<String, Object>();
	                map.put("clubId",clubId);
	                map.put("status",status);
	                List<Integer> selectList =session.selectList(sqlName, map);
	                if(!selectList.isEmpty())
						num = selectList.get(0);
	                session.close();
	            }
	        }catch (Exception e){
	        	log.E("countByClubId数据库操作出错！");
	            e.printStackTrace();
	        }finally {
	            session.close();
	        }
	        return num;
		}
		
		public static Integer countByUserId(Long userId ,String cid) {

			Integer num = null;
			SqlSession session = MyBatisUtils.getSession();
	        try {
	            if (session != null){
	                String sqlName = ClubMapper.class.getName()+".countByUserId";
	                Map<String, Object> map =new HashMap<String, Object>();
	                map.put("clubId",userId);
	                map.put("status",cid);
	                List<Integer> selectList =session.selectList(sqlName, map);
	                if(!selectList.isEmpty())
						num = selectList.get(0);
	                session.close();
	            }
	        }catch (Exception e){
	        	log.E("countByUserId数据库操作出错！");
	            e.printStackTrace();
	        }finally {
	            session.close();
	        }
	        return num;
		}
		
		public static int insert(ClubUser clubUser) {
			
			int num = 0;
			SqlSession session = MyBatisUtils.getSession();
	        try {
	            if (session != null){
	                String sqlName = ClubMapper.class.getName()+".insert";
	                log.I("sql name ==>>" + sqlName);
	                num = session.insert(sqlName, clubUser);
	                session.commit();
	            }
	        }catch (Exception e){
	        	log.E("insert数据库操作出错！");
	            e.printStackTrace();
	        }finally {
	            session.close();
	        }
	        return num;
		}
		
		public static int updateById(ClubUser clubUser) {

			int num = 0;
			SqlSession session = MyBatisUtils.getSession();
	        try {
	            if (session != null){
	                String sqlName = ClubMapper.class.getName()+".updateById";
	                log.I("sql name ==>>" + sqlName);
	                num = session.update(sqlName, clubUser);
	                session.commit();
	            }
	        }catch (Exception e){
	        	log.E("updateById数据库操作出错！");
	            e.printStackTrace();
	        }finally {
	            session.close();
	        }
	        return num;
		}
		
		public static Integer sumMoneyByClubIdAndDate(Integer clubId ,String cid) {
			
			Integer num = null;
			SqlSession session = MyBatisUtils.getSession();
	        try {
	            if (session != null){
	                String sqlName = ClubMapper.class.getName()+".sumMoneyByClubIdAndDate";
	                log.I("sql name ==>>" + sqlName);
	                Map<String, Object> map =new HashMap<String, Object>();
	                map.put("morning",StringUtils.getTimesmorning());
	                map.put("night",StringUtils.getTimesNight());
	                map.put("clubId",clubId);
	                map.put("cid",cid);
	                List<Integer> selectList =session.selectList(sqlName, map);
	                if(!selectList.isEmpty())
						num = selectList.get(0);
	                session.close();
	            }
	        }catch (Exception e){
	        	log.E("sumMoneyByClubIdAndDate数据库操作出错！");
	            e.printStackTrace();
	        }finally {
	            session.close();
	        }
	        return num;
		}
		
		public static List<Integer> todayPerson(String cid, Integer clubId) {
			List<Integer>  num = new ArrayList<Integer>();
			SqlSession session = MyBatisUtils.getSession();
	        try {
	            if (session != null){
	                String sqlName = ClubMapper.class.getName()+".todayPerson";
	                log.I("sql name ==>>" + sqlName);
	                HashMap<String,Object> map = new HashMap<String, Object>();
	        		map.put("clubId", clubId);
	        		map.put("cid", cid);
	        		map.put("morning", StringUtils.getTimesmorning());
	        		map.put("night", StringUtils.getTimesNight());
	                num = session.selectList(sqlName, map);
	                session.close();
	            }
	        }catch (Exception e){
	        	log.E("todayPerson数据库操作出错！");
	            e.printStackTrace();
	        }finally {
	            session.close();
	        }
	        return num;
		}
		
		public static Integer todayGames(String cid, Integer clubId) {
			Integer num = 0;
			SqlSession session = MyBatisUtils.getSession();
	        try {
	            if (session != null){
	                String sqlName = ClubMapper.class.getName()+".todayGames";
	                log.I("sql name ==>>" + sqlName);
	                HashMap<String,Object> map = new HashMap<String, Object>();
	        		map.put("clubId", clubId);
	        		map.put("cid", cid);
	        		map.put("morning", StringUtils.getTimesmorning());
	        		map.put("night", StringUtils.getTimesNight());
	                List<Integer> selectList =session.selectList(sqlName, map);
	                if(!selectList.isEmpty())
						num = selectList.get(0);
	                session.close();
	            }
	        }catch (Exception e){
	        	log.E("todayGames数据库操作出错！");
	            e.printStackTrace();
	        }finally {
	            session.close();
	        }
	        return num;
		}
		
		public static Integer selectUserState(Integer clubId, Long userId, String cid) {
			Integer num = 0;
			SqlSession session = MyBatisUtils.getSession();
	        try {
	            if (session != null){
	                String sqlName = ClubMapper.class.getName()+".selectUserState";
	                log.I("sql name ==>>" + sqlName);
	                Map<String, Object> map =new HashMap<String, Object>();
	                map.put("userId",userId);
	                map.put("clubId",clubId);
	                map.put("cid",cid);
	                List<Integer> selectList =session.selectList(sqlName, map);
	                if(!selectList.isEmpty())
						num = selectList.get(0);
	                session.close();	                
	            }
	        }catch (Exception e){
	        	log.E("selectUserState数据库操作出错！");
	            e.printStackTrace();
	        }finally {
	            session.close();
	        }
	        return num;
		}
		

		public static Integer userTodayGames(Integer clubId,Long userId,String cid) {
			Integer num = 0;
			SqlSession session = MyBatisUtils.getSession();
	        try {
	            if (session != null){
	                String sqlName = ClubMapper.class.getName()+".userTodayGames";
	                log.I("sql name ==>>" + sqlName);
	                HashMap<String,Object> map = new HashMap<String, Object>();
	        		map.put("clubId", clubId);
	        		map.put("userId", userId);
	        		map.put("cid", cid);
	        		map.put("morning", StringUtils.getTimesmorning());
	        		map.put("night", StringUtils.getTimesNight());
	                List<Integer> selectList =session.selectList(sqlName, map);
	                if(!selectList.isEmpty())
						num = selectList.get(0);
	                session.close();
	            }
	        }catch (Exception e){
	        	log.E("userTodayGames数据库操作出错！");
	            e.printStackTrace();
	        }finally {
	            session.close();
	        }
	        return num;
		}
		
		public static Integer todayUse(Integer clubId, Integer userId, String cid) {
			Integer num = null;
			SqlSession session = MyBatisUtils.getSession();
	        try {
	            if (session != null){
	                String sqlName = ClubMapper.class.getName()+".todayUse";
	                log.I("sql name ==>>" + sqlName);
	                Map<String, Object> map =new HashMap<String, Object>();
	                map.put("userId",userId);
	                map.put("clubId",clubId);
	                map.put("cid",cid);
	                map.put("morning", StringUtils.getTimesmorning());
	                map.put("night",StringUtils.getTimesNight());
	                num = session.selectOne(sqlName,map);
	                if(num==null)
	                	num=0;
	                session.close();
	            }
	        }catch (Exception e){
	        	log.E("todayUse数据库操作出错！");
	            e.printStackTrace();
	        }finally {
	            session.close();
	        }
	        return num;
		}
		
		public static void saveRoom(HashMap<String, String> map) {
	        SqlSession session = MyBatisUtils.getSession();
	        try {
	            if (session != null) {
	                String sqlName = ClubMapper.class.getName() + ".saveRoom";
	                session.insert(sqlName, map);
	                session.commit();
	            }
	        } catch (Exception e) {
	        	log.E("saveRoom数据库操作出错！");
	            e.printStackTrace();
	        } finally {
	            session.close();
	        }
	    }



		
		public static void updateRoomState(Integer roomId, Integer xiaoJuNum) {
			SqlSession session = MyBatisUtils.getSession();
	        try {
	            if (session != null) {
	                String sqlName = ClubMapper.class.getName() + ".updateRoomState";
	                Map<Object, Object> map =new HashMap<>();
	                map.put("roomId",roomId);
	                map.put("xiaoJuNum", xiaoJuNum);
	                session.update(sqlName,map);
	                session.commit();
	            }
	        } catch (Exception e) {
	        	log.E("数据库操作出错！");
	        } finally {
	            session.close();
	        }
		}
		
		public static void updateClubMoney(Integer clubId, Integer money,Integer cid) {
			SqlSession session = MyBatisUtils.getSession();
	        try {
	            if (session != null) {
	                String sqlName = ClubMapper.class.getName() + ".updateClubMoney";
	                Map<Object, Object> map =new HashMap<>();
	                map.put("clubId",clubId);
	                map.put("money", money);
	                map.put("cid", cid);
	                session.update(sqlName,map);
	                session.commit();
	            }
	        } catch (Exception e) {
	        	log.E("数据库操作出错！");
	        } finally {
	            session.close();
	        }
		}
		
		public static void saveUserUse(ClubUserUse enetiy) {
	        SqlSession session = MyBatisUtils.getSession();
	        try {
	            if (session != null) {
	                String sqlName = ClubMapper.class.getName() + ".saveUserUse";
	                session.insert(sqlName, enetiy);
	                session.commit();
	            }
	        } catch (Exception e) {
	        	log.E("saveUserUse数据库操作出错！");
	            e.printStackTrace();
	        } finally {
	            session.close();
	        }
	    }
		/**
		 * 更新数据库俱乐部的钱
		 * @param clubId
		 * @param cid
		 * @return
		 */
		public static Integer getClubMoneyByClubId(Integer clubId, String cid) {
			Integer num = null;
			SqlSession session =MyBatisUtils.getSession();
			try {
				if(session!=null){
					String sqlName=ClubMapper.class.getName()+".getClubMoneyByClubId";
					Map<Object, Object> map= new HashMap<Object, Object>();
					map.put("clubId", clubId);
					map.put("cid", cid);
					num=session.selectOne(sqlName,map);
					if(num==null){
						num=0;
					}
					session.close();
				}
				
			} catch (Exception e) {
				log.E("getClubMoneyByClubId数据库操作出错！");
	            e.printStackTrace();		
	           }finally{
	        	   session.close();
	           }
			return num;
		}
		
		
		/**
		 * 向俱乐部分数表添加房间所有玩家分数信息
		 */
	    public static void insertPlayRecord(Map<String,String> playRecord) {
	        SqlSession session = MyBatisUtils.getSession();
	        try {
	            if (session != null) {
	                String sqlName = UserMapper.class.getName() + ".insertPlayRecord";
	                session.insert(sqlName, playRecord);
	                session.commit();
//		                MyBatisUtils.closeSessionAndCommit();
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            session.close();
	        }
	        
	    }

			/**
		 * 通过用户id查找他所有的俱乐部id
		 * @param userId
		 * @return
		 */
		public static List<Integer> selectClubIdsByUserId(Long userId,String cid) {
			
			List<Integer> list = null;
			SqlSession session = MyBatisUtils.getSession();
			try {
				if (session != null){
					String sqlName = ClubMapper.class.getName()+".selectClubIdsByUserId";
					System.out.println("sql name ==>>" + sqlName);
					Map<String, Object> map =new HashMap<String, Object>();
					map.put("userId",userId);
					map.put("cid",cid);
					list = session.selectList(sqlName, map);
					session.close();
				}
			}catch (Exception e){
				System.out.println("selectClubIdsByUserId数据库操作出错！");
				e.printStackTrace();
			}finally {
				session.close();
			}
			return list;
		}
}
