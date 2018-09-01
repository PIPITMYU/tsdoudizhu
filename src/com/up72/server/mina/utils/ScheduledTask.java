package com.up72.server.mina.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.mina.core.session.IoSession;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.game.model.RoomRecord;
import com.up72.server.mina.function.HallFunctions;
import com.up72.server.mina.function.MessageFunctions;
import com.up72.server.mina.function.TCPGameFunctions;
import com.up72.server.mina.main.MinaServerManager;
import com.up72.server.mina.utils.redis.RedisClient;
import com.up72.server.mina.utils.redis.RedisUtil;

/**
 * Created by Administrator on 2017/7/28.
 */
@Component()
public class ScheduledTask {

	private static MyLog logger = MyLog.getLogger(ScheduledTask.class);

	@Scheduled(cron = Cnst.CLEAN_5)
	public void testTaskWithDate() {
		logger.I("每天凌晨5点清理任务开始执行 ");
		long startTime;
		long currentTime = new Date().getTime();
		try {
			Set<String> cIdRoomMapKeys = RedisUtil.getSameKeys(Cnst.REDIS_PREFIX_ROOMMAP);
			if (cIdRoomMapKeys != null && cIdRoomMapKeys.size() > 0) {
				for (String key : cIdRoomMapKeys) {
					key = key.replace(Cnst.REDIS_PREFIX_ROOMMAP, "");
					String cid = key.split("_")[0];
					String roomId = key.split("_")[1];
					RoomResp room = RedisUtil.getRoomRespByRoomId(roomId, cid);

					startTime = Long.parseLong(room.getCreateTime());
					if ((currentTime - startTime) >= Cnst.ROOM_OVER_TIME) {// 房间存在超过24小时，强制解散
						RedisUtil.deleteByKey(Cnst.get_REDIS_PREFIX_ROOMMAP(cid).concat(roomId + ""));
						//MessageFunctions.updateDatabasePlayRecord(room, cid);// 解散房间
						// 通知在线玩家房间被解散
						List<Player> players = RedisUtil.getPlayerList(roomId, cid);
						MessageFunctions.interface_100111(Cnst.REQ_STATE_12, players, Integer.valueOf(roomId), room.getCreateId());
					}
				}
			}
		} catch (Exception e) {
			logger.E("ERROR", e);
		}

		logger.I("每天5点任务执行结束");
	}

	// @Scheduled(cron = "0/5 * * * * ?")
	@Scheduled(cron = Cnst.CLEAN_EVERY_HOUR)
	public void JVMCount() {
		logger.I("每小时清理任务开始…… ");
		try {

			RoomRecordUtil.checkOutOfDate();
			checkOutOfDateRoomId();
		} catch (Exception e) {
			logger.E("每小时清理任务异常");
		} finally {
			cleanUserEveryHour();
			BackFileUtil.deletePlayRecord();
			cleanPlayRecord();// 需要加上清理战绩的逻辑
			cleanPlayDaiKaiRecord();
		}
		logger.I("每小时清理任务结束");
	}

	@Scheduled(cron = Cnst.COUNT_EVERY_TEN_MINUTE)
	public void onlineNumCount() {
		Map<String, Object> ipcounts = new HashMap<String, Object>();
		Map<String, Object> temp = new HashMap<String, Object>();
		int count = MinaServerManager.tcpServer.getSessions().size();
		temp.put("onlineNum", String.valueOf(count));
		List<Long> userIds = new ArrayList<Long>();

		Iterator<IoSession> iterator = MinaServerManager.tcpServer.getSessions().values().iterator();
		while (iterator.hasNext()) {
			IoSession se = iterator.next();
			try {
				Object o = se.getAttribute(Cnst.USER_SESSION_USER_ID);
				if (o != null) {
					Long userId = Long.parseLong(String.valueOf(o));
					userIds.add(userId);
				} else {
					se.close(true);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		temp.put("userIds", userIds);

		ipcounts.put(Cnst.SERVER_IP, temp);
		RedisUtil.setStringByKey(Cnst.REDIS_ONLINE_NUM_COUNT.concat(Cnst.SERVER_IP), JSONObject.toJSONString(ipcounts));
		logger.I("每分钟统计在线人数完成");

	}

	public static void cleanPlayRecord() {
		int cleanNum = 0;

		Set<String> recordKeys = RedisUtil.getKeys(Cnst.REDIS_PLAY_RECORD_PREFIX_ROE_USER.concat("*"));
		if (recordKeys != null && recordKeys.size() > 0) {
			long ct = System.currentTimeMillis();
			boolean go = true;
			for (String key : recordKeys) {
				go = true;
				while (go) {
					go = false;
					String record = RedisUtil.rpop(key);// 这个只是战绩表hash的field
					if (record != null) {
						try {
							Long createTime = Long.parseLong(record.split("-")[1]);
							if ((ct - createTime) < Cnst.BACKFILE_STORE_TIME) {// 如果没有过期，需要把记录放回原位
								RedisUtil.rpush(key, null, record);
							} else {// 继续删除
								// RedisUtil.hdel(Cnst.REDIS_PLAY_RECORD_PREFIX.concat(record));
								record = null;
								go = true;
								cleanNum++;
							}
						} catch (Exception e) {
							// maybe will
						}
					}
				}
			}
		}

		logger.I("每小时清理战绩完成，共清理过期记录" + cleanNum + "条");
	}

	public static void cleanPlayDaiKaiRecord() {
		int cleanNum = 0;

		Set<String> recordKeys = RedisUtil.getKeys(Cnst.REDIS_PLAY_RECORD_PREFIX_ROE_DAIKAI.concat("*"));
		if (recordKeys != null && recordKeys.size() > 0) {
			long ct = System.currentTimeMillis();
			boolean go = true;
			for (String key : recordKeys) {
				go = true;
				while (go) {
					go = false;
					String record = RedisUtil.rpop(key);// 这个只是战绩表hash的field
					if (record != null) {
						try {
							Long createTime = Long.parseLong(record.split("-")[1]);
							if ((ct - createTime) < Cnst.BACKFILE_STORE_TIME) {// 如果没有过期，需要把记录放回原位
								RedisUtil.rpush(key, null, record);
							} else {// 继续删除
								record = null;
								go = true;
								cleanNum++;
							}
						} catch (Exception e) {
							logger.E("清理代开错误error:" + e);
							// maybe will
						}
					}
				}
			}
		}

		logger.I("每小时清理代开战绩完成，共清理过期记录" + cleanNum + "条");
	}

	public static void cleanUserEveryHour() {

	}

	public static void checkOutOfDateRoomId() {
		try {
			Set<Entry<Integer, Long>> entrySet = HallFunctions.roomIdMap.entrySet();

			long now = System.currentTimeMillis();
			long diff = 3600000l * 3l;
			Iterator<Entry<Integer, Long>> iterator = entrySet.iterator();
			while (iterator.hasNext()) {
				Entry<Integer, Long> next = iterator.next();
				if (now - next.getValue() > diff) {
					HallFunctions.roomIdMap.remove(next.getKey());
					iterator = HallFunctions.roomIdMap.entrySet().iterator();
				}
			}
		} catch (Exception e) {
			logger.E("ERROR", e);
		}
	}

}