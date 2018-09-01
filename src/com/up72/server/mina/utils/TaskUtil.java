package com.up72.server.mina.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.fastjson.JSONObject;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.ClubInfo;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.server.mina.bean.ProtocolData;
import com.up72.server.mina.function.MessageFunctions;
import com.up72.server.mina.function.TCPGameFunctions;
import com.up72.server.mina.main.MinaServerManager;
import com.up72.server.mina.utils.redis.RedisUtil;

/**
 * Created by Administrator on 2017/7/28.
 */
public class TaskUtil implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8155411098353016824L;
	
	private static final MyLog log = MyLog.getLogger(TaskUtil.class);

	public static void initTaskSchdual() {
        //加载spring的定时配置文件，然后，启动定时任务
        ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"com/up72/server/mina/utils/applicationContext.xml"});
        log.I("加载spring的定时配置文件完成");
    }

    public class DissolveRoomTask implements Runnable,Serializable{

        /**
		 * 
		 */
		private static final long serialVersionUID = 5796473046207010831L;
		private int roomId;
        private int type;//1创建房间40分钟解散任务；2申请解散房间5分钟解散；
        private Integer taskId;
        private String cid;
        public DissolveRoomTask(int roomId,int type,String cid){
            synchronized (this){
                this.roomId = roomId;
                this.type = type;
                this.cid = cid;
            }
        }

        @Override
        public void run() {
            synchronized (this){
                try {
                    
                    taskId = 0;
                    if (this.type == Cnst.DIS_ROOM_TYPE_1){
                        this.wait(Cnst.ROOM_CREATE_DIS_TIME);
                    }else if(this.type == Cnst.DIS_ROOM_TYPE_2){
                        this.wait(Cnst.ROOM_DIS_TIME);
                    }
                    
                    RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId),cid);
                    if (room == null){
                        return ;
                    }
                    if (this.type == Cnst.DIS_ROOM_TYPE_1){
                        taskId = room.getCreateDisId();
                    }else if(this.type == Cnst.DIS_ROOM_TYPE_2){
                        taskId = room.getApplyDisId();
                    }
                    
                    Integer resultInfo = TCPGameFunctions.disRoomIdResultInfo.get(taskId);
                    if (resultInfo!=null){//notify的，需要终止直接解散
                    	log.I(roomId+"通知我终止解散房间");
                    }else{//非notify的，直接解散
                    	if (room.getDissolveRoom()==null) {//创建房间超过四十分钟，自动解散
                    		List<Player> players = RedisUtil.getPlayerList(room,cid);
                    		
                    		List<Player> disPlayers = new ArrayList<Player>();
                    		if (players!=null&&players.size()>0) {
                    			for(Player p:players){
                    				if(p == null)
                    					continue;
                    				if(!room.getRoomId().equals(p.getRoomId()))
                    					continue;
//                    				p.initPlayer(null,Cnst.PLAYER_STATE_DATING,0l);
    	        			        RedisUtil.updateRedisData(null, p,cid);
    	        			        disPlayers.add(p);
    	        		        }
                    			
							}
                    		
                    		Player fangzhu = RedisUtil.getPlayerByUserId(String.valueOf(room.getCreateId()),cid);
                    		if (fangzhu!=null) {
                    			if(room.getExtraType() == Cnst.ROOM_TYPE_1){
                					
                					if(String.valueOf(roomId).length() == 6){
                                		if (fangzhu!=null) {
                            				fangzhu.setMoney(fangzhu.getMoney()+Cnst.moneyMap_1.get(room.getCircleNum()));
                            				RedisUtil.updateRedisData(null, fangzhu,cid);
                						}
                            		}
                            		if(String.valueOf(roomId).length() == 7){
                						//退还俱乐部房卡
                            			ClubInfo redisClub = RedisUtil.getClubInfoByClubId(Cnst.get_REDIS_PREFIX_CLUBMAP(cid)+room.getClubId().toString());
                            			redisClub.setRoomCardNum(redisClub.getRoomCardNum()+Cnst.moneyMap_1.get(room.getCircleNum()));
                            			RedisUtil.setClubInfoByClubId(Cnst.get_REDIS_PREFIX_CLUBMAP(cid)+room.getClubId().toString(), redisClub);
                					}
                    			}else{
                    				fangzhu.setMoney(fangzhu.getMoney()+Cnst.moneyMap_2.get(room.getCircleNum()));
                    				RedisUtil.updateRedisData(null, fangzhu,cid);
                    			} 
							}
                    		
                    		RedisUtil.deleteByKey(Cnst.get_REDIS_PREFIX_ROOMMAP(cid).concat(String.valueOf(roomId)));
                    		
                    		if(room.getExtraType().intValue() == Cnst.ROOM_EXTRA_TYPE_2)
            				{
            					RedisUtil.hdel(Cnst.get_ROOM_DAIKAI_KEY(cid).concat(room.getCreateId() + ""), room.getRoomId() + "");
            				}
                    		
                    		//通知房间内用户房间被解散
                    		MessageFunctions.interface_100111(Cnst.REQ_STATE_13, disPlayers,room.getRoomId(),room.getCreateId());

                    		MessageFunctions.interface_100140(room,cid);
                			for(Player p:disPlayers){
                				if(p == null)
                					continue;
                				if(!room.getRoomId().equals(p.getRoomId()))
                					continue;
                				p.initPlayer(null,Cnst.PLAYER_STATE_DATING,0l);
	        			        RedisUtil.updateRedisData(null, p,cid);
	        		        }
                    			
                    	}else{//房间内申请解散超过5分钟自动解散
							List<Map<String, Object>> otherAgreeList = room.getDissolveRoom().getOthersAgree();
	                    	if (otherAgreeList!=null&&otherAgreeList.size()>0) {
	                    		for(int i=0;i<otherAgreeList.size();i++){
	                    			otherAgreeList.get(i).put("agree", 1);
	                    		}
							}
	                    	
	                    	Map<String,Object> info = new HashMap<>();
	                        info.put("dissolveTime",room.getDissolveRoom().getDissolveTime());
	                        info.put("userId",room.getDissolveRoom().getUserId());
	                        info.put("othersAgree",otherAgreeList);
	                        JSONObject result = TCPGameFunctions.getJSONObj(100204,1,info);
	                        ProtocolData pd = new ProtocolData(100204, result.toJSONString());

	                        List<Player> players = RedisUtil.getPlayerList(room,cid);
	                        
	                        room.setState(Cnst.ROOM_STATE_YJS);

	                        ArrayList<Long> arrayList = new ArrayList<Long>();
	        				for (Player player2 : players) {
	        					if (player2 != null) {
	        						arrayList.add(player2.getScore());
	        					} else {
	        						arrayList.add(null);
	        					}
	        				}
	        				room.setScore(arrayList);
	        				
	        				
	        				RedisUtil.updateRedisData(room, null,cid);
	        				
	        				////////////////////////
	        				// 这里更新数据库吧
	        				TCPGameFunctions.updateDatabasePlayRecord(room,cid);
	        				
	        				for(Player p:players){
	        					if(p == null)
	        						continue;
	        					if(!room.getRoomId().equals(p.getRoomId()))
                					continue;
	        					
	        					IoSession se = MinaServerManager.tcpServer.getSessions().get(p.getSessionId());
	        		            if(se!=null&&se.isConnected()){
	        		                se.write(pd);
	        		            }
	        		            p.initPlayer(null,Cnst.PLAYER_STATE_DATING,0l);
	        			        RedisUtil.updateRedisData(null, p,cid);
	        		        }
	        				//数据记录保存到文件里面
	        				RoomRecordUtil.save(room.getRoomId(), Long.valueOf(room.getCreateTime()),cid);
	        				
	        				if(room.getExtraType().intValue() == Cnst.ROOM_EXTRA_TYPE_2)
	        				{
	        					RedisUtil.hdel(Cnst.get_ROOM_DAIKAI_KEY(cid).concat(room.getCreateId() + ""), room.getRoomId() + "");
	        				}
	        				
	        				/////////////////////			
	        				MessageFunctions.interface_100140(room,cid);
						}
                    }
                   
                } catch (Exception e){
                    e.printStackTrace();
                }finally{
                	if(this.taskId != null)
                	{
                		TCPGameFunctions.disRoomIdMap.remove(taskId);
                		TCPGameFunctions.disRoomIdResultInfo.remove(taskId);
                	}
                    Thread.currentThread().interrupt();
                }
            }
        }
    }


}
