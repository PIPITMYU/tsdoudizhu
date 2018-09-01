package com.up72.server.mina.function;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.mina.core.session.IoSession;

import com.alibaba.fastjson.JSONObject;
import com.up72.game.constant.Cnst;
import com.up72.game.dao.ClubMapper;
import com.up72.game.dto.resp.ClubInfo;
import com.up72.game.dto.resp.ClubUserUse;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.game.model.PlayerMoneyRecord;
import com.up72.game.service.IRoomService;
import com.up72.game.service.IUserService;
import com.up72.game.service.IUserService_login;
import com.up72.game.service.impl.RoomServiceImpl;
import com.up72.game.service.impl.UserServiceImpl;
import com.up72.game.service.impl.UserService_loginImpl;
import com.up72.server.mina.bean.ProtocolData;
import com.up72.server.mina.utils.CommonUtil;
import com.up72.server.mina.utils.MyLog;
import com.up72.server.mina.utils.PostUtil;
import com.up72.server.mina.utils.StringUtils;
import com.up72.server.mina.utils.TaskUtil;
import com.up72.server.mina.utils.TaskUtil.DissolveRoomTask;
import com.up72.server.mina.utils.redis.RedisUtil;

public class TCPGameFunctions {

	public static final MyLog logger = MyLog.getLogger(TCPGameFunctions.class);
	public static IUserService userService = new UserServiceImpl();
	public static IUserService_login userService_login = new UserService_loginImpl();
	public static IRoomService roomService = new RoomServiceImpl();

	// 由于需要线程notify，需要保存线程的锁，所以保留这两个静态变量
	// 独立id，对应相对的任务，无论什么type的任务，id是唯一的
	public static ConcurrentHashMap<Integer, TaskUtil.DissolveRoomTask> disRoomIdMap = new ConcurrentHashMap<>(); // 解散房间的任务
	// 如果房间开局或者解散时没超过5分钟就有结果了，才会向这个集合里放数据，数据格式为id--1
	public static ConcurrentHashMap<Integer, Integer> disRoomIdResultInfo = new ConcurrentHashMap<>(); // 房间解散状态集合

	public static ExecutorService taskExecuter = Executors.newFixedThreadPool(16);

	/**
	 * 获取统一格式的返回obj
	 * 
	 * @param interfaceId
	 * @param state
	 * @param object
	 * @return
	 */
	public static JSONObject getJSONObj(Integer interfaceId, Integer state, Object object) {
		JSONObject obj = new JSONObject();
		obj.put("interfaceId", interfaceId);
		obj.put("state", state);
		obj.put("message", "");
		obj.put("info", object);
		obj.put("others", "");
		obj = getNewObj(obj);
		return obj;
	}

	// 路由转换
	public static JSONObject getNewObj(JSONObject temp) {
		Iterator<String> iterator = temp.keySet().iterator();
		JSONObject result = new JSONObject();
		while (iterator.hasNext()) {
			String str = iterator.next();
			Object o = temp.get(str);
			if (o instanceof List) {
				result.put(Cnst.ROUTE_MAP.get(str) == null ? str : Cnst.ROUTE_MAP.get(str), getNewList(o));
			} else if (o instanceof Map) {
				result.put(Cnst.ROUTE_MAP.get(str) == null ? str : Cnst.ROUTE_MAP.get(str), getNewMap(o));
			} else {
				result.put(Cnst.ROUTE_MAP.get(str) == null ? str : Cnst.ROUTE_MAP.get(str), o);
			}
		}
		return result;
	}

	public static List getNewList(Object list) {
		List<Object> temp1 = (List<Object>) list;
		List<Object> temp = new ArrayList<Object>(temp1);
		if (temp != null && temp.size() > 0) {
			for (int i = 0; i < temp.size(); i++) {
				Object o = temp.get(i);
				if (o instanceof List) {
					temp.set(i, getNewList(o));
				} else if (o instanceof Map) {// 基本上全是这个类型
					temp.set(i, getNewMap(o));
				} else {// 默认为String
					try {
						JSONObject obj = JSONObject.parseObject(o.toString());
						temp.set(i, getNewObj(obj));
					} catch (Exception e) {
						// e.printStackTrace();
					}
				}
			}
		}
		return temp;
	}

	public static Map getNewMap(Object map) {
		Map<String, Object> temp1 = (Map<String, Object>) map;
		Map<String, Object> temp = new HashMap<String, Object>(temp1);
		Map<String, Object> result = new ConcurrentHashMap<String, Object>();
		if (temp != null && temp.size() > 0) {
			Iterator<String> iterator = temp.keySet().iterator();
			while (iterator.hasNext()) {
				String str = String.valueOf(iterator.next());
				Object o = temp.get(str);
				if (o instanceof List) {
					result.put(Cnst.ROUTE_MAP.get(str) == null ? str : Cnst.ROUTE_MAP.get(str), getNewList(o));
				} else if (o instanceof Map) {
					result.put(Cnst.ROUTE_MAP.get(str) == null ? str : Cnst.ROUTE_MAP.get(str), getNewMap(o));
				} else {
					try {
						try {
							JSONObject obj = JSONObject.parseObject(o.toString());
							result.put(Cnst.ROUTE_MAP.get(str) == null ? str : Cnst.ROUTE_MAP.get(str), getNewObj(obj));
						} catch (Exception e) {
							result.put(Cnst.ROUTE_MAP.get(str) == null ? str : Cnst.ROUTE_MAP.get(str), o);
						}
					} catch (Exception e) {

					}
				}
			}
		}
		return result;
	}

	// 转换完成

	/**
	 * 房间不存在
	 * 
	 * @param interfaceId
	 * @param session
	 */
	public static void roomDoesNotExist(Integer interfaceId, IoSession session) {
		Map<String, Object> info = new HashMap<>();
		info.put("reqState", Cnst.REQ_STATE_4);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
	}

	/**
	 * 玩家在其他房间
	 * 
	 * @param interfaceId
	 * @param session
	 */
	public static void playerExistOtherRoom(Integer interfaceId, IoSession session) {
		Map<String, Object> info = new HashMap<>();
		info.put("reqState", Cnst.REQ_STATE_3);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
	}

	/**
	 * 房间已满
	 * 
	 * @param interfaceId
	 * @param session
	 */
	public static void roomFully(Integer interfaceId, IoSession session) {
		Map<String, Object> info = new HashMap<>();
		info.put("reqState", Cnst.REQ_STATE_5);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
	}

	/**
	 * 玩家房卡不足
	 * 
	 * @param interfaceId
	 * @param session
	 */
	public static void playerMoneyNotEnough(Integer interfaceId, IoSession session, Integer roomType) {
		Map<String, Object> info = new HashMap<>();
		info.put("reqState", Cnst.REQ_STATE_2);// 余额不足，请及时充值
		info.put("roomType", roomType);// 余额不足，请及时充值
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
	}

	/**
	 * 代开房间不能超过10个
	 * 
	 * @param interfaceId
	 * @param session
	 */
	public static void roomEnough(Integer interfaceId, IoSession session) {
		Map<String, Object> info = new HashMap<>();
		info.put("reqState", Cnst.REQ_STATE_11);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
	}

	/**
	 * 非法请求
	 * 
	 * @param session
	 * @param interfaceId
	 */
	public static void illegalRequest(Integer interfaceId, IoSession session) {
		Map<String, Object> info = new HashMap<>();
		JSONObject result = getJSONObj(interfaceId, 0, info);
		result.put("c", "非法请求！");
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
		session.close(true);
	}

	/**
	 * 参数错误
	 */
	public static void parameterError(Integer interfaceId, IoSession session) {
		Map<String, Object> info = new HashMap<>();
		JSONObject result = getJSONObj(interfaceId, 0, info);
		result.put("c", "参数错误！");
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
		session.close(true);
	}

	/**
	 * 敬请期待
	 * 
	 * @param interfaceId
	 * @param session
	 */
	public static void comingSoon(Integer interfaceId, IoSession session) {
		Map<String, Object> info = new HashMap<>();
		info.put("reqState", Cnst.REQ_STATE_FUYI);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
	}

	/**
	 * 游戏中，不能退出房间
	 * 
	 * @param interfaceId
	 * @param session
	 */
	public static void roomIsGaming(Integer interfaceId, IoSession session) {
		Map<String, Object> info = new HashMap<>();
		info.put("reqState", Cnst.REQ_STATE_6);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
	}

	// 像数据库添加房间信息
	public static void addRoomToDB(RoomResp room) {
		Integer cid = Integer.parseInt(room.getCid());
		Integer circle = room.getCircleNum();
		Long createId = room.getCreateId();
		Integer roomType = room.getRoomType();
		List<Long> playerIds = room.getPlayerIds();

		//FIXME
		taskExecuter.execute(new Runnable() {
			public void run() {
				// 扣除房主房卡
				if (roomType == Cnst.ROOM_TYPE_1) {
					//Integer userMoneyByUserId = userService.getUserMoneyByUserId(createId,cid);
					userService.updateMoney(userService.getUserMoneyByUserId(createId,cid) - Cnst.moneyMap_1.get(circle), createId + "",cid);
				}
				else{
					userService.updateMoney(userService.getUserMoneyByUserId(createId,cid) - Cnst.moneyMap_2.get(circle), createId + "",cid);
				}
			}
		});
		// 添加玩家消费记录
		PlayerMoneyRecord mr = new PlayerMoneyRecord();
		mr.setUserId(createId);
		if (roomType == Cnst.ROOM_TYPE_1)
			mr.setMoney(Cnst.moneyMap_1.get(circle));
		else
			mr.setMoney(Cnst.moneyMap_2.get(circle));
		mr.setType(100);
		mr.setCreateTime(new Date().getTime());

		/* 向数据库添加房间信息 */
		Map<String, String> roomSave = new HashMap<String, String>();
		for (int i = 0; i < playerIds.size(); i++) {
			roomSave.put("userId" + (i + 1), String.valueOf(playerIds.get(i)));
		}
		roomSave.put("isPlaying", "1");
		roomSave.put("roomId", room.getRoomId() + "");
		roomSave.put("createId", room.getCreateId() + "");
		roomSave.put("createTime", room.getCreateTime() + "");
		roomSave.put("roomType", room.getRoomType() + "");
		roomSave.put("circleNum", room.getCircleNum() + "");
		roomSave.put("ip", room.getIp());
		roomSave.put("XiaoJuNum", room.getXiaoJuNum() + "");
		roomSave.put("tiShi", room.getTiShi() == null ? "0" : room.getTiShi() + "");
		roomSave.put("can4Take2", room.getCan4take2() + "");
		roomSave.put("laiZi", room.getLaiZi() + "");
		roomSave.put("dingFen", room.getDiFen() + "");
		roomSave.put("mulType", room.getMulType() + "");
		roomSave.put("daPaiJiaoMan", room.getDaPaiJiaoMan() + "");
		roomSave.put("extraType", room.getExtraType() + "");
		roomSave.put("mingPaiType", room.getMingPaiType() + "");
		roomSave.put("diZhuType", room.getDizhuType() + "");
		roomSave.put("cid", room.getCid());
		taskExecuter.execute(new Runnable() {
			public void run() {
				roomService.save(roomSave);// 房间信息
				userService.insertPlayerMoneyRecord(mr);// 消费记录
			}
		});

		// 统计消费
		taskExecuter.execute(new Runnable() {
			@Override
			public void run() {
				try {
					if (roomType == 1)
						PostUtil.doCount(createId, Cnst.moneyMap_1.get(circle), roomType,room.getRoomId());
					else
						PostUtil.doCount(createId, Cnst.moneyMap_2.get(circle), roomType,room.getRoomId());
				} catch (Exception e) {
					System.out.println("调用统计借口出错");
					e.printStackTrace();
				}
			}
		});
	}
	
	// 像数据库添加房间信息
	public static void addRoomToClubDB(RoomResp room) {
		Integer clubId = room.getClubId();
		String cid = room.getCid()+"";
		ClubInfo redisClub = RedisUtil.getClubInfoByClubId(Cnst.get_REDIS_PREFIX_CLUBMAP(cid)+clubId.toString());
		if (null == redisClub) {// 如果为空 从数据库查询
			redisClub = ClubMapper.selectByClubId(StringUtils.parseInt(clubId),cid);// 根据俱乐部id查询
			// 保存到redis
			RedisUtil.setClubInfoByClubId(Cnst.get_REDIS_PREFIX_CLUBMAP(cid)+clubId.toString(), redisClub);
		}
		Long createTime = StringUtils.parseLong(room.getCreateTime());
		Long freeStart = redisClub.getFreeStart();
		Long freeEnd = redisClub.getFreeEnd();
		
		Integer roomType = room.getRoomType();
		Integer circle = room.getCircleNum();
		Integer money=0;
		if (roomType == Cnst.ROOM_TYPE_1) {//扣除俱乐部的房卡
			money= Cnst.moneyMap_1.get(circle);
		}else{
			money= Cnst.moneyMap_2.get(circle);
		}
		//更新缓存
//		boolean updateRedis=false;
		if(freeStart!=0 && freeEnd!=0){
			if(freeStart<=createTime && createTime<=freeEnd){//限免时间
				//不用扣除房卡
			}else{//扣除房卡
//				updateRedis=true;
				taskExecuter.execute(new Runnable() {
					public void run() {
						Integer money=0;
						if (roomType == Cnst.ROOM_TYPE_1) {//扣除俱乐部的房卡
							money= Cnst.moneyMap_1.get(circle);
						}else{
							money= Cnst.moneyMap_2.get(circle);
						}
						ClubMapper.updateClubMoney( clubId, money,Integer.valueOf(cid));
					}
				});
			}
		}else{//扣除房卡
//			updateRedis=true;
			taskExecuter.execute(new Runnable() {
				public void run() {
					Integer money=0;
					if (roomType == Cnst.ROOM_TYPE_1) {//扣除俱乐部的房卡
						money= Cnst.moneyMap_1.get(circle);
					}else{
						money= Cnst.moneyMap_2.get(circle);
					}
					ClubMapper.updateClubMoney( clubId, money,Integer.valueOf(cid));
				}
			});
		}
		//俱乐部缓存 减少房卡
//		if(updateRedis){
//			redisClub.setRoomCardNum(redisClub.getRoomCardNum()-money);
//			RedisUtil.setClubInfoByClubId(Cnst.get_REDIS_PREFIX_CLUBMAP(cid)+clubId.toString(), redisClub);
//		}
		Long createId = room.getCreateId();
		List<Long> playerIds = room.getPlayerIds();

		 //添加玩家消费记录  ClubUserUse
		taskExecuter.execute(new Runnable() {
			public void run() {
				Integer money=0;
				if (roomType == Cnst.ROOM_TYPE_1) {//扣除俱乐部的房卡
					money= Cnst.moneyMap_1.get(circle);
				}else{
					money= Cnst.moneyMap_2.get(circle);
				}
				ClubUserUse clubUserUse = new ClubUserUse();
				clubUserUse.setClubId(room.getClubId());
				clubUserUse.setCid(Integer.valueOf(cid));
				clubUserUse.setRoomId(room.getRoomId());
				clubUserUse.setMoney(money/4);
				clubUserUse.setCreateTime(System.currentTimeMillis());
				for (Long long1 : playerIds) {
					clubUserUse.setUserId(long1);
					ClubMapper.saveUserUse(clubUserUse);
				}
			}
		});
		/* 向数据库添加房间信息 */
		HashMap<String, String> roomSave = new HashMap<String, String>();
		for (int i = 0; i < playerIds.size(); i++) {
			roomSave.put("userId" + (i + 1), String.valueOf(playerIds.get(i)));
		}
		roomSave.put("isPlaying", "1");
		roomSave.put("roomId", room.getRoomId() + "");
		roomSave.put("createId", room.getCreateId() + "");
		roomSave.put("createTime", room.getCreateTime() + "");
		roomSave.put("roomType", room.getRoomType() + "");
		roomSave.put("circleNum", room.getCircleNum() + "");
		roomSave.put("ip", room.getIp());
		roomSave.put("XiaoJuNum", room.getXiaoJuNum() + "");
		roomSave.put("tiShi", room.getTiShi() == null ? "0" : room.getTiShi() + "");
		roomSave.put("can4Take2", room.getCan4take2() + "");
		roomSave.put("laiZi", room.getLaiZi() + "");
		roomSave.put("dingFen", room.getDiFen() + "");
		roomSave.put("mulType", room.getMulType() + "");
		roomSave.put("daPaiJiaoMan", room.getDaPaiJiaoMan() + "");
		roomSave.put("extraType", room.getExtraType() + "");
		roomSave.put("mingPaiType", room.getMingPaiType() + "");
		roomSave.put("diZhuType", room.getDizhuType() + "");
		roomSave.put("cid", room.getCid());
		roomSave.put("clubId", clubId+"");
		taskExecuter.execute(new Runnable() {
			public void run() {
				ClubMapper.saveRoom(roomSave);;// 房间信息
			}
		});

		// 统计消费
		taskExecuter.execute(new Runnable() {
			@Override
			public void run() {
				try {
					if (roomType == 1)
						PostUtil.doCount(createId, Cnst.moneyMap_1.get(circle), room.getExtraType(),room.getRoomId());
					else
						PostUtil.doCount(createId, Cnst.moneyMap_2.get(circle), room.getExtraType(),room.getRoomId());
				} catch (Exception e) {
					System.out.println("调用统计借口出错");
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * 向数据库添加玩家分数信息
	 */
	public static void updateDatabasePlayRecord(RoomResp room, String cid) {
		if (room == null)
			return;
		//俱乐部房间
		if(String.valueOf(room.getRoomId()).length()==7){
			updateClubDatabasePlayRecord(room,cid);
			return ;
		}
		// 刷新数据库
		taskExecuter.execute(new Runnable() {
			public void run() {
				roomService.updateRoomState(room.getRoomId().intValue(), room.getXiaoJuNum());
			}
		});

		// 判断totalNum 在小结算时+1
		Integer totalNum = room.getTotalNum();
		Integer roomType = room.getRoomType();
		Map<String, String> roomSave = new HashMap<String, String>();
		if (room.getState().intValue() != Cnst.ROOM_STATE_CREATED) {
			List<Long> playerIds = room.getPlayerIds();

			roomSave.put("endTime", String.valueOf(System.currentTimeMillis()));
			List<Player> players = RedisUtil.getPlayerList(room, cid);

			List<Map> redisRecord = new ArrayList<Map>();
			for (Player p : players) {
				if (p == null)
					continue;
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("userId", p.getUserId());
				map.put("finalScore", p.getScore());
				map.put("position", p.getPosition());
				redisRecord.add(map);
			}
			String score = null;
			String userId1 = null;
			if (playerIds.get(0) != null) {
				userId1 = String.valueOf(playerIds.get(0));

				score = String.valueOf(players.get(0).getScore());
				roomSave.put("firstUserId", userId1);
				roomSave.put("firstUserName", players.get(0).getUserName());
				roomSave.put("firstUserImg", players.get(0).getUserImg());
				roomSave.put("firstUserMoneyRecord", score);
				roomSave.put("firstUserMoneyRemain", score);
			} else {
				roomSave.put("firstUserId", null);
				roomSave.put("firstUserName", null);
				roomSave.put("firstUserImg", null);
				roomSave.put("firstUserMoneyRecord", "0");
				roomSave.put("firstUserMoneyRemain", "0");
			}
			String userId2 = null;
			if (playerIds.get(1) != null) {
				userId2 = String.valueOf(playerIds.get(1));
				score = String.valueOf(players.get(1).getScore());
				roomSave.put("secondUserId", userId2);
				roomSave.put("secondUserName", players.get(1).getUserName());
				roomSave.put("secondUserImg", players.get(1).getUserImg());
				roomSave.put("secondUserMoneyRecord", score);
				roomSave.put("secondUserMoneyRemain", score);
			} else {
				roomSave.put("secondUserId", null);
				roomSave.put("secondUserName", null);
				roomSave.put("secondUserImg", null);
				roomSave.put("secondUserMoneyRecord", "0");
				roomSave.put("secondUserMoneyRemain", "0");

			}
			String userId3 = null;
			if (playerIds.get(2) != null) {
				userId3 = String.valueOf(playerIds.get(2));
				score = String.valueOf(players.get(2).getScore());
				roomSave.put("thirdUserId", userId3);
				roomSave.put("thirdUserName", players.get(2).getUserName());
				roomSave.put("thirdUserImg", players.get(2).getUserImg());
				roomSave.put("thirdUserMoneyRecord", score);
				roomSave.put("thirdUserMoneyRemain", score);
			} else {
				roomSave.put("thirdUserId", null);
				roomSave.put("thirdUserName", null);
				roomSave.put("thirdUserImg", null);
				roomSave.put("thirdUserMoneyRecord", "0");
				roomSave.put("thirdUserMoneyRemain", "0");
			}
			roomSave.put("roomId", room.getRoomId() + "");
			roomSave.put("createTime", room.getCreateTime());
			roomSave.put("endTime", System.currentTimeMillis() + "");
			roomSave.put("circleNum", room.getCircleNum() + "");
			roomSave.put("lastNum", room.getLastNum() + "");
			roomSave.put("state", room.getState() + "");
			roomSave.put("tiShi", room.getTiShi() == null ? "0" : room.getTiShi() + "");
			roomSave.put("XiaoJuNum", room.getXiaoJuNum() + "");
			roomSave.put("roomType", room.getRoomType() + "");
			roomSave.put("can4Take2", room.getCan4take2() + "");
			roomSave.put("laiZi", room.getLaiZi() + "");
			roomSave.put("dingFen", room.getDingFen() + "");
			roomSave.put("mulType", room.getMulType() + "");
			roomSave.put("daPaiJiaoMan", room.getDaPaiJiaoMan() + "");

			String fileName = new StringBuffer().append("http://").append(room.getIp()).append(":8086/").append(Cnst.BACK_FILE_PATH).toString();

			roomSave.put("backUrl", fileName);
			roomSave.put("extraType", room.getExtraType() + "");
			roomSave.put("xiaoJuInfo", JSONObject.toJSONString(room.getXiaoJuFen()));

			roomSave.put("mingPaiType", room.getMingPaiType() + "");
			roomSave.put("diZhuType", room.getDizhuType() + "");
			// 更新redis 缓存
			String key = room.getRoomId() + "-" + room.getCreateTime();

			RedisUtil.hmset(Cnst.get_REDIS_PLAY_RECORD_PREFIX(cid).concat(key), roomSave, Cnst.PLAYOVER_LIFEE_TIME);
			haveRedisRecord(userId1, key, cid);
			haveRedisRecord(userId2, key, cid);
			haveRedisRecord(userId3, key, cid);
			if (room.getExtraType() != null && room.getExtraType().intValue() == Cnst.ROOM_EXTRA_TYPE_2) {
				// 代开模式
				String key1 = Cnst.get_REDIS_PLAY_RECORD_PREFIX_ROE_DAIKAI(cid).concat(room.getCreateId() + "");
				RedisUtil.lpush(key1, null, key);
			}
			// setOverInfo 信息 大结算时 调用
			RedisUtil.setObject(Cnst.get_REDIS_PLAY_RECORD_PREFIX_OVERINFO(cid).concat(key), redisRecord, Cnst.OVERINFO_LIFE_TIME_COMMON);
		} else {
			return;
		}

		taskExecuter.execute(new Runnable() {
			public void run() {
				userService.insertPlayRecord(roomSave);
			}
		});
	}

	/**
	 * 俱乐部数据库
	 */
	public static void updateClubDatabasePlayRecord(RoomResp room, String cid) {
		if (room == null)
			return;
		// 刷新数据库
		taskExecuter.execute(new Runnable() {
			public void run() {
				ClubMapper.updateRoomState(room.getRoomId().intValue(), room.getXiaoJuNum());
			}
		});

		// 判断totalNum 在小结算时+1
//		Integer totalNum = room.getTotalNum();
//		Integer roomType = room.getRoomType();
		Map<String, String> roomSave = new HashMap<String, String>();
		Map<String, String> clubRoom = new HashMap<String, String>();
		if (room.getState().intValue() != Cnst.ROOM_STATE_CREATED) {
			List<Long> playerIds = room.getPlayerIds();

			roomSave.put("endTime", String.valueOf(System.currentTimeMillis()));
			List<Player> players = RedisUtil.getPlayerList(room.getRoomId(), cid);

			List<Map> redisRecord = new ArrayList<Map>();
			for (Player p : players) {
				if (p == null)
					continue;
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("userId", p.getUserId());
				map.put("finalScore", p.getScore());
				map.put("position", p.getPosition());
				redisRecord.add(map);
				todayPlayerScoreForRedis(cid,room.getClubId(),p.getUserId(),p.getScore().intValue());
			}
			clubRoom.put("roomSn", room.getRoomId()+"");
			clubRoom.put("createTime", room.getCreateTime());
			String score = null;
			String userId1 = null;
			if (playerIds.get(0) != null) {
				userId1 = String.valueOf(playerIds.get(0));

				score = String.valueOf(players.get(0).getScore());
				roomSave.put("firstUserId", userId1);
				roomSave.put("firstUserName", players.get(0).getUserName());
				roomSave.put("firstUserImg", players.get(0).getUserImg());
				roomSave.put("firstUserMoneyRecord", score);
				roomSave.put("firstUserMoneyRemain", score);
				
//				clubRoom.put("firstUserId", userId1);
				clubRoom.put("firstUserName", players.get(0).getUserName());
				clubRoom.put("firstUserMoneyRecord", score);
//				clubRoom.put("fp", players.get(0).getPosition()+"");
			} else {
				roomSave.put("firstUserId", null);
				roomSave.put("firstUserName", null);
				roomSave.put("firstUserImg", null);
				roomSave.put("firstUserMoneyRecord", "0");
				roomSave.put("firstUserMoneyRemain", "0");
			}
			String userId2 = null;
			if (playerIds.get(1) != null) {
				userId2 = String.valueOf(playerIds.get(1));
				score = String.valueOf(players.get(1).getScore());
				roomSave.put("secondUserId", userId2);
				roomSave.put("secondUserName", players.get(1).getUserName());
				roomSave.put("secondUserImg", players.get(1).getUserImg());
				roomSave.put("secondUserMoneyRecord", score);
				roomSave.put("secondUserMoneyRemain", score);
				
//				clubRoom.put("secondUserId", userId2);
				clubRoom.put("secondUserName", players.get(1).getUserName());
				clubRoom.put("secondUserMoneyRecord", score);
//				clubRoom.put("sp", players.get(1).getPosition()+"");
			} else {
				roomSave.put("secondUserId", null);
				roomSave.put("secondUserName", null);
				roomSave.put("secondUserImg", null);
				roomSave.put("secondUserMoneyRecord", "0");
				roomSave.put("secondUserMoneyRemain", "0");

			}
			String userId3 = null;
			if (playerIds.get(2) != null) {
				userId3 = String.valueOf(playerIds.get(2));
				score = String.valueOf(players.get(2).getScore());
				roomSave.put("thirdUserId", userId3);
				roomSave.put("thirdUserName", players.get(2).getUserName());
				roomSave.put("thirdUserImg", players.get(2).getUserImg());
				roomSave.put("thirdUserMoneyRecord", score);
				roomSave.put("thirdUserMoneyRemain", score);
				
//				clubRoom.put("thirdUserId", userId3);
				clubRoom.put("thirdUserName", players.get(2).getUserName());
				clubRoom.put("thirdUserMoneyRecord", score);
//				clubRoom.put("tp", players.get(2).getPosition()+"");
			} else {
				roomSave.put("thirdUserId", null);
				roomSave.put("thirdUserName", null);
				roomSave.put("thirdUserImg", null);
				roomSave.put("thirdUserMoneyRecord", "0");
				roomSave.put("thirdUserMoneyRemain", "0");
			}
			roomSave.put("roomId", room.getRoomId() + "");
			//俱乐部的id
			roomSave.put("clubId", room.getClubId()+"");
			roomSave.put("createTime", room.getCreateTime());
			roomSave.put("endTime", System.currentTimeMillis() + "");
			roomSave.put("circleNum", room.getCircleNum() + "");
			roomSave.put("lastNum", room.getLastNum() + "");
			roomSave.put("state", room.getState() + "");
			roomSave.put("tiShi", room.getTiShi() == null ? "0" : room.getTiShi() + "");
			roomSave.put("XiaoJuNum", room.getXiaoJuNum() + "");
			roomSave.put("roomType", room.getRoomType() + "");
			roomSave.put("can4Take2", room.getCan4take2() + "");
			roomSave.put("laiZi", room.getLaiZi() + "");
			roomSave.put("dingFen", room.getDingFen() + "");
			roomSave.put("mulType", room.getMulType() + "");
			roomSave.put("daPaiJiaoMan", room.getDaPaiJiaoMan() + "");

			String fileName = new StringBuffer().append("http://").append(room.getIp()).append(":8086/").append(Cnst.BACK_FILE_PATH).toString();

			roomSave.put("backUrl", fileName);
			//只可能是1
			roomSave.put("extraType",1 + "");
			roomSave.put("xiaoJuInfo", JSONObject.toJSONString(room.getXiaoJuFen()));
			roomSave.put("mingPaiType", room.getMingPaiType() + "");
			roomSave.put("diZhuType", room.getDizhuType() + "");
			
			// 更新redis 缓存
			String key = room.getRoomId() + "_" + room.getCreateTime();
			String redisDate = StringUtils.toString(StringUtils.getTimesmorning());
			String userKey = Cnst.get_REDIS_CLUB_PLAY_RECORD_PREFIX_ROE_USER(cid) + room.getClubId();

			RedisUtil.hmset(Cnst.get_REDIS_CLUB_PLAY_RECORD_PREFIX(cid).concat(key), clubRoom, Cnst.PLAYOVER_LIFEE_TIME);
			haveClubRedisRecord(userKey,userId1, key, redisDate);
			haveClubRedisRecord(userKey,userId2, key, redisDate);
			haveClubRedisRecord(userKey,userId3, key, redisDate);
			
			
			// setOverInfo 信息 大结算时 调用
			RedisUtil.setObject(Cnst.get_REDIS_PLAY_RECORD_PREFIX_OVERINFO(cid).concat(key), redisRecord, Cnst.OVERINFO_LIFE_TIME_COMMON);
		} else {
			return;
		}

		taskExecuter.execute(new Runnable() {
			public void run() {
				ClubMapper.insertPlayRecord(roomSave);
			}
		});
	}
	/**
	 * 更新玩家的分数
	 * @param cid
	 * @param clubId
	 * @param playerId
	 * @param score
	 */
	private static void todayPlayerScoreForRedis(String cid, Integer clubId,Long playerId,int score) {
		//更新每个玩家每天的分数
		String key = Cnst.REDIS_CLUB_TODAYSCORE_ROE_USER+cid+"_"+clubId+"_"+playerId+"_"+StringUtils.getTimesmorning();
		if(RedisUtil.exists(key)){
			Integer oldSocre = RedisUtil.getObject(key,Integer.class);
			RedisUtil.setObject(key,String.valueOf(oldSocre+score),null);
		}else{
			int dieTime=Cnst.REDIS_CLUB_DIE_TIME;
//			Long dieTime=StringUtils.getTimesNight()-System.currentTimeMillis();
			RedisUtil.setObject(key,String.valueOf(score),dieTime);
		}
	}
	
	
	public static void haveRedisRecord(String userId, String value, String cid) {
		if (userId == null)
			return;
		String key = Cnst.get_REDIS_PLAY_RECORD_PREFIX_ROE_USER(cid).concat(userId);
		RedisUtil.lpush(key, null, value);
	}
	/*
	 * key:uid+cid ; value:roomId+创建时间
	 */
	public static void haveClubRedisRecord(String qianZui, String userId, String value, String redisDate) {
		if (userId == null)
			return;
		String key = qianZui+"_"+userId+"_"+redisDate;
		RedisUtil.lpush(key, null, value);
	}

	/**
	 * 开启等待解散房间任务
	 * 
	 * @param roomId
	 * @param type
	 */
	public static void startDisRoomTask(int roomId, int type, String cid) {
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId), cid);
		Integer createDisId = null;
		while (true) {
			createDisId = CommonUtil.getGivenRamdonNum(8);
			if (!disRoomIdMap.containsKey(createDisId)) {
				break;
			}
		}
		if (type == Cnst.DIS_ROOM_TYPE_1) {
			room.setCreateDisId(createDisId);
		} else if (type == Cnst.DIS_ROOM_TYPE_2) {
			room.setApplyDisId(createDisId);
		}
		TaskUtil.DissolveRoomTask task = new TaskUtil().new DissolveRoomTask(roomId, type, cid);
		disRoomIdMap.put(createDisId, task);
		RedisUtil.updateRedisData(room, null, cid);
		new Thread(task).start();
	}

	/**
	 * 关闭解散房间任务
	 * 
	 * @param roomId
	 * @param type
	 */
	public static void notifyDisRoomTask(RoomResp room, int type) {
		if (room == null) {
			return;
		}
		Integer taskId = 0;
		if (type == Cnst.DIS_ROOM_TYPE_1) {
			taskId = room.getCreateDisId();
			room.setCreateDisId(null);
		} else if (type == Cnst.DIS_ROOM_TYPE_2) {
			taskId = room.getApplyDisId();
			room.setApplyDisId(null);
		}
		if (taskId == null) {
			return;
		}
		TaskUtil.DissolveRoomTask task = disRoomIdMap.get(taskId);
		disRoomIdResultInfo.put(taskId, Cnst.DIS_ROOM_RESULT);
		if (task != null) {
			// if (type==Cnst.DIS_ROOM_TYPE_1) {
			//
			// //首先向数据库添加房间记录
			// addRoomToDB(room);
			//
			//
			// }
			synchronized (task) {
				task.notify();
			}
		}
	}

// 	private static void addupdateDatabasePlayRecord(RoomResp room) {
// 		// 刷新数据库
//  		taskExecuter.execute(new Runnable() {		
//			public void run() {
//				ClubMapper.updateRoomState(room.getRoomId(),room.getXiaoJuNum());
//			}
//		});
//  		
//  		//判断totalNum 在小结算时+1
//  		Map<String,String> roomSave = new HashMap<String,String>();
//  		if(room.getXiaoJuNum() != null && room.getXiaoJuNum() != 0){  			
//  			List<Player> players = RedisUtil.getPlayerList(room.getRoomId(),cid);  			
//  			List<Map> redisRecord = new ArrayList<Map>();
//  			for(Player p:players){
//  				Map<String,Object> map = new HashMap<String, Object>();
//  				map.put("userId", p.getUserId());
//  				map.put("finalScore", p.getScore());
//  				map.put("position", p.getPosition());
//  				map.put("userName", p.getUserName());
//  				map.put("userImg", p.getUserImg());
//  				redisRecord.add(map);
//  			}
//  			//setOverInfo 信息 大结算时 调用
//  			String key = room.getRoomId()+"_"+room.getCreateTime();
//  			RedisUtil.setObject(Cnst.REDIS_PLAY_RECORD_PREFIX_OVERINFO.concat(key), redisRecord,Cnst.OVERINFO_LIFE_TIME_COMMON );
//  			List<Map<String,Object>> userInfo = new ArrayList<Map<String,Object>>();
//  			for(Player p:players){
//  				Map<String,Object> map = new HashMap<String, Object>();
//  				map.put("userId", p.getUserId());
//  				map.put("userName", p.getUserId());
//  				map.put("finalScore", p.getScore());
//  				userInfo.add(map);
//  			}
//  			roomSave.put("userInfo", JSONObject.toJSONString(userInfo));
//  			roomSave.put("roomId", String.valueOf(room.getRoomId()));
//  			roomSave.put("createTime", room.getCreateTime());
//
//  			//战绩
//  			
//  			//更新redis 缓存
//  			RedisUtil.hmset(Cnst.REDIS_CLUB_PLAY_RECORD_PREFIX.concat(key), roomSave, Cnst.PLAYOVER_LIFE_TIME);
//  			for(Player p:players){
//  				haveClubRedisRecord(room.getClubId(),p.getUserId(),key,p.getScore());
//  			} 
//  		}else{
//  			return;
//  		}
//	}
}