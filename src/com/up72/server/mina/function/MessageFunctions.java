package com.up72.server.mina.function;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Card;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.server.mina.bean.ProtocolData;
import com.up72.server.mina.main.MinaServerManager;
import com.up72.server.mina.utils.StringUtils;
import com.up72.server.mina.utils.redis.RedisUtil;

/**
 * Created by Administrator on 2017/7/10. 推送消息类
 */
public class MessageFunctions extends TCPGameFunctions {

	/**
	 * 发送玩家信息
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100100(IoSession session, Map<String, Object> readData) throws Exception {
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Map<String, Object> info = new HashMap<>();
		if (interfaceId.equals(100100)) {// 刚进入游戏主动请求
			String openId = String.valueOf(readData.get("openId"));

			Player currentPlayer = null;
			String cid = String.valueOf(readData.get("cId"));
			if (openId == null || cid == null) {
				illegalRequest(interfaceId, session);
				return;
			} else {
				String ip = (String) session.getAttribute(Cnst.USER_SESSION_IP);
				currentPlayer = HallFunctions.getPlayerInfos(openId, ip, cid, session);
			}
			if (currentPlayer == null) {
				illegalRequest(interfaceId, session);
				return;
			}

			// 更新心跳为最新上线时间
//			RedisUtil.hset(Cnst.get_REDIS_HEART_PREFIX(cid), currentPlayer.getUserId() + "", String.valueOf(new Date().getTime()), null);

			currentPlayer.setCId(cid);

			currentPlayer.setSessionId(session.getId());// 更新sesisonId
			session.setAttribute(Cnst.USER_SESSION_USER_ID, currentPlayer.getUserId() + "");
			session.setAttribute(Cnst.USER_SESSION_CID, cid);
			if (openId != null) {
				RedisUtil.setObject(Cnst.get_REDIS_PREFIX_OPENIDUSERMAP(cid).concat(openId), currentPlayer.getUserId(), null);
			}

			RoomResp room = null;
			List<Player> players = null;

			if (currentPlayer.getRoomId() != null) {// 玩家下有roomId，证明在房间中
				room = RedisUtil.getRoomRespByRoomId(String.valueOf(currentPlayer.getRoomId()), cid);
				if (room != null && room.getState() != Cnst.ROOM_STATE_YJS) {
					info.put("roomInfo", getRoomInfo(room, currentPlayer.getUserId()));
					players = RedisUtil.getPlayerList(room, cid);

					info.put("anotherUsers", getAnotherUserInfo(players, room, currentPlayer.getUserId()));

				} else {
					currentPlayer.initPlayer(null, Cnst.PLAYER_STATE_DATING, 0l);
				}

			} else {
				if (currentPlayer.getPlayStatus() == null)
					currentPlayer.setPlayStatus(Cnst.PLAYER_STATE_DATING);
			}

			RedisUtil.updateRedisData(room, currentPlayer, cid);
			info.put("currentUser", getCurrentUserInfo(currentPlayer, room));

			if (room != null) {
				// room.setWsw_sole_main_id(room.getWsw_sole_main_id()+1);

				info.put("wsw_sole_main_id", room.getWsw_sole_main_id());
				info.put("wsw_sole_action_id", room.getWsw_sole_action_id());
				Map<String, Object> roomInfo = (Map<String, Object>) info.get("roomInfo");
				List<Map<String, Object>> anotherUsers = (List<Map<String, Object>>) info.get("anotherUsers");

				info.remove("roomInfo");
				info.remove("anotherUsers");

				JSONObject result = getJSONObj(interfaceId, 1, info);
				ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
				session.write(pd);

				info.remove("currentUser");
				info.put("roomInfo", roomInfo);
				result = getJSONObj(interfaceId, 1, info);
				pd = new ProtocolData(interfaceId, result.toJSONString());
				session.write(pd);

				info.remove("roomInfo");
				info.put("anotherUsers", anotherUsers);
				result = getJSONObj(interfaceId, 1, info);
				pd = new ProtocolData(interfaceId, result.toJSONString());
				session.write(pd);

				MessageFunctions.interface_100109(players, Cnst.PLAYER_LINE_STATE_INLINE, currentPlayer.getUserId(), currentPlayer.getPlayStatus());
				if (room.getExtraType().intValue() == Cnst.ROOM_EXTRA_TYPE_2) {
					MessageFunctions.interface_100112(currentPlayer, room, Cnst.ROOM_NOTICE_DAIKAI_IN, cid);
				}
			} else {
				JSONObject result = getJSONObj(interfaceId, 1, info);
				ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
				session.write(pd);
			}

		} else {
			session.close(true);
		}

	}

	// 封装currentUser
	public static Map<String, Object> getCurrentUserInfo(Player player, RoomResp room) {
		Map<String, Object> currentUserInfo = new HashMap<String, Object>();
		currentUserInfo.put("version", String.valueOf(Cnst.version));
		currentUserInfo.put("userId", player.getUserId());
		currentUserInfo.put("position", player.getPosition());
		currentUserInfo.put("playStatus", player.getPlayStatus());
		currentUserInfo.put("userName", player.getUserName());
		currentUserInfo.put("userImg", player.getUserImg());
		currentUserInfo.put("gender", player.getGender());
		currentUserInfo.put("ip", player.getIp());
		currentUserInfo.put("joinIndex", player.getJoinIndex());
		currentUserInfo.put("userAgree", player.getUserAgree());
		currentUserInfo.put("money", player.getMoney());
		currentUserInfo.put("score", player.getScore());
		currentUserInfo.put("notice", player.getNotice());

		int idx = -1;

		if (room != null) {
			for (int i = 0; i < room.getPlayerIds().size(); i++) {
				if (room.getPlayerIds().get(i) == null)
					continue;
				if (room.getPlayerIds().get(i).longValue() == player.getUserId().longValue()) {
					idx = i;
					break;
				}
			}

			if (idx > -1 && room.getState() > Cnst.ROOM_STATE_CREATED) {
				long mul = 1l;
				if (room.getMuls() != null && room.getMuls().size() > idx && room.getMuls().get(idx) != null && room.getMuls().get(idx) != 0)
					mul = mul * 2l;

				if (room.getMingPaiInfo() != null && room.getMingPaiInfo().size() > idx && room.getMingPaiInfo().get(idx) != null && room.getMingPaiInfo().get(idx) != 0)
					mul = mul * 2l;

				currentUserInfo.put("mul", mul);// 倍数 需要给前端直接算好
			}

			if (room != null && room.getPlayStatus() != null) {
				List<Integer> paiInfos = new ArrayList<Integer>();
				List<Card> currentCardList = player.getCurrentCardList();

				for (int i = 0; i < currentCardList.size(); i++) {
					paiInfos.add(currentCardList.get(i).getOrigin());
				}
				currentUserInfo.put("paiInfos", paiInfos);
			}
		}
		return currentUserInfo;
	}

	// 封装anotherUsers
	public static List<Map<String, Object>> getAnotherUserInfo(List<Player> players, RoomResp room, Long exceptUid) {
		List<Map<String, Object>> anotherUserInfos = new ArrayList<Map<String, Object>>();
		int idx = -1;
		for (Player player : players) {
			++idx;
			if (player == null || player.getUserId().longValue() == exceptUid)
				continue;
			Map<String, Object> currentUserInfo = new HashMap<String, Object>();
			currentUserInfo.put("userId", player.getUserId());
			currentUserInfo.put("position", player.getPosition());
			currentUserInfo.put("playStatus", player.getPlayStatus());
			currentUserInfo.put("userName", player.getUserName());
			currentUserInfo.put("userImg", player.getUserImg());
			currentUserInfo.put("gender", player.getGender());
			currentUserInfo.put("ip", player.getIp());
			currentUserInfo.put("joinIndex", player.getJoinIndex());
			currentUserInfo.put("userAgree", player.getUserAgree());
			currentUserInfo.put("money", player.getMoney());
			currentUserInfo.put("score", player.getScore());
			currentUserInfo.put("notice", player.getNotice());
			if (room != null && room.getPlayStatus() != null) {
				Integer paiInfos = player.getCurrentCardList().size();

				currentUserInfo.put("paiInfos", paiInfos);

				if (room.getMingPaiInfo() != null && room.getMingPaiInfo().size() > idx && room.getMingPaiInfo().get(idx) != null && room.getMingPaiInfo().get(idx) == 1) {
					// 明牌了
					JSONArray tmpPaiInfos = new JSONArray();// 封装当前手牌
					for (Card card : player.getCurrentCardList()) {
						tmpPaiInfos.add(card.getOrigin());
					}
					currentUserInfo.put("paiInfos", tmpPaiInfos);
				}
			}
			anotherUserInfos.add(currentUserInfo);
		}
		return anotherUserInfos;
	}

	// 封装房间信息
	public static Map<String, Object> getRoomInfo(RoomResp room, Long userId) {
		Map<String, Object> roomInfo = new HashMap<String, Object>();
		roomInfo.put("userId", room.getCreateId());
		roomInfo.put("openName", room.getOpenName());
		roomInfo.put("createTime", room.getCreateTime());
		roomInfo.put("roomId", room.getRoomId());
		roomInfo.put("state", room.getState());
		roomInfo.put("playStatus", room.getPlayStatus());
		roomInfo.put("lastNum", room.getLastNum());
		roomInfo.put("totalNum", room.getTotalNum());
		roomInfo.put("roomType", room.getRoomType());
		roomInfo.put("circleNum", room.getCircleNum());
		roomInfo.put("can4Take2", room.getCan4take2());
		roomInfo.put("isLaiZi", room.getLaiZi());
		roomInfo.put("dingFen", room.getDingFen());
		roomInfo.put("daPaiJiaoMan", room.getDaPaiJiaoMan());
		roomInfo.put("mulType", room.getMulType());
		roomInfo.put("extraType", room.getExtraType());
		
		roomInfo.put("mingPaiType", room.getMingPaiType() + "");
		roomInfo.put("diZhuType", room.getDizhuType() + "");
		if (room.getState() != Cnst.ROOM_STATE_CREATED) {
			roomInfo.put("xjst", room.getXjst());

			if (room.getDizhu() != null && room.getDiFen() != null && room.getDiFen() > 0)
				roomInfo.put("diFen", room.getDiFen() * Math.pow(2, RoomResp.getRealGiveUpTime(room)));

			ArrayList<JSONObject> arrayList2 = new ArrayList<JSONObject>();

			JSONObject mulsJson = new JSONObject();
			JSONObject mingPaiJson = new JSONObject();
			for (int i = 0; i < room.getPlayerIds().size(); i++) {
				if (room.getMuls() != null && room.getMuls().size() > i && room.getMuls().get(i) != null) {
					mulsJson.put(room.getPlayerIds().get(i) + "", room.getMuls().get(i));
				}

				if (room.getMingPaiInfo() != null && room.getMingPaiInfo().size() > i && room.getMingPaiInfo().get(i) != null) {
					mingPaiJson.put(room.getPlayerIds().get(i) + "", room.getMingPaiInfo().get(i));
				}

				JSONObject json2 = new JSONObject();
				json2.put("uid", room.getPlayerIds().get(i));
				if (room.getJiaoDiZhu() != null && room.getJiaoDiZhu().size() > i && room.getJiaoDiZhu().get(i) != null) {

					json2.put("info", room.getJiaoDiZhu().get(i));
				} else {
					json2.put("info", 0);
				}
				arrayList2.add(json2);
			}

			roomInfo.put("muls", mulsJson);
			roomInfo.put("mul", GameFunctions.getMulJsonObject(room));
			roomInfo.put("mingPaiInfo", mingPaiJson);

			roomInfo.put("jiaodizhu", arrayList2);// :[{"uid":"玩家UID","info":"0不叫 1-3叫了地主 -1没发言过","type":"1叫 2抢"}],

			roomInfo.put("dizhu", room.getDizhu());

			if (room.getCards() != null) {
				JSONObject cards = new JSONObject();
				int tmpIdx = 0;
				for (ArrayList<Card> list : room.getCards()) {
					if (list != null) {
						List<JSONObject> tmp = new ArrayList<JSONObject>();

						for (Card c : list) {
							tmp.add(Card.getReturnJson(c));
						}

						cards.put(room.getPlayerIds().get(tmpIdx) + "", tmp);
					}
					++tmpIdx;
				}
				roomInfo.put("chuPaiInfo", cards);
			}

//			if (room.getLastCards() != null) {
//				JSONObject cards = new JSONObject();
//				int tmpIdx = 0;
//				for (ArrayList<Card> list : room.getLastCards()) {
//					if (list != null) {
//						List<JSONObject> tmp = new ArrayList<JSONObject>();
//
//						for (Card c : list) {
//							tmp.add(Card.getReturnJson(c));
//						}
//						cards.put(room.getPlayerIds().get(tmpIdx) + "", tmp);
//					}
//					++tmpIdx;
//				}
//				roomInfo.put("lastCards", cards);
//			}
			if (room.getLastCards() != null) {
				roomInfo.put("lastCards", room.getLastCards());
			}

			if (room.getDizhu() != null) {
				List<Card> currentCardList = room.getCurrentCardList();
				JSONArray arr = new JSONArray();
				for (Card c : currentCardList) {
					arr.add(c.getOrigin());
				}
				roomInfo.put("diZhuPai", arr);
			}
			roomInfo.put("lastAction", room.getLastAction());
			roomInfo.put("lastUserId", room.getLastUserId());
			if (room.getLastAction() == null)
				roomInfo.put("lastActionExtra", null);
			else if (room.getLastAction() == Cnst.ACTION_CHUPAI) {
				List<Card> lastChuPai = room.getRealLastChuPai();
				List<JSONObject> list = new ArrayList<JSONObject>();
				for (Card c : lastChuPai) {
					list.add(Card.getReturnJson(c));
				}
				roomInfo.put("lastActionExtra", list);
			} else {
				roomInfo.put("lastActionExtra", room.getLastActionExtra());
			}
			roomInfo.put("realLastAction", room.getRealLastAction());
			roomInfo.put("realLastUser", room.getRealLastUserId());
			if (room.getRealLastChuPai() != null) {
				List<Card> lastChuPai = room.getRealLastChuPai();
				List<JSONObject> list = new ArrayList<JSONObject>();
				for (Card c : lastChuPai) {
					list.add(Card.getReturnJson(c));
				}
				roomInfo.put("realLastChuPai", list);
			}
			roomInfo.put("currentUserId", room.getCurrentUserId());
			roomInfo.put("currentUserAction", room.getCurrentUserAction());
			roomInfo.put("giveUpTime", RoomResp.getRealGiveUpTime(room));

		}

		roomInfo.put("tiShi", room.getTiShi());
		if (room.getDissolveRoom() != null) {
			Map<String, Object> dissolveRoom = new HashMap<String, Object>();
			dissolveRoom.put("dissolveTime", room.getDissolveRoom().getDissolveTime());
			dissolveRoom.put("userId", room.getDissolveRoom().getUserId());
			dissolveRoom.put("othersAgree", room.getDissolveRoom().getOthersAgree());
			roomInfo.put("dissolveRoom", dissolveRoom);
		} else {
			roomInfo.put("dissolveRoom", null);
		}
		return roomInfo;
	}

	/**
	 * 小结算
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100102(IoSession session, Map<String, Object> readData) {
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomSn"));

		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);

		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId), cid);

		if (room == null || room.getState() < Cnst.ROOM_STATE_XJS)
			return;// 现在状态不对 不能请求小结算

		List<Player> players = RedisUtil.getPlayerList(room, cid);
		List<Map<String, Object>> userInfos = new ArrayList<Map<String, Object>>();

		long constMul = 1l;
		if (room.getNongminChu() != null && room.getNongminChu() == 0)
			constMul = constMul * 2l;

		if (room.getDizhuChu() != null && room.getDizhuChu() < 2)
			constMul = constMul * 2l;

		if (room.getZhandans() != null) {
			for (int i = 0; i < room.getZhandans().size(); i++) {
				constMul = constMul * 2l;
			}
		}

		for (int idx = 0; idx < players.size(); idx++) {
			Map<String, Object> map = new HashMap<String, Object>();
			Player p = players.get(idx);
			map.put("userId", p.getUserId());

			if (room.getWinner() == 0) {
				// 农民赢
				if (p.getUserId().longValue() == room.getDizhu().longValue()) {
					map.put("result", 0);
					map.put("role", 1);
				} else {
					map.put("result", 1);
					map.put("role", 0);
				}
			} else {
				if (p.getUserId().longValue() == room.getDizhu().longValue()) {
					map.put("result", 1);
					map.put("role", 1);
				} else {
					map.put("result", 0);
					map.put("role", 0);
				}
			}

			List<Integer> pais = new ArrayList<Integer>();
			for (Card c : p.getCurrentCardList()) {
				pais.add(c.getOrigin());
			}

			map.put("pais", pais);
			map.put("finalScore", p.getScore());
			map.put("mul", room.getMuls().get(idx));
			map.put("mingPai", room.getMingPaiInfo().get(idx));
			map.put("spring", (room.getNongminChu() == 0 || room.getDizhuChu() < 2) ? 1 : 0);
			map.put("score", room.getXiaoJieSuanScore().get(idx));

			map.put("zhandan", room.getZhandanInfo().get(idx));

			long tmpMul = constMul;

			// 额外判断加倍问题
			if (p.getUserId().equals(room.getDizhu())) {

				if (room.getMuls() != null && room.getMuls().size() > idx && room.getMuls().get(idx) != null && room.getMuls().get(idx) != 0) {
					// 自己喊了加倍
					tmpMul = tmpMul * 2;
				}

				// 自己明牌了
				if (room.getMingPaiInfo().get(idx) != null && room.getMingPaiInfo().get(idx) == 1)
					tmpMul = tmpMul * 2;

				// 判断其他农民是否喊了加倍
				long tmp = 0;
				for (int i = 1; i < 3; i++) {
					int tmpX = (i + idx) % 3;

					int thisOne = 1;
					if (room.getMuls() != null && room.getMuls().size() > tmpX && room.getMuls().get(tmpX) != null && room.getMuls().get(tmpX) != 0) {
						// 自己喊了加倍
						thisOne = thisOne * 2;
					}

					if (room.getMingPaiInfo().get(tmpX) != null && room.getMingPaiInfo().get(tmpX) != 0) {
						// 有人也明牌了
						thisOne = thisOne * 2;
					}

					tmp = tmp + thisOne;
				}
				tmpMul = tmpMul * tmp;

			} else {
				if (room.getMuls() != null && room.getMuls().size() > idx && room.getMuls().get(idx) != null && room.getMuls().get(idx) != 0) {
					// 自己喊了加倍
					tmpMul = tmpMul * 2;
				}

				// 自己明牌了
				if (room.getMingPaiInfo().get(idx) != null && room.getMingPaiInfo().get(idx) == 1)
					tmpMul = tmpMul * 2;

				long tmp = 0;
				for (int i = 1; i < 3; i++) {
					int tmpX = (i + idx) % 3;
					if (!room.getDizhu().equals(room.getPlayerIds().get(tmpX)))
						continue;

					int thisOne = 1;
					if (room.getMuls() != null && room.getMuls().size() > tmpX && room.getMuls().get(tmpX) != null && room.getMuls().get(tmpX) != 0) {
						// 自己喊了加倍
						thisOne = thisOne * 2;
					}

					if (room.getMingPaiInfo().get(tmpX) != null && room.getMingPaiInfo().get(tmpX) != 0) {
						// 地主喊了加倍
						thisOne = thisOne * 2;
					}

					tmp = thisOne;
					break;
				}
				tmpMul = tmpMul * tmp;
			}
			
			map.put("currentMul", tmpMul);
			room.getRealMuls().add(idx,(int)tmpMul);
			userInfos.add(map);
		}
		
		// 赢得人的最后出的牌
		List<JSONObject> winPai = new ArrayList<JSONObject>();

		// 如果癞子过多 会有问题找不到真正赢得人 特殊请求 最后所有人手里面有癞子 现在只有一张癞子 暂时没问题
		//FIXME
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i).getCurrentCardList().isEmpty()) {
				ArrayList<Card> arrayList = room.getCards().get(i);
				for (Card card : arrayList) {
					winPai.add(Card.getReturnJson(card));
				}

				break;
			}
		}

		JSONObject info = new JSONObject();
		info.put("lastNum", room.getLastNum());
		info.put("winType", room.getWinner());
		info.put("winPais", winPai);
		info.put("difen", room.getDiFen() * Math.pow(2, RoomResp.getRealGiveUpTime(room)));
		info.put("userInfos", userInfos);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);

	}

	/**
	 * 大结算
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100103(IoSession session, Map<String, Object> readData) {
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomSn"));

		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId), cid);

		String key = roomId + "-" + room.getCreateTime();
		List<Map> userInfos = RedisUtil.getPlayRecord(Cnst.get_REDIS_PLAY_RECORD_PREFIX_OVERINFO(cid).concat(key));
		JSONObject info = new JSONObject();
		info.put("XiaoJuNum", room.getXiaoJuNum());
		if (!RedisUtil.exists(Cnst.get_REDIS_PLAY_RECORD_PREFIX_OVERINFO(cid).concat(key))) {
			List<Map<String, Object>> zeroUserInfos = new ArrayList<Map<String, Object>>();
			List<Player> players = RedisUtil.getPlayerList(room, cid);
			int idx = 0;
			for (Player p : players) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("userId", p.getUserId());
				map.put("score", room.getScore().get(idx));
				map.put("finalScore", room.getScore().get(idx));
				map.put("position", p.getPosition());
				map.put("userName", p.getUserName());
				map.put("userImg", p.getUserImg());
				zeroUserInfos.add(map);
				++idx;
			}
			info.put("userInfos", zeroUserInfos);
		} else {
			info.put("userInfos", userInfos);
		}

		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);

		// 更新 player
		Player p = RedisUtil.getPlayerByUserId(userId + "", cid);
		p.initPlayer(null, Cnst.PLAYER_STATE_DATING, 0l);
		room.setOutNum(room.getOutNum() == null ? 1 : room.getOutNum() + 1);
		if (room.getOutNum() >= room.getPlayerIds().size()) {
			RedisUtil.deleteByKey(Cnst.get_REDIS_PREFIX_ROOMMAP(cid).concat(roomId + ""));
		}
		// 更新outNum
		RedisUtil.updateRedisData(room, p, cid);
	}

	/**
	 * 动作回应
	 */
	public static void interface_100104(JSONObject info, RoomResp room, Integer interfaceId, String cid) {
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		List<Player> players = RedisUtil.getPlayerList(room, cid);
		for (int i = 0; i < players.size(); i++) {
			IoSession se = MinaServerManager.tcpServer.getSessions().get(players.get(i).getSessionId());
			if (se != null && se.isConnected()) {
				se.write(pd);
			}
		}
	}
	
	/**
	 * 闪斗到最后,单独推送一个出牌消息
	 */
	public static void interface_100104(JSONObject info, RoomResp room, Player player, Integer interfaceId, String cid) {
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		IoSession se = MinaServerManager.tcpServer.getSessions().get(player.getSessionId());
		if (se != null && se.isConnected()) {
			logger.I("发送数据");
		}
	}
	

	/**
	 * 发牌推送
	 */
	public static void interface_100105(JSONObject info, RoomResp room, Integer interfaceId, String cid) {
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		List<Player> players = RedisUtil.getPlayerList(room, cid);
		for (int i = 0; i < players.size(); i++) {
			IoSession se = MinaServerManager.tcpServer.getSessions().get(players.get(i).getSessionId());
			if (se != null && se.isConnected()) {
				se.write(pd);
			}
		}
	}

	/**
	 * 多地登陆提示
	 * 
	 * @param session
	 */
	public static void interface_100106(IoSession session) {
		Integer interfaceId = 100106;
		JSONObject result = getJSONObj(interfaceId, 1, "out");
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
		session.close(true);
	}

	/**
	 * 玩家被踢/房间被解散提示
	 * 
	 * @param session
	 */
	public static void interface_100107(IoSession session, Integer type, List<Player> players, Long leaveUserId) {
		Integer interfaceId = 100107;
		Map<String, Object> info = new HashMap<String, Object>();

		if (players == null || players.size() == 0) {
			return;
		}
		info.put("userId", session.getAttribute(Cnst.USER_SESSION_USER_ID));
		info.put("type", type);

		if (leaveUserId != null)
			info.put("leaveUserId", leaveUserId);

		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		for (Player p : players) {
			if (p == null)
				continue;
			IoSession se = MinaServerManager.tcpServer.getSessions().get(p.getSessionId());
			if (se != null && se.isConnected()) {
				se.write(pd);
			}
		}
	}

	/**
	 * 方法id不符合
	 * 
	 * @param session
	 */
	public static void interface_100108(IoSession session) {
		Integer interfaceId = 100108;
		Map<String, Object> info = new HashMap<String, Object>();
		info.put("reqState", Cnst.REQ_STATE_9);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
	}

	/**
	 * 用户离线/上线提示
	 * 
	 * @param state
	 */
	public static void interface_100109(List<Player> players, Integer state, Long userId, Integer playStatus) {
		Integer interfaceId = 100109;
		Map<String, Object> info = new HashMap<String, Object>();
		info.put("userId", userId);
		info.put("state", state);
		info.put("playStatus", playStatus);
		
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());

		if (players != null && players.size() > 0) {
			for (Player p : players) {
				if (p != null && !p.getUserId().equals(userId)) {
					IoSession se = MinaServerManager.tcpServer.getSessions().get(p.getSessionId());
					if (se != null && se.isConnected()) {
						se.write(pd);
					}
				}
			}
		}
	}

	/**
	 * 后端主动解散房间推送
	 * 
	 * @param reqState
	 * @param players
	 */
	public static void interface_100111(int reqState, List<Player> players, Integer roomId, Long creater) {
		Integer interfaceId = 100111;
		Map<String, Object> info = new HashMap<String, Object>();
		info.put("reqState", reqState);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		if (players != null && players.size() > 0) {
			for (Player p : players) {
				if (p == null)
					continue;

				if (p.getRoomId() != null && p.getRoomId().equals(roomId)) {
					IoSession se = MinaServerManager.tcpServer.getSessions().get(p.getSessionId());
					if (se != null && se.isConnected()) {
						if (creater != null && creater.equals(p.getUserId())) {
							info.put("money", p.getMoney());
							JSONObject result1 = getJSONObj(interfaceId, 1, info);
							ProtocolData pd1 = new ProtocolData(interfaceId, result1.toJSONString());
							se.write(pd1);
						} else
							se.write(pd);
					}
				}
			}
		}

	}

	/**
	 * 加入代开房间推送
	 * 
	 * @param reqState
	 * @param players
	 */
	public static void interface_100112(Player player, RoomResp room, Integer extraType, String cid) {
		Integer interfaceId = 100112;
		// 先判断房主是否在线
		Player roomCreater = RedisUtil.getPlayerByUserId(String.valueOf(room.getCreateId()), cid);
		IoSession se = MinaServerManager.tcpServer.getSessions().get(roomCreater.getSessionId());
		if (se != null && se.isConnected()) {
			Map<String, Object> info = new HashMap<String, Object>();
			info.put("roomSn", room.getRoomId());
			info.put("userId", player.getUserId());
			info.put("userName", player.getUserName());
			info.put("userImg", player.getUserImg());
			if (extraType.intValue() == Cnst.ROOM_NOTICE_IN){
				info.put("position", player.getPosition());
				info.put("extraType", extraType);
			}
			if (extraType.intValue() == Cnst.ROOM_NOTICE_OUT){
				info.put("extraType", extraType);
			}
			if(extraType.intValue() == Cnst.ROOM_NOTICE_DAIKAI_IN){
				info.put("position", player.getPosition());
				info.put("extraType", Cnst.ROOM_NOTICE_DAIKAI_IN);
			}
			if(extraType.intValue() == Cnst.ROOM_NOTICE_DAIKAI_OUT){
				info.put("position", player.getPosition());
				info.put("extraType", Cnst.ROOM_NOTICE_DAIKAI_OUT);
			}
		    
			JSONObject result = getJSONObj(interfaceId, 1, info);
			ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
			se.write(pd);
		} else {
			return;
		}

	}

	/**
	 * 代开房间因为任何原因关闭
	 * 
	 * @param room
	 */
	public static void interface_100140(RoomResp room, String cid) {
		if (room == null)
			return;
		if (room.getExtraType().intValue() != Cnst.ROOM_EXTRA_TYPE_2)
			return;
		Integer interfaceId = 100140;
		List<Player> playerList = RedisUtil.getPlayerList(room, cid);
		// 先判断房主是否在线
		Player roomCreater = RedisUtil.getPlayerByUserId(String.valueOf(room.getCreateId()), cid);
		IoSession se = MinaServerManager.tcpServer.getSessions().get(roomCreater.getSessionId());
		if (se != null && se.isConnected()) {
			Map<String, Object> info = new HashMap<String, Object>();
			info.put("roomId", room.getRoomId());
			JSONObject result = getJSONObj(interfaceId, 1, info);
			ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
			se.write(pd);
			//MessageFunctions.interface_100107(se, Cnst.EXIST_TYPE_DISSOLVE, playerList, null);
		} else {
			return;
		}

	}
}
