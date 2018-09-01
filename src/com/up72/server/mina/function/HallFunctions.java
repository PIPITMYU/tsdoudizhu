package com.up72.server.mina.function;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.core.session.IoSession;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Feedback;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.game.model.SystemMessage;
import com.up72.server.mina.bean.ProtocolData;
import com.up72.server.mina.main.MinaServerManager;
import com.up72.server.mina.utils.BackFileUtil;
import com.up72.server.mina.utils.CommonUtil;
import com.up72.server.mina.utils.StringUtils;
import com.up72.server.mina.utils.redis.RedisUtil;

/**
 * Created by Administrator on 2017/7/8. 大厅方法类
 */
public class HallFunctions extends TCPGameFunctions {

	public static ConcurrentHashMap<Integer, Long> roomIdMap = new ConcurrentHashMap<Integer, Long>(); // 房间信息

	/**
	 * 大厅查询战绩
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100002(IoSession session, Map<String, Object> readData) {
		logger.I("大厅查询战绩,interfaceId -> 100002");
		Integer interfaceId = Integer.parseInt(String.valueOf(readData.get("interfaceId")));
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		// 玩家UID
		String userId = String.valueOf(readData.get("userId"));
		// 页数
		Integer page = Integer.parseInt(String.valueOf(readData.get("page")));
		String userKey = Cnst.get_REDIS_PLAY_RECORD_PREFIX_ROE_USER(cid).concat(userId);

		// Redis里面是一个userKey的List List的长度就是页数的内容长度,但是每页10行
		Long pageSize = RedisUtil.llen(userKey);
		// 计算出page页的开始和结束索引
		int start = (page - 1) * Cnst.PAGE_SIZE;
		int end = start + Cnst.PAGE_SIZE;
		List<String> keys = RedisUtil.lrange(userKey, start, end);
		JSONObject info = new JSONObject();
		List<Map<String, String>> maps = new ArrayList<Map<String, String>>();
		for (String roomKey : keys) {
			// 获取房间的数据
			if (RedisUtil.exists(Cnst.get_REDIS_PLAY_RECORD_PREFIX(cid).concat(roomKey))) {
				Map<String, String> roomInfos = RedisUtil.hgetAll(Cnst.get_REDIS_PLAY_RECORD_PREFIX(cid).concat(roomKey));
				maps.add(roomInfos);
			}
		}
		info.put("infos", maps);
		info.put("pages", pageSize == null ? 0 : pageSize % Cnst.PAGE_SIZE == 0 ? pageSize / Cnst.PAGE_SIZE : (pageSize / Cnst.PAGE_SIZE + 1));
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
	}

	/**
	 * 大厅查询系统消息
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100003(IoSession session, Map<String, Object> readData) {
		logger.I("大厅查询系统消息,interfaceId -> 100003");
		Integer interfaceId = Integer.parseInt(String.valueOf(readData.get("interfaceId")));
		Integer page = Integer.parseInt(String.valueOf(readData.get("page")));
		List<SystemMessage> info = userService.getSystemMessage(null, (page - 1) * Cnst.PAGE_SIZE, Cnst.PAGE_SIZE);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
	}

	/**
	 * 大厅请求联系我们
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100004(IoSession session, Map<String, Object> readData) {
		logger.I("大厅请求联系我们,interfaceId -> 100004");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Map<String, String> info = new HashMap<>();
		info.put("connectionInfo", userService.getConectUs());
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
	}

	/**
	 * 大厅请求帮助信息
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100005(IoSession session, Map<String, Object> readData) {
		logger.I("大厅请求帮助信息,interfaceId -> 100005");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Map<String, String> info = new HashMap<>();
		info.put("help", "帮助帮助帮助帮助帮助帮助帮助帮助帮助");
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
	}

	/**
	 * 反馈信息
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100006(IoSession session, Map<String, Object> readData) {
		logger.I("反馈信息,interfaceId -> 100006");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		String content = String.valueOf(readData.get("content"));
		String tel = String.valueOf(readData.get("tel"));
		// 插入反馈信息
		Feedback back = new Feedback();
		back.setContent(content);
		back.setCreateTime(new Date().getTime());
		back.setTel(tel);
		back.setUserId(userId);
		userService.userFeedback(back);
		// 返回反馈信息
		Map<String, String> info = new HashMap<>();
		info.put("content", "感谢您的反馈！");
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
	}

	/**
	 * 创建房间-经典玩法
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100007(IoSession session, Map<String, Object> readData) {
		logger.I("创建房间,interfaceId -> 100007");

		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		// 局数
		Integer circleNum = StringUtils.parseInt(readData.get("circleNum"));
		// 房间类型 ROOM_TYPE_1 经典 ROOM_TYPE_2 闪斗
		Integer roomType = StringUtils.parseInt(readData.get("roomType"));
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		// 积分
		if (roomType == null || (roomType != Cnst.ROOM_TYPE_2 && roomType != Cnst.ROOM_TYPE_1)) {
			illegalRequest(interfaceId, session);
			return;
		}

		Integer extraType = StringUtils.parseInt(readData.get("extraType"));

		if (extraType == null || extraType.intValue() != Cnst.ROOM_EXTRA_TYPE_2)
			extraType = Cnst.ROOM_EXTRA_TYPE_1;

		Integer mingPaiType = StringUtils.parseInt(readData.get("mingPaiType"));
		Integer diZhuType = StringUtils.parseInt(readData.get("diZhuType"));

		if (mingPaiType != Cnst.MINGPAY_TYPE_1 && mingPaiType != Cnst.MINGPAY_TYPE_2) {
			illegalRequest(interfaceId, session);
			return;
		}

		if (diZhuType != Cnst.DIZHU_TYPE_2 && diZhuType != Cnst.DIZHU_TYPE_3) {
			illegalRequest(interfaceId, session);
			return;
		}
		if (extraType.equals(Cnst.ROOM_EXTRA_TYPE_2)) {
			int num = getDaiKaiNum(userId, cid);

			if (num >= 10) {
				roomEnough(interfaceId, session);
				return;
			}
		}

		// 是否可以4带2
		Integer tmpCan4take2 = StringUtils.parseInt(readData.get("can4Take2"));
		Integer can4take2 = tmpCan4take2.intValue() == 0 ? 0 : 1;

		// 是否可以是癞子 如果是经典斗 不考虑这个选项

		Integer laizi = 0;
		if (roomType.intValue() == Cnst.ROOM_TYPE_2) {
			Integer tmplaizi = StringUtils.parseInt(readData.get("isLaiZi"));
			laizi = tmplaizi.intValue() == 0 ? 0 : 1;
		}

		// 加倍规则1不加倍 2农民优先 3都可以加倍
		Integer mul = StringUtils.parseInt(readData.get("mul"));

		if (mul.intValue() != Cnst.ROOM_MUL_TYPE_2 && mul.intValue() != Cnst.ROOM_MUL_TYPE_3 && mul.intValue() != Cnst.ROOM_MUL_TYPE_1) {
			illegalRequest(interfaceId, session);
			return;
		}

		Player p = RedisUtil.getPlayerByUserId(String.valueOf(session.getAttribute(Cnst.USER_SESSION_USER_ID)), cid);

		int money = 0;
		if (roomType.intValue() == Cnst.ROOM_TYPE_1) {
			money = Cnst.moneyMap_1.get(circleNum);
			if (p.getMoney() < money) {// 玩家房卡不足
				playerMoneyNotEnough(interfaceId, session, roomType);
				return;
			}

		} else {
			money = Cnst.moneyMap_2.get(circleNum);
			if (p.getMoney() < money) {// 玩家房卡不足
				playerMoneyNotEnough(interfaceId, session, roomType);
				return;
			}
		}

		// 封顶分数
		Integer dingFeng = StringUtils.parseInt(readData.get("dingFen"));

		if (dingFeng == null) {
			illegalRequest(interfaceId, session);
			return;
		}

		if (dingFeng.intValue() != Cnst.ROOM_MAX_SCORE_2 && dingFeng.intValue() != Cnst.ROOM_MAX_SCORE_3 && dingFeng.intValue() != Cnst.ROOM_MAX_SCORE_4 && dingFeng.intValue() != Cnst.ROOM_MAX_SCORE_5 && dingFeng.intValue() != Cnst.ROOM_MAX_SCORE_1) {
			illegalRequest(interfaceId, session);
			return;
		}

		if (p.getRoomId() != null) {// 已存在其他房间
			playerExistOtherRoom(interfaceId, session);
			return;
		}

		if (extraType.intValue() == Cnst.ROOM_EXTRA_TYPE_2) {// 代开开房，玩家房卡必须大于等于100
			if (p.getMoney() < 100) {
				playerMoneyNotEnough(interfaceId, session, roomType);
				return;
			}
		}

		Integer daPaiJiaoMan = StringUtils.parseInt(readData.get("daPaiJiaoMan"));

		if (daPaiJiaoMan.intValue() != Cnst.ROOM_JIAO_TYPE_1 && daPaiJiaoMan.intValue() != Cnst.ROOM_JIAO_TYPE_2) {
			illegalRequest(interfaceId, session);
			return;
		}
		RoomResp room = new RoomResp();

		room.setMingPaiType(mingPaiType);
		room.setDizhuType(diZhuType);

		long now = System.currentTimeMillis();
		while (true) {
			room.setRoomId(CommonUtil.getGivenRamdonNum(6));// 设置随机房间密码

			if (roomIdMap.containsKey(room.getRoomId()))
				continue;// 有个正在用
			roomIdMap.put(room.getRoomId(), now);

			Long long1 = roomIdMap.get(room.getRoomId());
			if (long1 == null || long1.longValue() != now)
				continue;
			// 成功放入了map里面

			// 已经存在这个房间
			if (RedisUtil.exists(Cnst.get_REDIS_PREFIX_ROOMMAP(cid).concat(room.getRoomId() + "")))
				continue;

			break;
		}

		String createTime = String.valueOf(new Date().getTime());
		room.setCreateId(userId);
		room.setState(Cnst.ROOM_STATE_CREATED);
		room.setCircleNum(circleNum);
		room.setTotalNum(0);
		room.setLastNum(circleNum);
		room.setRoomType(roomType);
		room.setCreateTime(createTime);
		room.setOpenName(p.getUserName());
		room.setDiFen(1);
		room.setCan4take2(can4take2);
		room.setLaiZi(laizi);
		room.setMulType(mul);
		room.setDingFen(dingFeng);
		room.setDaPaiJiaoMan(daPaiJiaoMan);
		room.setExtraType(extraType);
		room.setCid(cid);
		// 初始化大接口的id
		room.setWsw_sole_action_id(1);
		room.setWsw_sole_main_id(1);

		List<Long> userIds = new ArrayList<Long>();

		Map<String, Object> info = new HashMap<>();
		Map<String, Object> userInfos = new HashMap<String, Object>();

		if (extraType.intValue() != Cnst.ROOM_EXTRA_TYPE_2) {
			userIds.add(userId);
			p.setRoomId(room.getRoomId());
			// 处理开房模式

			// 设置用户信息
			p.setPosition(1);// 标识位置 不存在东西南北
			p.setPlayStatus(Cnst.PLAYER_STATE_IN);// 进入房间状态

			p.setJoinIndex(1);

			// 初始化 用户 初始给0积分
			p.initPlayer(p.getRoomId(), Cnst.PLAYER_STATE_IN, 0l);

		}
		room.setPlayerIds(userIds);

		info.put("reqState", Cnst.REQ_STATE_1);
		// 扣除房卡
		p.setMoney(p.getMoney() - money);
		userInfos.put("money", p.getMoney());
		userInfos.put("playStatus", String.valueOf(Cnst.PLAYER_STATE_IN));

		room.setIp(Cnst.SERVER_IP);
		// 直接把传来的readData处理 返回
		readData.put("roomSn", room.getRoomId());
		info.put("userInfos", userInfos);
		readData.put("state", room.getState());
		readData.put("userId", userId);
		readData.put("lastNum", room.getLastNum());
		readData.put("totalNum", room.getTotalNum());
		readData.put("roomType", room.getRoomType());
		readData.put("can4Take2", room.getCan4take2());
		readData.put("extraType", room.getExtraType());
		if (room.getRoomType() == Cnst.ROOM_TYPE_2)
			readData.put("isLaiZi", room.getLaiZi());
		readData.put("dingFen", room.getDingFen());
		readData.put("daPaiJiaoMan", room.getDaPaiJiaoMan());

		readData.put("mingPaiType", room.getMingPaiType() + "");
		readData.put("diZhuType", room.getDizhuType() + "");

		readData.remove("interfaceId");

		// toEdit 需要去数据库匹配，查看房间号是否存在，如果存在，则重新生成
		RedisUtil.setObject(Cnst.get_REDIS_PREFIX_ROOMMAP(cid).concat(room.getRoomId() + ""), room, Cnst.ROOM_LIFE_TIME_CREAT);

		roomIdMap.remove(room.getRoomId());

		// 更新redis数据 player roomMap
		RedisUtil.updateRedisData(null, p, cid);

		if (extraType.intValue() == Cnst.ROOM_EXTRA_TYPE_2) {
			RedisUtil.hset(Cnst.get_ROOM_DAIKAI_KEY(cid).concat(session.getAttribute(Cnst.USER_SESSION_USER_ID) + ""), room.getRoomId() + "", "1", Cnst.DAI_KAI_RECORD_EXPIRE);
		}

		// 解散房间超时任务开启
		startDisRoomTask(room.getRoomId(), Cnst.DIS_ROOM_TYPE_1, cid);

		info.put("roomInfos", readData);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
	}

	/**
	 * 加入房间
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100008(IoSession session, Map<String, Object> readData) {
		logger.I("加入房间,interfaceId -> 100008");

		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomSn"));

		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);

		Player p = RedisUtil.getPlayerByUserId(String.valueOf(session.getAttribute(Cnst.USER_SESSION_USER_ID)), cid);

		// 已经在其他房间里
		if (p.getRoomId() != null) {// 玩家已经在非当前请求进入的其他房间里
			playerExistOtherRoom(interfaceId, session);
			return;
		}
		// 房间不存在
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId), cid);
		if (room == null || room.getState() == Cnst.ROOM_STATE_YJS) {
			roomDoesNotExist(interfaceId, session);
			return;
		}

		List<Long> playerIds = room.getPlayerIds();

		boolean haveNull = false;
		int joinIdx = 0;
		for (Long long1 : playerIds) {
			if (long1 == null) {
				haveNull = true;
			} else
				++joinIdx;
		}
		// 房间人满
		if (!haveNull && playerIds.size() > 2) {
			roomFully(interfaceId, session);
			return;
		}

		// 验证ip是否一致
		if (!Cnst.SERVER_IP.equals(room.getIp())) {
			Map<String, Object> info = new HashMap<>();
			info.put("reqState", Cnst.REQ_STATE_14);
			info.put("roomSn", roomId);
			info.put("roomIp", room.getIp().concat(":").concat(Cnst.MINA_PORT));
			JSONObject result = getJSONObj(interfaceId, 1, info);
			ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
			session.write(pd);
			return;
		}

		// 设置用户信息
		// p.setPlayStatus(Cnst.PLAYER_STATE_PREPARED);// 准备状态
		p.setRoomId(roomId);
		p.setJoinIndex(joinIdx + 1);// 用户加入顺序
		if (haveNull) {
			for (int i = 0; i < playerIds.size(); i++) {
				if (playerIds.get(i) == null) {
					playerIds.set(i, p.getUserId());
					p.setPosition(i + 1);
					break;
				}
			}
		} else {
			playerIds.add(userId);
			p.setPosition(playerIds.size()); // 用户实际的位置
		}
		// 初始化用户
		p.initPlayer(p.getRoomId(), Cnst.PLAYER_STATE_IN, 0l);

		JSONArray players = new JSONArray();

		// 更新redis数据
		RedisUtil.updateRedisData(room, p, cid);

		// 通知另外几个人
		for (Long ids : playerIds) {
			if (ids == null) {
				continue;
			}
			if (ids.equals(userId)) {
				continue;
			}
			{
				// 其他人的信息
				Player otherPlayer = RedisUtil.getPlayerByUserId(String.valueOf(ids), cid);

				JSONObject otherInfo = new JSONObject();
				otherInfo.put("userId", otherPlayer.getUserId());
				otherInfo.put("score", otherPlayer.getScore());
				otherInfo.put("position", otherPlayer.getPosition());
				otherInfo.put("money", otherPlayer.getMoney());
				otherInfo.put("playStatus", otherPlayer.getPlayStatus());
				otherInfo.put("userName", otherPlayer.getUserName());
				otherInfo.put("userImg", otherPlayer.getUserImg());
				otherInfo.put("gender", otherPlayer.getGender());
				otherInfo.put("ip", otherPlayer.getIp());
				otherInfo.put("joinIndex", otherPlayer.getJoinIndex());
				otherInfo.put("gender", otherPlayer.getGender());
				players.add(otherInfo);
			}

			Map<String, Object> userInfos = new HashMap<String, Object>();
			userInfos.put("userId", p.getUserId());
			userInfos.put("score", p.getScore());
			userInfos.put("position", p.getPosition());
			userInfos.put("money", p.getMoney());
			userInfos.put("gender", p.getGender());
			userInfos.put("playStatus", p.getPlayStatus());
			userInfos.put("userName", p.getUserName());
			userInfos.put("userImg", p.getUserImg());
			userInfos.put("ip", p.getIp());
			userInfos.put("joinIndex", p.getJoinIndex());
			Player pp = RedisUtil.getPlayerByUserId(ids + "", cid);
			IoSession se = MinaServerManager.tcpServer.getSessions().get(pp.getSessionId());
			// 返回给房间内其他玩家
			if (se != null && se.isConnected()) {
				JSONObject result1 = getJSONObj(interfaceId, 1, userInfos);
				ProtocolData pd1 = new ProtocolData(interfaceId, result1.toJSONString());
				se.write(pd1);
			}
		}
		Map<String, Object> info = new HashMap<>();
		info.put("reqState", Cnst.REQ_STATE_1);
		info.put("playerNum", playerIds.size());
		info.put("roomSn", roomId);
		info.put("ip", room.getIp().concat(":").concat(Cnst.MINA_PORT));
		info.put("players", players);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		// 返回给刚进入房间的人
		session.write(pd);

		// 如果加入的代开房间 通知房主
		if (room.getExtraType().intValue() == Cnst.ROOM_EXTRA_TYPE_2 && !userId.equals(room.getCreateId())) {
			MessageFunctions.interface_100112(p, room, Cnst.ROOM_NOTICE_IN, cid);
		}
	}

	/**
	 * 用户点击同意协议
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100009(IoSession session, Map<String, Object> readData) throws Exception {
		logger.I("用户点击同意协议,interfaceId -> 100009");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		Player p = RedisUtil.getPlayerByUserId(String.valueOf(session.getAttribute(Cnst.USER_SESSION_USER_ID)), cid);
		if (p == null) {
			illegalRequest(interfaceId, session);
			return;
		}
		p.setUserAgree(1);
		Map<String, Object> info = new JSONObject();
		info.put("reqState", Cnst.REQ_STATE_1);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);

		// 更新redis数据
		RedisUtil.updateRedisData(null, p, cid);

		/* 刷新数据库，用户同意协议 */
		userService.updateUserAgree(p.getUserId());
	}

	/**
	 * 代开的房间数量
	 * 
	 * @param userId
	 * @param cid
	 * @return
	 */
	public static int getDaiKaiNum(Long userId, String cid) {
		int num = 0;
		Map<String, String> hgetAll = RedisUtil.hgetAll(Cnst.get_ROOM_DAIKAI_KEY(cid).concat(userId + ""));
		if (hgetAll != null) {
			for (Entry<String, String> entry : hgetAll.entrySet()) {
				if (entry == null || entry.getKey() == null)
					continue;
				RoomResp room = RedisUtil.getRoomRespByRoomId(entry.getKey(), cid);
				if (room.getCreateId().equals(userId) && room.getExtraType() != null && room.getExtraType().intValue() == Cnst.ROOM_EXTRA_TYPE_2 && room.getState() != Cnst.ROOM_STATE_YJS) {
					++num;
				} else {
					RedisUtil.hdel(Cnst.get_ROOM_DAIKAI_KEY(cid).concat(userId + ""), entry.getKey());
				}
			}
		}

		return num;
	}

	/**
	 * 查看代开房间列表
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100010(IoSession session, Map<String, Object> readData) throws Exception {
		logger.I("查看代开房间列表,interfaceId -> 100010");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		List<Map<String, Object>> info = new ArrayList<Map<String, Object>>();

		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		Map<String, String> hgetAll = RedisUtil.hgetAll(Cnst.get_ROOM_DAIKAI_KEY(cid).concat(userId + ""));

		if (hgetAll != null) {
			for (Entry<String, String> entry : hgetAll.entrySet()) {
				if (entry == null || entry.getKey() == null)
					continue;
				RoomResp room = RedisUtil.getRoomRespByRoomId(entry.getKey(), cid);
				if (room == null) {
					RedisUtil.hdel(Cnst.get_ROOM_DAIKAI_KEY(cid).concat(userId + ""), entry.getKey());
					continue;
				}
				if (room.getCreateId().equals(userId) && room.getExtraType() != null && room.getExtraType().intValue() == Cnst.ROOM_EXTRA_TYPE_2 && room.getState() != Cnst.ROOM_STATE_YJS) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("roomId", room.getRoomId());
					map.put("createTime", room.getCreateTime());
					map.put("circleNum", room.getCircleNum());
					map.put("lastNum", room.getLastNum());
					map.put("state", room.getState());
					map.put("roomType", room.getRoomType());
					map.put("can4Take2", room.getCan4take2());
					map.put("laiZi", room.getLaiZi());
					map.put("dingFen", room.getDingFen());
					map.put("mulType", room.getMulType());
					map.put("daPaiJiaoMan", room.getDaPaiJiaoMan());
					map.put("extraType", room.getExtraType());
					map.put("tiShi", room.getTiShi());
					map.put("XiaoJuNum", room.getXiaoJuNum());

					map.put("mingPaiType", room.getMingPaiType());
					map.put("diZhuType", room.getDizhuType());

					List<Map<String, Object>> playerInfo = new ArrayList<Map<String, Object>>();

					List<Player> list = RedisUtil.getPlayerList(room, cid);
					if (list != null && list.size() > 0) {
						for (Player p : list) {
							if (p == null)
								continue;
							Map<String, Object> pinfo = new HashMap<String, Object>();
							pinfo.put("userId", p.getUserId());
							pinfo.put("position", p.getPosition());
							pinfo.put("userName", p.getUserName());
							pinfo.put("userImg", p.getUserImg());
							pinfo.put("state", p.getState());
							playerInfo.add(pinfo);
						}
					}
					map.put("playerInfo", playerInfo);
					info.add(map);
				} else {
					RedisUtil.hdel(Cnst.get_ROOM_DAIKAI_KEY(cid).concat(userId + ""), entry.getKey());
				}
			}
		}
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
	}

	/**
	 * 查看历史代开房间列表
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100011(IoSession session, Map<String, Object> readData) throws Exception {

		logger.I("查看历史代开房间列表,interfaceId -> 100011");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		String userId = String.valueOf(readData.get("userId"));
		Integer page = StringUtils.parseInt(readData.get("page"));
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		String key = Cnst.get_REDIS_PLAY_RECORD_PREFIX_ROE_DAIKAI(cid).concat(userId);

		Long pageSize = RedisUtil.llen(key);
		int start = (page - 1) * Cnst.PAGE_SIZE;
		int end = start + Cnst.PAGE_SIZE;
		List<String> keys = RedisUtil.lrange(key, start, end);

		Map<String, Object> info = new HashMap<>();
		List<Map<String, String>> maps = new ArrayList<Map<String, String>>();
		for (String roomKey : keys) {
			if (RedisUtil.exists(Cnst.get_REDIS_PLAY_RECORD_PREFIX(cid).concat(roomKey))) {
				Map<String, String> roomInfos = RedisUtil.hgetAll(Cnst.get_REDIS_PLAY_RECORD_PREFIX(cid).concat(roomKey));
				maps.add(roomInfos);
			}
		}
		info.put("roomInfo", maps);
		info.put("pages", pageSize == null ? 0 : pageSize % Cnst.PAGE_SIZE == 0 ? pageSize / Cnst.PAGE_SIZE : (pageSize / Cnst.PAGE_SIZE + 1));
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
	}

	/**
	 * 代开模式中踢出玩家
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100012(IoSession session, Map<String, Object> readData) {
		logger.I("准备,interfaceId -> 100012");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomSn"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		// 房间不存在
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId), cid);
		if (room == null) {
			roomDoesNotExist(interfaceId, session);
			return;
		}

		try {
			// 验证解散人是否是真正的房主
			Long createId = Long.valueOf((String) session.getAttribute(Cnst.USER_SESSION_USER_ID));
			if (createId == null || !createId.equals(room.getCreateId())) {
				illegalRequest(interfaceId, session);
				return;
			}

			// 房主不能踢自己
			if (room.getCreateId().equals(userId)) {
				illegalRequest(interfaceId, session);
				return;
			}
		} catch (Exception e) {
			illegalRequest(interfaceId, session);
			return;
		}
		// 房间已经开局
		if (room.getState() != Cnst.ROOM_STATE_CREATED) {
			roomIsGaming(interfaceId, session);
			return;
		}

		List<Player> list = RedisUtil.getPlayerList(room, cid);

		boolean hasPlayer = false;// 列表中有当前玩家
		for (Player p : list) {
			if (p == null)
				continue;
			if (p.getUserId().equals(userId)) {
				// 初始化玩家
				p.initPlayer(null, Cnst.PLAYER_STATE_DATING, 0l);

				// 刷新房间用户列表
				List<Long> pids = room.getPlayerIds();
				if (pids != null) {
					for (int i = 0; i < pids.size(); i++) {
						if (userId.equals(pids.get(i))) {
							pids.set(i, null);
							break;
						}
					}
				}

				// 更新redis数据
				RedisUtil.updateRedisData(room, p, cid);
				hasPlayer = true;
				MessageFunctions.interface_100107(session, Cnst.EXIST_TYPE_EXIST, list, userId);
				break;
			}
		}

		Map<String, String> info = new HashMap<String, String>();
		info.put("reqState", String.valueOf(hasPlayer ? Cnst.REQ_STATE_1 : Cnst.REQ_STATE_8));
		if (hasPlayer) {
			info.put("userId", "" + userId);
			info.put("roomSn", "" + roomId);
		}
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
	}

	/**
	 * 代开模式中房主解散房间
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100013(IoSession session, Map<String, Object> readData) {
		logger.I("代开模式中踢出玩家,interfaceId -> 100013");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomSn"));

		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);

		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId), cid);
		// 房间不存在
		if (room == null) {
			roomDoesNotExist(interfaceId, session);
			return;
		}

		try {
			// 验证解散人是否是真正的房主
			Long createId = Long.valueOf((String) session.getAttribute(Cnst.USER_SESSION_USER_ID));
			if (createId == null || !createId.equals(room.getCreateId())) {
				illegalRequest(interfaceId, session);
				return;
			}
		} catch (Exception e) {
			illegalRequest(interfaceId, session);
			return;
		}

		// 房间已经开局
		if (room.getState() != Cnst.ROOM_STATE_CREATED) {
			roomIsGaming(interfaceId, session);
			return;
		}
		List<Player> players = RedisUtil.getPlayerList(room, cid);
		if (players != null && players.size() > 0) {
			for (Player p : players) {
				if (p == null)
					continue;
				// 初始化玩家
				p.initPlayer(null, Cnst.PLAYER_STATE_DATING, 0l);
			}
			RedisUtil.setPlayersList(players, cid);
		}

		// 归还玩家房卡
		Player cp = RedisUtil.getPlayerByUserId(String.valueOf(session.getAttribute(Cnst.USER_SESSION_USER_ID)), cid);

		if (room.getRoomType() == Cnst.ROOM_TYPE_1)
			cp.setMoney(cp.getMoney() + Cnst.moneyMap_1.get(room.getCircleNum()));
		else
			cp.setMoney(cp.getMoney() + Cnst.moneyMap_2.get(room.getCircleNum()));

		// 更新房主的redis数据
		RedisUtil.updateRedisData(null, cp, cid);

		RedisUtil.deleteByKey(Cnst.get_REDIS_PREFIX_ROOMMAP(cid).concat(String.valueOf(roomId)));

		if (room.getExtraType().intValue() == Cnst.ROOM_EXTRA_TYPE_2) {
			RedisUtil.hdel(Cnst.get_ROOM_DAIKAI_KEY(cid).concat(room.getCreateId() + ""), room.getRoomId() + "");
		}

		MessageFunctions.interface_100107(session, Cnst.EXIST_TYPE_DISSOLVE, players, null);

		Map<String, String> info = new HashMap<String, String>();
		info.put("reqState", String.valueOf(Cnst.REQ_STATE_1));
		info.put("money", String.valueOf(cp.getMoney()));
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);

		MessageFunctions.interface_100140(room, cid);
	}

	/**
	 * 回放的时候，获取房间的局数
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100014(IoSession session, Map<String, Object> readData) {
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		String roomId = StringUtils.toString((readData.get("roomSn")));
		String createTime = StringUtils.toString(readData.get("createTime"));
		Map<String, Object> info = new HashMap<String, Object>();
		int juNum = BackFileUtil.getFileNumByRoomId(Integer.parseInt(roomId));
		info.put("num", juNum);
		info.put("url", Cnst.HTTP_URL.concat(Cnst.BACK_FILE_PATH));
		info.put("roomSn", String.valueOf(roomId));
		info.put("createTime", createTime);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
	}

	/**
	 * 强制解散房间 1.必须是房主自己 2.游戏没开局
	 * 
	 * @param session
	 * @param readData
	 * @throws Exception
	 */
	public static void interface_100015(IoSession session, Map<String, Object> readData) throws Exception {
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomSn"));
		logger.I("*******强制解散房间" + roomId);
		Long userId = Long.valueOf((String) session.getAttribute(Cnst.USER_SESSION_USER_ID));
		if (userId == null) {
			illegalRequest(interfaceId, session);
			return;
		}

		Map<String, Object> info = new HashMap<>();
		info.put("reqState", Cnst.REQ_STATE_1);
		JSONObject result = MessageFunctions.getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		if (roomId != null) {
			RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId), cid);
			// 房间不是这个人创建的
			if (room == null || !room.getCreateId().equals(userId) || room.getState().intValue() < Cnst.ROOM_STATE_CREATED) {
				illegalRequest(interfaceId, session);
				return;
			}
			if (room != null && room.getCreateId().equals(userId)) {
				room.setState(Cnst.ROOM_STATE_YJS);
				List<Player> players = RedisUtil.getPlayerList(room, cid);

				if (room.getExtraType().intValue() == Cnst.ROOM_EXTRA_TYPE_2) {
					RedisUtil.hdel(Cnst.get_ROOM_DAIKAI_KEY(cid).concat(room.getCreateId() + ""), room.getRoomId() + "");
				}

				ArrayList<Long> arrayList = new ArrayList<Long>();
				for (Player player2 : players) {
					if (player2 != null) {
						arrayList.add(player2.getScore());
					} else {
						arrayList.add(null);
					}
				}
				room.setScore(arrayList);

				MessageFunctions.updateDatabasePlayRecord(room, cid);

				RedisUtil.deleteByKey(Cnst.get_REDIS_PREFIX_ROOMMAP(cid).concat(String.valueOf(roomId)));// 删除房间
				if (players != null && players.size() > 0) {
					for (Player p : players) {
						if (p == null)
							continue;
						// 初始化玩家
						p.initPlayer(null, Cnst.PLAYER_STATE_DATING, 0l);

						if (p.getUserId().equals(userId)) {
							if (room.getRoomType() == Cnst.ROOM_TYPE_1)
								p.setMoney(p.getMoney() + Cnst.moneyMap_1.get(room.getCircleNum()));
							else
								p.setMoney(p.getMoney() + Cnst.moneyMap_2.get(room.getCircleNum()));
						}
						RedisUtil.updateRedisData(null, p, cid);
					}
					for (Player p : players) {
						if (p == null)
							continue;
						if (p.getUserId().longValue() == userId.longValue())
							continue;
						IoSession se = MinaServerManager.tcpServer.getSessions().get(p.getSessionId());
						if (se != null && se.isConnected()) {
							// se.write(pd);
							MessageFunctions.interface_100107(session, Cnst.EXIST_TYPE_DISSOLVE, players, null);
						}
					}
				}
			} else {
				System.out.println("*******强制解散房间" + roomId + "，房间不存在");
			}

			MessageFunctions.interface_100140(room, cid);
		}

		session.write(pd);

	}

	/**
	 * 或得到的是一个正数，要拿当前玩家的剩余房卡，减去这个值
	 * 
	 * @param userId
	 * @return
	 */
	private static Integer getFrozenMoney(Long userId, String cid) {
		int frozenMoney = 0;
		Set<String> roomMapKeys = RedisUtil.getSameKeys(Cnst.get_REDIS_PREFIX_ROOMMAP(cid));
		if (roomMapKeys != null && roomMapKeys.size() > 0) {
			for (String roomId : roomMapKeys) {
				RoomResp room = RedisUtil.getRoomRespByRoomId(roomId, cid);
				if (room.getCreateId().equals(userId) && room.getState() == Cnst.ROOM_STATE_CREATED) {
					if (room.getRoomType() == Cnst.ROOM_TYPE_1)
						frozenMoney += Cnst.moneyMap_1.get(room.getCircleNum());
					else
						frozenMoney += Cnst.moneyMap_2.get(room.getCircleNum());
				}
			}
		}
		return frozenMoney;
	}

	/**
	 * 返回用户
	 * 
	 * @param openId
	 * @param ip
	 * @return
	 * @throws Exception
	 */
	public static Player getPlayerInfos(String openId, String ip, String cid, IoSession session) {
		if (cid == null) {
			return null;
		}
		Player p = null;
		String clientIp = ((InetSocketAddress) session.getRemoteAddress()).getAddress().getHostAddress();
		long updateTime = 0;
		try {
			String notice = RedisUtil.getStringByKey(Cnst.get_NOTICE_KEY(cid));
			if (notice == null) {
				notice = userService.getNotice(cid);
				RedisUtil.setObject(Cnst.get_NOTICE_KEY(cid), notice, null);
				// setStringByKey(Cnst.NOTICE_KEY, "接口都是经济");
			}
			Set<String> openIds = RedisUtil.getSameKeys(Cnst.get_REDIS_PREFIX_OPENIDUSERMAP(cid));
			if (openIds != null && openIds.contains(openId)) {// 用户是断线重连
				Long userId = RedisUtil.getUserIdByOpenId(openId, cid);
				p = RedisUtil.getPlayerByUserId(String.valueOf(userId), cid);
				IoSession se = session.getService().getManagedSessions().get(p.getSessionId());
				p.setNotice(notice);
				p.setState(Cnst.PLAYER_LINE_STATE_INLINE);
				updateTime = p.getUpdateTime() == null ? 0l : p.getUpdateTime();
				if (se != null) {
					Long tempuserId = Long.valueOf((String) se.getAttribute(Cnst.USER_SESSION_USER_ID));
					if (se.getId() != session.getId() && userId.equals(tempuserId)) {
						MessageFunctions.interface_100106(se);
					}
				}
				if (p.getPlayStatus() != null && p.getPlayStatus().equals(Cnst.PLAYER_STATE_DATING)) {// 去数据库重新请求用户，//需要减去玩家开的房卡
					p = userService.getByOpenId(openId, cid);
					if (p == null) {
						p = userService_login.getUserInfoByOpenId(openId, cid);
						if (p == null) {
							return null;
						} else {
							p.setUserAgree(0);
							p.setGender(p.getGender());
							p.setTotalGameNum("0");
							p.setMoney(userService.getMoneyInit(cid));
							p.setLoginStatus(1);
							p.setCId(cid);
							String time = String.valueOf(new Date().getTime());
							p.setLastLoginTime(time);
							p.setSignUpTime(time);
							p.setUpdateTime(System.currentTimeMillis());
							userService.save(p);

						}
					} else {
						// FIXME 判断是否更新昵称等数据 注意这个是从数据库读取到的数据 所以怎么判断 要注意
						// 但是这个库里面金币是正确的 UID也是正确的 只是昵称不太一样
						if (System.currentTimeMillis() - updateTime > Cnst.updateDiffTime) {
							Player updatep = userService_login.getUserInfoByOpenId(openId, cid);
							p.setUserName(updatep.getUserName());
							p.setUserImg(updatep.getUserImg());
							p.setGender(updatep.getGender());
							p.setUpdateTime(System.currentTimeMillis());
							// FIXME 以后私自在服务库改CID对应的TID 注意在这里判断updatep.uid 是否=
							// p.uid
							// 如果不相等 修改p.uid 为最新UID 然后更新到本地库
						}
					}
					p.setScore(0l);
					p.setIp(ip);
					p.setNotice(notice);
					p.setState(Cnst.PLAYER_LINE_STATE_INLINE);
					p.setPlayStatus(Cnst.PLAYER_STATE_DATING);
					p.setMoney(p.getMoney() - getFrozenMoney(p.getUserId(), cid));
				}
				// 更新用户ip 最后登陆时间
//				userService.updateIpAndLastTime(openId, clientIp);
				return p;
			}
			p = userService.getByOpenId(openId, cid);
			if (p != null) {// 当前游戏的数据库中存在该用户
				p.setNotice(notice);

				Player redisP = RedisUtil.getPlayerByUserId(String.valueOf(p.getUserId()), cid);
				updateTime = (redisP == null || redisP.getUpdateTime() == null) ? 0l : redisP.getUpdateTime();
				// FIXME 判断是否更新昵称等数据 注意这个是从数据库读取到的数据 所以怎么判断 要注意
				// 但是这个库里面金币是正确的 UID也是正确的
				if (System.currentTimeMillis() - updateTime > Cnst.updateDiffTime) {
					Player updatep = userService_login.getUserInfoByOpenId(openId, cid);
					p.setUserName(updatep.getUserName());
					p.setUserImg(updatep.getUserImg());
					p.setGender(updatep.getGender());
					p.setUpdateTime(System.currentTimeMillis());
					// FIXME 以后私自在服务库改CID对应的TID 注意在这里判断updatep.uid 是否= p.uid
					// 如果不相等 修改p.uid 为最新UID 然后更新到本地库
				}
			} else {// 如果没有，需要去微信的用户里查询
				p = userService_login.getUserInfoByOpenId(openId, cid);
				if (p == null) {
					return null;
				} else {
					p.setUserAgree(0);
					p.setGender(p.getGender());
					p.setTotalGameNum("0");
					p.setMoney(userService.getMoneyInit(cid));
					p.setLoginStatus(1);
					p.setCId(cid);
					String time = String.valueOf(new Date().getTime());
					p.setLastLoginTime(time);
					p.setSignUpTime(time);
					p.setUpdateTime(System.currentTimeMillis());
					userService.save(p);
				}
			}
			p.setScore(0l);
			p.setIp(ip);
			p.setNotice(notice);
			p.setState(Cnst.PLAYER_LINE_STATE_INLINE);
			p.setPlayStatus(Cnst.PLAYER_STATE_DATING);
			p.setMoney(p.getMoney() - getFrozenMoney(p.getUserId(), cid));
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 更新用户ip 最后登陆时间
//		userService.updateIpAndLastTime(openId, clientIp);
		return p;
	}

}
