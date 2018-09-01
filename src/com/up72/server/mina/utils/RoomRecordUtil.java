package com.up72.server.mina.utils;

import java.util.Map;
import com.up72.game.constant.Cnst;
import com.up72.game.model.RoomRecord;
import com.up72.server.mina.utils.redis.RedisUtil;

public class RoomRecordUtil {
	
	private static final MyLog log = MyLog.getLogger(RoomRecordUtil.class);
	
//	private static ConcurrentHashMap<Integer,RoomRecord> m_allRoomRecord = new ConcurrentHashMap<Integer, RoomRecord>();
	
	
	private static final boolean isOpenRecord = true;
	
	public static RoomRecord getRoomRecord(Integer roomId,Long createTime,String cid){
		try {
			RoomRecord roomRecord = RedisUtil.getObject(Cnst.get_HUIFANG(cid).concat(roomId + ""), RoomRecord.class);
			if(roomRecord == null)
				return null;
			if(roomRecord.getCreateTime() != createTime.longValue())
			{
				return null;
			}
			return roomRecord;
		} catch (Exception e) {
			log.E("ERROR", e);
		}
		return null;
	}
	public static void removeRoomRecord(Integer roomId,String cid){
		RedisUtil.deleteByKey(Cnst.get_HUIFANG(cid).concat(roomId + ""));
	}
	
	private static void addRoomRecord(RoomRecord record,String cid){
		if(isOpenRecord)
		{
			RedisUtil.setObject(Cnst.get_HUIFANG(cid).concat(record.getRoomId() + ""), record, 36000);
		}
	}
	//添加回放记录文件
	public static void addRecord(Integer roomId,Long createTime,Map<String,Object> recordMap,boolean newCreate,String cid){
		try {
			RoomRecord roomRecord = null;
			if(newCreate)
			{
				roomRecord = new RoomRecord();
				roomRecord.setCreateTime(createTime);
				roomRecord.setRoomId(roomId);
				roomRecord.getRecords().add(recordMap);
				addRoomRecord(roomRecord,cid);
			}
			else
			{
				roomRecord = getRoomRecord(roomId, createTime,cid);
				if(roomRecord == null)
					return;
				roomRecord.getRecords().add(recordMap);
				addRoomRecord(roomRecord,cid);
			}
		} catch (Exception e) {
			log.E("ERROR", e);
		}
	}
	
	public static void clearLiuJu(Integer roomId,Long createTime,String cid){
		try {
			//清理流局信息
			RoomRecord roomRecord = getRoomRecord(roomId, createTime,cid);
			if(roomRecord == null)
				return;
			if(roomRecord.getRecords().size() < 2)
				return;
			int i = roomRecord.getRecords().size() - 1;
			for (; i > 0; i--) {
				Map map = roomRecord.getRecords().get(i);
				
				int intValue = Integer.valueOf((String)map.get(Cnst.ROUTE_MAP.get("interfaceId")));
				if(intValue == 3)
					break;
				
				roomRecord.getRecords().remove(i);
				if(intValue == 2)
					break;
			}
		} catch (Exception e) {
			log.E("ERROR", e);
		}
	}
	
	public static void save(Integer roomId,Long createTime,String cid){
		try {
			RoomRecord roomRecord = getRoomRecord(roomId, createTime,cid);
			if(roomRecord == null)
				return;
			removeRoomRecord(roomId,cid);
			BackFileUtil.write(roomRecord);
			
		} catch (Exception e) {
			log.E("ERROR", e);
		}
	}
	
	public static void checkOutOfDate(){
//		try {
//			Iterator<Integer> iterator = m_allRoomRecord.keySet().iterator();
//			long now = System.currentTimeMillis();
//			long diff = 3600000l * 3l;
//			while (iterator.hasNext()) {
//				Integer next = iterator.next();
//				RoomRecord roomRecord = m_allRoomRecord.get(next);
//				if(roomRecord == null)
//					continue;
//				
//				if(now - roomRecord.getCreateTime() > diff)
//				{
//					m_allRoomRecord.remove(next);
//					iterator = m_allRoomRecord.keySet().iterator();
//				}
//			}
//		} catch (Exception e) {
//			log.E("ERROR", e);
//		}
	}
}
