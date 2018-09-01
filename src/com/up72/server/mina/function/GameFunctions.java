package com.up72.server.mina.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Card;
import com.up72.game.dto.resp.ClubInfo;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.server.mina.bean.DissolveRoom;
import com.up72.server.mina.bean.ProtocolData;
import com.up72.server.mina.main.MinaServerManager;
import com.up72.server.mina.utils.RoomRecordUtil;
import com.up72.server.mina.utils.StringUtils;
import com.up72.server.mina.utils.redis.RedisUtil;
import com.up72.server.mina.utils.dcuse.JieSuan;

/**
 * Created by Administrator on 2017/7/13. 游戏中
 */

public class GameFunctions extends TCPGameFunctions {
	final static Object object = new Object();

	/**
	 * 用户点击准备，用在小结算那里，
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100200(IoSession session, Map<String, Object> readData) {
		logger.I("准备,interfaceId -> 100200");

		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		String userIdStr = (String) session.getAttribute(Cnst.USER_SESSION_USER_ID);
		Long userId = Long.valueOf(userIdStr);
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		
		Player currentPlayer = RedisUtil.getPlayerByUserId(userIdStr, cid);
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(currentPlayer.getRoomId()), cid);
		// Player currentPlayer = null;
		List<Player> players = RedisUtil.getPlayerList(room, cid);

		if (room.getState() == Cnst.ROOM_STATE_GAMIING) {
			return;
		}

		if (room.getLastNum() < 1) {
			return;// 没有次数了 不能准备了
		}

		if (currentPlayer == null || currentPlayer.getPlayStatus() == Cnst.PLAYER_STATE_PREPARED) {
			return;
		}

		
		boolean checkFirst = true;
		if (room.getLastNum() == room.getCircleNum() && userId.equals(room.getCreateId())) {
			// 第一局房主必须最后准备
			int num = 0;
			for (Player p : players) {
				if (p == null)
					continue;
				if (p.getUserId().equals(userId))
					continue;
				if (p.getPlayStatus().equals(Cnst.PLAYER_STATE_PREPARED)) {
					++num;
				}
			}

			if (num < 2) {
				// 房主必须最后准备
				checkFirst = false;
			}
		}

		//if (checkFirst) {
			currentPlayer.initPlayer(currentPlayer.getRoomId(), Cnst.PLAYER_STATE_PREPARED, currentPlayer.getScore());
			RedisUtil.setPlayerByUserId(currentPlayer, cid);

			players = RedisUtil.getPlayerList(room, cid);

			for (int i = 0; i < room.getPlayerIds().size(); i++) {
				if (room.getPlayerIds().get(i) == null)
					continue;
				if (room.getPlayerIds().get(i).longValue() != userId.longValue())
					continue;

				RedisUtil.updateRedisData(room, null, cid);
				break;
			}
		//}

		boolean allPrepared = true;

		for (Player p : players) {
			if (p == null || !p.getPlayStatus().equals(Cnst.PLAYER_STATE_PREPARED)) {
				allPrepared = false;
				break;
			}
		}

		boolean isFirst = false;//FIXME
		if (allPrepared && players != null && players.size() == 3) {
			if (room.getLastNum() == room.getCircleNum()) {//最后一局的时候写入文件
				createRecord(room, players, cid);
				isFirst = true;
			}
			startGame(room, players, cid);
		}
		Map<String, Object> info = new HashMap<String, Object>();
		List<Map<String, Object>> userInfo = new ArrayList<Map<String, Object>>();
		// old
		int position = -1;
		for (Player p : players) {
			++position;
			if (p == null)
				continue;
			Map<String, Object> i = new HashMap<String, Object>();
			i.put("userId", p.getUserId());
			i.put("playStatus", p.getPlayStatus());

			userInfo.add(i);
		}
		info.put("userInfos", userInfo);
		Map<String, Object> roominfo = new HashMap<String, Object>();
		roominfo.put("state", room.getState());
		roominfo.put("playStatus", room.getPlayStatus());
		info.put("roomInfos", roominfo);
		if (!checkFirst)
			info.put("creatorInfo", 1);

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

		if (isFirst && room.getExtraType() == Cnst.ROOM_EXTRA_TYPE_2) {
			// 如果是代开 发消息给代开房主
			Player p = null;
			if (room.getPlayerIds().contains(room.getCreateId())) {
				for (Player tmpP : players) {
					if (tmpP.getUserId().longValue() == room.getCreateId().longValue()) {
						p = tmpP;
						break;
					}
				}
			} else
				p = RedisUtil.getPlayerByUserId(room.getCreateId() + "", cid);

			if (p != null) {

				JSONObject jsonObject = new JSONObject();
				jsonObject.put("roomSn", room.getRoomId());
				JSONObject result1 = getJSONObj(100115, 1, jsonObject);
				ProtocolData pd1 = new ProtocolData(interfaceId, result1.toJSONString());
				IoSession se = MinaServerManager.tcpServer.getSessions().get(p.getSessionId());
				if (se != null && se.isConnected()) {
					se.write(pd1);
				}
			}

		}
	}

	/**
	 * 开局发牌
	 * 
	 * @param roomId
	 */
	public static void startGame(RoomResp room, List<Player> players, String cid) {

		room.setXiaoJuNum(room.getXiaoJuNum() == null ? 0 : room.getXiaoJuNum() + 1);
		room.setXjst(System.currentTimeMillis());

		// 初始化房间手牌
		List<Card> cards = new ArrayList<Card>();
		if (room.getRoomType() == Cnst.ROOM_TYPE_1) {
			for (int i = 0; i < Cnst.CARD_ARRAY_1.length; i++) {
				cards.add(new Card(Cnst.CARD_ARRAY_1[i]));
			}
		} else {
			if (room.getLaiZi().intValue() != 0) {
				for (int i = 0; i < Cnst.CARD_ARRAY_2.length; i++) {
					cards.add(new Card(Cnst.CARD_ARRAY_2[i]));
				}
			} else {
				// 最后一张癞子不给
				for (int i = 0; i < Cnst.CARD_ARRAY_2.length - 1; i++) {
					cards.add(new Card(Cnst.CARD_ARRAY_2[i]));
				}
			}
		}

		for (Player p : players) {
			p.setCurrentCardList(new ArrayList<Card>());
		}

		boolean setDizhu = false;
		if (room.getDizhuType() == Cnst.DIZHU_TYPE_2) {
			// 轮有产生地主
			if (room.getLastJiaoDiZhu() != null) {
				int dizhuIdx = -1;
				for (int i = 0; i < room.getPlayerIds().size(); i++) {
					if (room.getLastJiaoDiZhu().equals(room.getPlayerIds().get(i))) {
						dizhuIdx = i;
						break;
					}
				}

				if (dizhuIdx != -1) {
					dizhuIdx = (dizhuIdx + 1) % room.getPlayerIds().size();
					setDizhu = true;
					room.setFirstTakePaiUser(players.get(dizhuIdx).getUserId());
				}
			}
			//优先上一局赢得做地主
		} else if (room.getDizhuType() == Cnst.DIZHU_TYPE_3) {
			if (room.getLastWinner() != null) {
				setDizhu = true;
				room.setFirstTakePaiUser(room.getLastWinner());
			}
		}

		int firstIdx= 0;
		if (!setDizhu) {
			// 设置第一个叫地主的人 并且给他默认动作
			firstIdx = (int) (Math.random() * players.size());
			room.setFirstTakePaiUser(players.get(firstIdx).getUserId());
		}

		room.setLastJiaoDiZhu(room.getFirstTakePaiUser());

		int liuPai = 3;
		if (room.getRoomType() == Cnst.ROOM_TYPE_2 && room.getLaiZi().intValue() != 0){
			liuPai = 4;
		}
		 //直接发牌 最后剩三或者4张
		 //每人随机发一个张
		 for (int i = 0; cards.size() > liuPai; i++) {
		 Card card = cards.get((int) (Math.random() * (cards.size())));
		 int userIdx = (firstIdx + i) % 3;
		 Player player = players.get(userIdx);
		 player.getCurrentCardList().add(card);
		 cards.remove(card);
		 }

		room.initRoom();
		// 房间设置为游戏环节
		room.setState(Cnst.ROOM_STATE_GAMIING);
		// 设置游戏环节具体一步 可以直接设置开始阶段
		room.setPlayStatus(Cnst.ROOM_PLAYSTATE_JIAODIZHU);
		room.setLastNum(room.getLastNum() - 1);

		room.setCurrentCardList(cards);

		List<Integer> actions = new ArrayList<Integer>();
		actions.add(Cnst.ACTION_DIZHU0);

		if (room.getDaPaiJiaoMan().intValue() == Cnst.ROOM_JIAO_TYPE_2 && isHaveDaPai(RedisUtil.getPlayerByUserId(room.getFirstTakePaiUser() + "", cid))) {
			// 双王或者四个2必须叫满
			actions.add(Cnst.ACTION_DIZHU3);
		} else if (RoomResp.getRealGiveUpTime(room) > 0) {
			actions.add(Cnst.ACTION_DIZHU3);
		} else {
			actions.add(Cnst.ACTION_DIZHU1);
			actions.add(Cnst.ACTION_DIZHU2);
			actions.add(Cnst.ACTION_DIZHU3);
		}

		room.setCurrentUserAction(actions);
		room.setCurrentUserId(room.getFirstTakePaiUser());

		// 更新 room players
		RedisUtil.setObject(Cnst.get_REDIS_PREFIX_ROOMMAP(cid).concat(room.getRoomId() + ""), room, Cnst.ROOM_LIFE_TIME_COMMON);
		for (Player p : players) {
			p.setPlayStatus(Cnst.PLAYER_STATE_GAME);
			
		}
		// 如果第解一次添加到定时散任务 并且保存到DB
		if (room.getXiaoJuNum() == 0) {
			notifyDisRoomTask(room, Cnst.DIS_ROOM_TYPE_1);
			if(String.valueOf(room.getRoomId()).length()==7){
				/*
				 * 俱乐部或与人数   key: cid+clubId+当天0点的时间   value：userId的集合
				 */
				//获取当天活跃总人数
				Long timesmorning = StringUtils.getTimesmorning();
				Long scard = RedisUtil.scard(Cnst.get_REDIS_CLUB_ACTIVE_PERSON(cid).concat(room.getClubId()+"_").concat(timesmorning+""));
				int dieTime=Cnst.REDIS_CLUB_DIE_TIME;

				if(scard==null || scard==0l){//当天没人,有人最少为5
					//创建一个并设置过期时间(其中1l为假数据)
					RedisUtil.sadd(Cnst.get_REDIS_CLUB_ACTIVE_PERSON(cid).concat(room.getClubId()+"_").concat(timesmorning+""),1l,dieTime);
					for (Long userId : room.getPlayerIds()) {
						RedisUtil.sadd(Cnst.get_REDIS_CLUB_ACTIVE_PERSON(cid).concat(room.getClubId()+"_").concat(timesmorning+""),userId,null);
					}
				}else{//有人
					for (Long userId : room.getPlayerIds()) {
						RedisUtil.sadd(Cnst.get_REDIS_CLUB_ACTIVE_PERSON(cid).concat(room.getClubId()+"_").concat(timesmorning+""),userId,null);
					}
				}
				
				//今日俱乐部局数   --昨日和前日
				Integer clubId = room.getClubId();
				Integer todayJuNum = RedisUtil.getObject(Cnst.get_REDIS_CLUB_TODAYKAI_NUM(cid).concat(clubId+"_").concat(timesmorning+""), Integer.class);
				if(todayJuNum==null || todayJuNum==0){
					RedisUtil.setObject(Cnst.get_REDIS_CLUB_TODAYKAI_NUM(cid).concat(clubId+"_").concat(timesmorning+""),1,dieTime);
				}else{
					RedisUtil.setObject(Cnst.get_REDIS_CLUB_TODAYKAI_NUM(cid).concat(clubId+"_").concat(timesmorning+""),1+todayJuNum,null);
				}
				List<Long> playerIds = room.getPlayerIds();
				//今日玩家局数  --保存一天
				Integer juNum=null;
				for (Long playerId : playerIds) {
					//key clubId+userId+今天早上时间
					juNum = RedisUtil.getObject(Cnst.getREDIS_CLUB_TODAYJUNUM_ROE_USER(cid).concat(clubId+"_").concat(playerId+"_").concat(timesmorning+""), Integer.class);
					if(juNum==null || juNum==0){
						RedisUtil.setObject(Cnst.getREDIS_CLUB_TODAYJUNUM_ROE_USER(cid).concat(clubId+"_").concat(playerId+"_").concat(timesmorning+""),1, Cnst.REDIS_CLUB_PLAYERJUNUM_TIME);
					}else{
						RedisUtil.setObject(Cnst.getREDIS_CLUB_TODAYJUNUM_ROE_USER(cid).concat(clubId+"_").concat(playerId+"_").concat(timesmorning+""),juNum+1,Cnst.REDIS_CLUB_PLAYERJUNUM_TIME );
					}
				}
				
				//移除俱乐部未开房间
				RedisUtil.hdel(Cnst.get_REDIS_CLUB_ROOM_LIST(cid), String.valueOf(room.getRoomId()));
				addRoomToClubDB(room);
			}else{
				addRoomToDB(room);
			}			
		}
		RedisUtil.setPlayersList(players, cid);

		{
			// 记录玩家牌面信息
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("interfaceId", "2");
			JSONArray a1 = new JSONArray();
			for (Card c : players.get(0).getCurrentCardList()) {
				JSONObject json = Card.getReturnJson(c);
				a1.add(json);
			}

			// FIXME 缩写
			map.put("firstUserPais", a1);

			JSONArray a2 = new JSONArray();
			for (Card c : players.get(1).getCurrentCardList()) {
				JSONObject json = Card.getReturnJson(c);
				a2.add(json);
			}
			map.put("secondUserPais", a2);

			JSONArray a3 = new JSONArray();
			for (Card c : players.get(2).getCurrentCardList()) {
				JSONObject json = Card.getReturnJson(c);
				a3.add(json);
			}
			map.put("thirdUserPais", a3);

			JSONArray a4 = new JSONArray();
			for (Card c : room.getCurrentCardList()) {
				JSONObject json = Card.getReturnJson(c);
				a4.add(json);
			}
			map.put("diPais", a4);

			map = getNewMap(map);
			addRecord(room, map, cid);
		}
	}

	private static boolean isHaveDaPai(Player player) {
		int wang = 0;
		int two = 0;
		for (Card card : player.getCurrentCardList()) {
			if (card.getSymble() == 14)
				++wang;
			if (card.getSymble() == 2)
				++two;
		}
		if (wang > 1 || two > 3) {
			return true;
		}
		return false;
	}

	/**
	 * 请求发牌
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100207(IoSession session, Map<String, Object> readData) {
		logger.I("请求发牌,interfaceId -> 100207");

		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomSn"));

		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId), cid);

		Player player = RedisUtil.getPlayerByUserId(userId + "", cid);
		Integer playStatus = room.getPlayStatus();

		JSONObject roomInfos = new JSONObject();// 封装房间信息
		JSONObject userInfos = new JSONObject();// 封装玩家信息
		JSONArray paiInfos = new JSONArray();// 封装当前手牌

		for (Card card : player.getCurrentCardList()) {
			paiInfos.add(card.getOrigin());
		}


		JSONObject info = new JSONObject();
		info.put("reqState", Cnst.REQ_STATE_1);
		userInfos.put("playStatus", player.getPlayStatus());
		userInfos.put("state", player.getState());
		userInfos.put("paiInfos", paiInfos);

		info.put("userInfos", userInfos);

		roomInfos.put("action", room.getCurrentUserAction());
		roomInfos.put("actionPlayer", room.getCurrentUserId());
		roomInfos.put("playStatus", playStatus);
		roomInfos.put("state", room.getState());
		roomInfos.put("lastNum", room.getLastNum());
		roomInfos.put("giveUpTime", RoomResp.getRealGiveUpTime(room));
		// roomInfos.put("otherPaiInfos", otherPais);
		info.put("roomInfos", roomInfos);

		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);

	}

	/**
	 * 玩家动作
	 * 会用100104接口把数据发给前端
	 * @param session
	 * @param readData
	 */
	public static void interface_100202(IoSession session, Map<String, Object> readData) throws Exception {
		logger.I("准备,interfaceId -> 100202");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomSn"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		Integer action = StringUtils.parseInt(readData.get("action"));// 动作

		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		if (action == null)
			return;
		Integer dizhuFen = null;

		RoomResp room = RedisUtil.getRoomRespByRoomId(roomId + "", cid);

		
		// 叫地主环节的验证
		if (action.intValue() == Cnst.ACTION_DIZHU1 || action.intValue() == Cnst.ACTION_DIZHU2 || action.intValue() == Cnst.ACTION_DIZHU3) {
			dizhuFen = action - 1;
		} else
			dizhuFen = 0;

		List<Card> chuPai = null;
		if (action.intValue() == Cnst.ACTION_CHUPAI) {
			JSONArray array = (JSONArray) readData.get("actionExtra");
			logger.I("array 的数据:"+JSONArray.toJSONString(array));
			JSONArray newArray = new JSONArray();
			chuPai = new ArrayList<Card>();
			Iterator<Object> iterator = array.iterator();
			while (iterator.hasNext()) {
				JSONObject json = (JSONObject) iterator.next();
				Card card = new Card(json.getIntValue(Cnst.ROUTE_MAP.get("origin")));
				logger.I("card的值:"+card);
				if (json.containsKey(Cnst.ROUTE_MAP.get("laizi")) && Card.isLaizi(card)) {
					card.setLaizi(json.getIntValue(Cnst.ROUTE_MAP.get("laizi")));
				}
				chuPai.add(card);
				newArray.add(getNewObj(json));
			}
			// 方便后面记录使用
			readData.put("actionExtra", newArray);
		}
		
		//闪斗功能,抢出功能
		List<Long> playerIds = room.getPlayerIds();
		Player p = null;
		Player nongMin = null;
		Card nongMinCard = null;
		boolean checkOnlyPai = false;
		for (Long id : playerIds) {
			nongMin = RedisUtil.getPlayerByUserId(id + "", cid);
			if( chuPai != null && nongMin.getCurrentCardList().size() == 1 && room.getRoomType() == Cnst.ROOM_TYPE_2 && chuPai.size() == 1){ //必须是闪斗
				checkOnlyPai = true;
				nongMinCard = nongMin.getCurrentCardList().get(0);
				break;
			}
		}

		Player diZhu = null;
		boolean diZhuOnlyPai = false;
		//如果地主剩一张牌.就不能抢出了.
		for (Long id : playerIds) {
		  if(id.equals(room.getDizhu())){
			  diZhu = RedisUtil.getPlayerByUserId(id + "", cid);
			  int size = diZhu.getCurrentCardList().size();
			  if(size == 1){
				  diZhuOnlyPai = true;
			  }
		  }
		}
			
		
		//地主是最后出牌人,只出了一张牌,并且农民剩余一张且大于地主出牌,就直接出牌.
		if (action.intValue() == Cnst.ACTION_CHUPAI && userId.equals(room.getDizhu()) && checkOnlyPai && !diZhuOnlyPai) {
			for (Long long1 : playerIds) {
				p = RedisUtil.getPlayerByUserId(long1 + "", cid);
				if(p.getUserId().equals(room.getDizhu())){
						logger.I("card的值:"+chuPai.get(0));
						Card card = chuPai.get(0);
						if( !Card.isLaizi(nongMinCard)){
							if(computer(nongMinCard,card)){//前面的是否比后面的大,并且不是癞子
								//记录出牌信息
								chuPai(room, p, action, chuPai, readData, cid);
								
								logger.I("条件成熟!!!");
								JSONObject json = new JSONObject();
								json.put("continue", 0);
								json.put("lastAction", 8);//最后一个人的动作
								//json.put("lastUserId", 225912);//最后出牌的玩家
								json.put("lastUserId", room.getDizhu());//最后出牌的玩家
//							for (Long id : playerIds) {
//								if(!id.equals(room.getDizhu()) && !id.equals(nongMin.getUserId()) ){
//									json.put("lastUserId", id);//最后出牌的玩家,应该直接跳过的玩家ID
//								}
//							}
								
								json.put("lastUserPaisNum", p.getCurrentCardList().size());//当前出牌人剩余的牌
								int[] a = new int[]{8};
								json.put("currentUserAction", a);// 直接给8就是出牌
								//json.put("currentUserId",310947);//要出最后一个牌的农民ID
								json.put("currentUserId",nongMin.getUserId());//要出最后一个牌的农民ID
								
								
								json.put("realLastAction", 8);//最后的动作
								//json.put("realLastUser", 978290); //最后出牌人的ID
								json.put("realLastUser", room.getDizhu()); //最后出牌人的ID(应该是地主的ID)
								List<Card> lastChuPai = room.getRealLastChuPai();
								List<JSONObject> list = new ArrayList<JSONObject>();
								list.add(Card.getReturnJson(card));
								json.put("realLastChuPai", list);
								
								if ((room.getPlayStatus() != null && room.getPlayStatus().intValue() == Cnst.ROOM_PLAYSTATE_END)) {
									readData.put("mul", getMulJsonObject(room));
									json.put("mul", getMulJsonObject(room));
								}
								json.put("playStatus", room.getPlayStatus());
								json.put("state", room.getState().intValue() == Cnst.ROOM_STATE_YJS ? Cnst.ROOM_STATE_XJS : room.getState());
								RedisUtil.updateRedisData(null, p, cid);
								RedisUtil.updateRedisData(room, null, cid);
								MessageFunctions.interface_100104(json, room, 100104, cid);
								return;
							}
						}
					}
					
				}
			}
		
		
		// 如果不算游戏中不允许这样操作
		if (room.getState() != Cnst.ROOM_STATE_GAMIING) {
			return;
		}

		Player player = RedisUtil.getPlayerByUserId(userId + "", cid);

		

		// 在开始 要求所有人明牌的结算 不需要校验这个玩家是否有这个动作
//		if (room.getCurrentUserAction() == null || !room.getCurrentUserAction().contains(action)) {
//			// 没有这个人的操作
//			return;
//		}
//
//		if (room.getCurrentUserId() == null || !room.getCurrentUserId().equals(userId)) {
//			// 没有这个人的操作
//			return;
//		}
		// ======room
		Integer playStatus = room.getPlayStatus();// 房间状态
		if (playStatus == Cnst.ROOM_PLAYSTATE_CHUPAI ) {
			handlerChuPai(room, player, action, chuPai, readData, cid);
		} else if (playStatus == Cnst.ROOM_PLAYSTATE_JIABEI) {
			handlerJiaBei(room, player, action, readData, cid);
		} else if (playStatus == Cnst.ROOM_PLAYSTATE_JIAODIZHU) {
			// 叫地主环节
			handlerJiaoDizhu(room, player, dizhuFen, action, readData, cid);
		} else if (playStatus == Cnst.ROOM_PLAYSTATE_MINGPAI) {
			handlerMingPai(room, player, action, readData, cid);
		}
	}

	/**
	 * 是否明牌
	 * @param room
	 * @param player
	 * @param action
	 * @param readData
	 * @param cid
	 */
	private static void handlerMingPai(RoomResp room, Player player, Integer action, Map<String, Object> readData, String cid) {

		room.setLastUserId(player.getUserId());
		room.setLastAction(action);
		room.setLastActionExtra(null);

		for (int i = 0; i < room.getPlayerIds().size(); i++) {
			if (room.getPlayerIds().get(i).longValue() == player.getUserId().longValue()) {
				if (action.intValue() == Cnst.ACTION_MINGPAI)
					room.getMingPaiInfo().set(i, 1);
				else
					room.getMingPaiInfo().set(i, 0);
				break;
			}
		}
		Long nextUid = null;
		if (room.getMingPaiType() == Cnst.MINGPAY_TYPE_3) {
			boolean beginFind = false;
			for (int i = 0; i < room.getPlayerIds().size() * 2; i++) {
				if (!beginFind && room.getMingPaiInfo().get(i % room.getPlayerIds().size()) != null) {
					beginFind = true;
					continue;
				}

				if (beginFind && room.getMingPaiInfo().get(i % room.getPlayerIds().size()) == null) {
					nextUid = room.getPlayerIds().get(i % room.getPlayerIds().size());
					break;
				}
			}
		}

		if (nextUid != null) {
			room.setCurrentUserId(nextUid);
			ArrayList<Integer> arrayList = new ArrayList<Integer>();
			arrayList.add(Cnst.ACTION_BUMINGPAI);
			arrayList.add(Cnst.ACTION_MINGPAI);
			room.setPlayStatus(Cnst.ROOM_PLAYSTATE_MINGPAI);
			room.setCurrentUserAction(arrayList);
		} else if (room.getMulType() == Cnst.ROOM_MUL_TYPE_2) {
			// 农民加倍
			int dizhuIdx = 0;
			for (; dizhuIdx < 3; dizhuIdx++) {
				if (room.getPlayerIds().get(dizhuIdx).longValue() == room.getDizhu().longValue())
					break;
			}
			// 找到地主之后的第一个农民
			room.setPlayStatus(Cnst.ROOM_PLAYSTATE_JIABEI);
			int nextUseIdx = (dizhuIdx + 1) % 3;
			ArrayList<Integer> arrayList = new ArrayList<Integer>();
			arrayList.add(Cnst.ACTION_BUJIABEI);
			arrayList.add(Cnst.ACTION_JIABEI);
			room.setCurrentUserAction(arrayList);
			room.setCurrentUserId(room.getPlayerIds().get(nextUseIdx));
		} else if (room.getMulType() == Cnst.ROOM_MUL_TYPE_3) {
			// 直接让地主加倍
			room.setPlayStatus(Cnst.ROOM_PLAYSTATE_JIABEI);
			ArrayList<Integer> arrayList = new ArrayList<Integer>();
			arrayList.add(Cnst.ACTION_BUJIABEI);
			arrayList.add(Cnst.ACTION_JIABEI);
			room.setCurrentUserAction(arrayList);
			room.setCurrentUserId(room.getDizhu());
		} else {
			// 直接进入出牌环节
			room.setPlayStatus(Cnst.ROOM_PLAYSTATE_CHUPAI);
			ArrayList<Integer> arrayList = new ArrayList<Integer>();
			arrayList.add(Cnst.ACTION_CHUPAI);
			room.setCurrentUserAction(arrayList);
			room.setCurrentUserId(room.getDizhu());
		}

		RedisUtil.updateRedisData(room, null, cid);
		//readData.put("difen", room.getDiFen()); //回放底分
		Map record = getNewMap(readData);
		addRecord(room, record, cid);

		// 返回消息 所有人明牌情况
		JSONObject json = new JSONObject();
		json.put("playStatus", room.getPlayStatus());
		if (room.getCurrentUserId() != null) {
			json.put("currentUserAction", room.getCurrentUserAction());
			json.put("currentUserId", room.getCurrentUserId());
		}
		json.put("lastAction", action);
		json.put("lastUserId", player.getUserId());

		if (action.intValue() != Cnst.ACTION_BUMINGPAI)
			json.put("mul", getMulJsonObject(room));

		JSONObject jsonObject = new JSONObject();

		JSONObject pais = new JSONObject();

		for (int i = 0; i < room.getMingPaiInfo().size(); i++) {

			if (room.getMingPaiInfo().get(i) == null)
				continue;
			jsonObject.put(room.getPlayerIds().get(i) + "", room.getMingPaiInfo().get(i));

			if (room.getMingPaiInfo().get(i).intValue() == 0)
				continue;

			Player p = null;
			if (room.getPlayerIds().get(i).longValue() == player.getUserId().longValue()) {
				p = player;
			} else
				p = RedisUtil.getPlayerByUserId(room.getPlayerIds().get(i) + "", cid);

			JSONArray arr = new JSONArray();

			for (Card c : p.getCurrentCardList()) {
				arr.add(c.getOrigin());
			}
			pais.put(room.getPlayerIds().get(i) + "", arr);
		}
		json.put("mingPaiInfo", jsonObject);
		json.put("mingPais", pais);

		MessageFunctions.interface_100104(json, room, 100104, cid);

	}

	
	/**
	 * 农民直接出最后一张牌
	 * @param room
	 * @param player
	 * @param action
	 * @param chuPai
	 * @param readData
	 * @param cid
	 */
	private static void chuPai(RoomResp room, Player player, Integer action, List<Card> chuPai, Map<String, Object> readData, String cid) {
	
		int idx = 0;
		Map newMap = getNewMap(readData);
		addRecord(room, newMap, cid);

		ArrayList<Card> cards = new ArrayList<Card>();
		for (Card card : chuPai) {
			logger.I("出牌的信息1:"+card.getOrigin());
			Card card2 = new Card(card.getOrigin());
			cards.add(card2);
		}
		room.getCards().set(idx, cards);
		{
			// 把上一家的动作放到上一手牌里面
			int lastIdx = (idx - 1 + 3) % 3;
			room.getLastCards().set(lastIdx, room.getCards().get(lastIdx));
		}

		//移除手牌
		for (Card card : chuPai) {
			player.getCurrentCardList().remove(card);
		}
		
		//是否春天
		if (!room.getDizhu().equals(player.getUserId())){
			room.setNongminChu(1);//农民出牌次数.非0就不是春天
		}else{
			room.setDizhuChu(room.getDizhuChu() + 1);//次数不超过2次就算春天
		}

	}
	/**
	 * 出牌的方法
	 * @param room
	 * @param player
	 * @param action
	 * @param chuPai
	 * @param readData
	 * @param cid
	 */
	private static void handlerChuPai(RoomResp room, Player player, Integer action, List<Card> chuPai, Map<String, Object> readData, String cid) {
		if (chuPai != null) {
			// 真实牌面是否有重复的问题
			HashMap<Integer, Integer> hashMap = new HashMap<Integer, Integer>();
			boolean check = true;
			for (Card card : chuPai) {
				if (hashMap.containsKey(card.getOrigin())) {
					check = false;
					break;
				}
				hashMap.put(card.getOrigin(), 1);
			}

			if (!check) {
				// 牌有问题;
				sendChuPaiInfo(player.getSessionId(), 3);
				return;
			}
			// 检查真实牌面玩家是否有这张牌的问题
			for (Card card : chuPai) {
				if (!player.getCurrentCardList().contains(card)) {
					sendChuPaiInfo(player.getSessionId(), 3);
					return;
				}
			}
		}
		if (action.intValue() == Cnst.ACTION_CHUPAI) {
			if (chuPai == null || chuPai.isEmpty()) {
				sendChuPaiInfo(player.getSessionId(), 3);
				return;
			}
		}

		//这段代码可以得到当前playerid在room.PlayerIds中的位置.如0,1,或2.
		int idx = 0;
		for (; idx < room.getPlayerIds().size(); idx++) {
			if (room.getPlayerIds().get(idx).longValue() == player.getUserId().longValue()) {
				break;
			}
		}

		boolean isZhaDan = false;
		if (action.equals(Cnst.ACTION_GUO)) {
			// 过
			room.setLastAction(action);
			room.setLastActionExtra(null);
			room.setLastUserId(player.getUserId());
			room.getCards().set(idx, new ArrayList<Card>());
			{
				// 把上一家的动作放到上一手牌里面
				int lastIdx = (idx - 1 + 3) % 3;
				room.getLastCards().set(lastIdx, room.getCards().get(lastIdx));
			}

			int nextIdx = (idx + 1) % 3; //这是一下要出牌的人的ID 在room.ids中的位置

			//记录回放文件
			Map newMap = getNewMap(readData);
			addRecord(room, newMap, cid);

			boolean isWin = true;
			// 下个人出牌或者过
			for (int i = 0; i < 3; i++) {
				int tmpnextIdx = (nextIdx + i) % 3;

				if (room.getPlayerIds().get(tmpnextIdx).equals(room.getRealLastUserId())) {
					room.setRealLastAction(null);
					room.setRealLastChuPai(null);
					room.setRealLastUserId(null);
				}

				//如果是闪斗并且是癞子局
				if (room.getRoomType() == Cnst.ROOM_TYPE_2 && room.getLaiZi() == 1) {
					Player tmpPlayer = RedisUtil.getPlayerByUserId(room.getPlayerIds().get(tmpnextIdx) + "", cid);
					if (isOnlyLaiZi(tmpPlayer)) {
						room.getCards().set(tmpnextIdx, new ArrayList<Card>());//如果对应的size是空说明过了
						continue;
					}
				}
				room.setCurrentUserId(room.getPlayerIds().get(tmpnextIdx));
				ArrayList<Integer> arrayList = new ArrayList<Integer>();//当前玩家动作集合
				arrayList.add(Cnst.ACTION_CHUPAI);
				if (!room.getPlayerIds().get(tmpnextIdx).equals(room.getRealLastUserId()) && room.getRealLastUserId() != null)
					arrayList.add(Cnst.ACTION_GUO);
				room.setCurrentUserAction(arrayList);
				isWin = false;
				break;
			}
			if (isWin) {
				room.setCurrentUserId(null);
				room.setCurrentUserAction(null);
				if (room.getDizhu().equals(player.getUserId()))
					room.setWinner(1);
				else
					room.setWinner(0);

				room.setLastWinner(player.getUserId());
				RedisUtil.updateRedisData(null, player, cid);
				JieSuan.xiaoJieSuan(room, cid);
				player = RedisUtil.getPlayerByUserId(player.getUserId() + "", cid);
			}
			
		} else {//出牌
			if (room.getRoomType() == Cnst.ROOM_TYPE_2 && room.getLaiZi() == 1) {//是闪斗,可以是癞子
				// 癞子不能单出 而且癞子是合法的癞子
				int realCard = 0;// 不是癞子的排数
				for (Card card : chuPai) {
					if (Card.isLaizi(card)) {
						if (!Card.isLegalLaizi(card)) {
							return;// 有牌不是合法的癞子
						}
					} else {
						++realCard;
						card.setLaizi(0);
					}
				}
				if (realCard == 0) {
					// 都是癞子牌
					sendChuPaiInfo(player.getSessionId(), 3);
					return;
				}

				// 判断整个癞子有没有重复变得
				HashMap<Integer, Integer> hashMap = new HashMap<Integer, Integer>();
				boolean check = true;
				for (Card card : chuPai) {
					int tmp = card.getLaizi() != 0 ? card.getLaizi() : card.getOrigin();
					if (hashMap.containsKey(tmp)) {
						check = false;
						break;
					}
					hashMap.put(tmp, 1);
				}
				if (!check) {
					// 癞子有重复变牌的问题
					sendChuPaiInfo(player.getSessionId(), 3);
					return;
				}
			} else {// 不能使用癞子
				for (Card card : chuPai) {
					card.setLaizi(0);
				}
			}

			Collections.sort(chuPai);

			// 所有出的牌都已经检查完了基本牌面 现在开始检查牌型问题
			List<Card> realLastChuPai = room.getRealLastChuPai();
			logger.I("realLastChuPai: "+realLastChuPai);
			int realLastCardType = Cnst.CARD_TYPE_DEFAULT; //没有牌型
			List<ArrayList<Card>> last = null;
			if (realLastChuPai == null || realLastChuPai.isEmpty()) {
				realLastCardType = Cnst.CARD_TYPE_DEFAULT;
			} else {
				Collections.sort(realLastChuPai);
				last = handlerCard(realLastChuPai);
				realLastCardType = getCardType(last, room.getRoomType(), room.getCan4take2());
			}
			List<ArrayList<Card>> current = handlerCard(chuPai);
			logger.I("current: "+current);
			int cardType = getCardType(current, room.getRoomType(), room.getCan4take2());//获取牌型,如,单,对,链子
			if (cardType == Cnst.CARD_TYPE_DEFAULT) {
				// 牌型有问题 不允许出牌
				sendChuPaiInfo(player.getSessionId(), 1);
				return;
			}
			if (realLastCardType == Cnst.CARD_TYPE_DEFAULT) {
				// 直接出牌了 放过去统一处理
			} else if (cardType != realLastCardType) {
				if (cardType > Cnst.CARD_TYPE_ZHANDAN) {
					// 必定有问题
					sendChuPaiInfo(player.getSessionId(), 2);
					return;
				}

				if (cardType < realLastCardType) {
					// 可以管住这个人 出牌成功 放过去统一处理
				} else {
					// 必定有问题
					sendChuPaiInfo(player.getSessionId(), 2);
					return;
				}

			} else {
				// 俩人牌型一样 判断是否能管住
				boolean canBig = computer(cardType, current, last);
				if (!canBig) {
					sendChuPaiInfo(player.getSessionId(), 2);
					return;// 管不住 有问题
				}
				// 放过去统一处理
			}

			if (cardType == Cnst.CARD_TYPE_ZHANDAN || cardType == Cnst.CARD_TYPE_HUOJIAN) {
				isZhaDan = true;
				if (cardType == Cnst.CARD_TYPE_ZHANDAN)
					room.getZhandans().add(Card.getRealSymble(current.get(3).get(0)));
				else
					room.getZhandans().add(Card.getRealSymble(current.get(0).get(0)));
				room.getZhandanInfo().get(idx).add(room.getZhandans().get(room.getZhandans().size() - 1));
			}
			//记录回放文件
			Map newMap = getNewMap(readData);
			addRecord(room, newMap, cid);

			room.setLastAction(action);
			room.setLastActionExtra(null);
			room.setLastUserId(player.getUserId());
			room.setRealLastAction(action);
			room.setRealLastChuPai(chuPai);
			room.setRealLastUserId(player.getUserId());
			if (!room.getDizhu().equals(player.getUserId()))
				room.setNongminChu(1);//农民出牌次数.非0就不是春天
			else
				room.setDizhuChu(room.getDizhuChu() + 1);//次数不超过2次就算春天

			ArrayList<Card> cards = new ArrayList<Card>();
			for (Card card : chuPai) {
				logger.I("出牌的信息1:"+card.getOrigin());
				Card card2 = new Card(card.getOrigin());
				card2.setLaizi(card.getLaizi());
				cards.add(card2);
			}
			room.getCards().set(idx, cards);
			{
				// 把上一家的动作放到上一手牌里面
				int lastIdx = (idx - 1 + 3) % 3;
				room.getLastCards().set(lastIdx, room.getCards().get(lastIdx));
			}

			for (Card card : chuPai) {
				player.getCurrentCardList().remove(card);
			}

			if (player.getCurrentCardList().isEmpty()) { //手牌数量为空,就是赢了.
				room.setCurrentUserAction(null);
				room.setCurrentUserId(null);

				if (room.getDizhu().equals(player.getUserId()))
					room.setWinner(1);
				else
					room.setWinner(0);

				room.setLastWinner(player.getUserId());

				RedisUtil.updateRedisData(null, player, cid);
				JieSuan.xiaoJieSuan(room, cid);
				player = RedisUtil.getPlayerByUserId(player.getUserId() + "", cid);
			} else {//不为空
				boolean isWin = true;
				// 下个人出牌或者过
				for (int i = 1; i < 4; i++) {
					int nextIdx = (idx + i) % 3; //下一个出牌人的id
					if (room.getRoomType() == Cnst.ROOM_TYPE_2 && room.getLaiZi() == 1) {
						Player tmpPlayer = RedisUtil.getPlayerByUserId(room.getPlayerIds().get(nextIdx) + "", cid);
						if (isOnlyLaiZi(tmpPlayer)) //如果只剩一个癞子,不能再出牌.
							continue;
					}
					room.setCurrentUserId(room.getPlayerIds().get(nextIdx));//下一个要出牌人的id
					ArrayList<Integer> arrayList = new ArrayList<Integer>();
					arrayList.add(Cnst.ACTION_CHUPAI);
					if (room.getPlayerIds().get(nextIdx).longValue() != player.getUserId().longValue())
						arrayList.add(Cnst.ACTION_GUO);
					room.setCurrentUserAction(arrayList);
					isWin = false;
					break;
				}
				if (isWin) {
					room.setCurrentUserId(null);
					room.setCurrentUserAction(null);
					if (room.getDizhu().equals(player.getUserId()))
						room.setWinner(1);
					else
						room.setWinner(0);

					room.setLastWinner(player.getUserId());

					RedisUtil.updateRedisData(null, player, cid);
					JieSuan.xiaoJieSuan(room, cid);
					player = RedisUtil.getPlayerByUserId(player.getUserId() + "", cid);
				} else
					RedisUtil.updateRedisData(null, player, cid);
			}
		}

		// 返回的JSON
		//FIXME
		JSONObject json = new JSONObject();
		json.put("continue", 0);
		json.put("lastAction", room.getLastAction());
		json.put("lastUserId", room.getLastUserId());
		if (room.getLastAction().intValue() == Cnst.ACTION_CHUPAI) {
			List<Card> lastChuPai = room.getRealLastChuPai();
			List<JSONObject> list = new ArrayList<JSONObject>();
			for (Card c : lastChuPai) {
				list.add(Card.getReturnJson(c));
			}
			json.put("lastActionExtra", list);
		}

		//玩家剩余手牌
		Player tmpPlayer = null;
		int PNum =  room.getPlayerIds().size();
		
		for (int j = 0; j < PNum; j++) {
			tmpPlayer = RedisUtil.getPlayerByUserId(room.getPlayerIds().get(j) + "", cid);
			room.getPaiNumList().set(j, tmpPlayer.getCurrentCardList().size());
		}
		json.put("paiNumList",room.getPaiNumList());
		//上一个出牌人的牌的数量
		json.put("lastUserPaisNum", player.getCurrentCardList().size());
		json.put("currentUserAction", room.getCurrentUserAction());// 直接给8就是出牌
		json.put("currentUserId", room.getCurrentUserId()); //当前动作人UID 如果是自己就轮到自己操作了

		json.put("realLastAction", room.getRealLastAction());
		json.put("realLastUser", room.getRealLastUserId());

		if (room.getRealLastChuPai() != null) {
			List<Card> lastChuPai = room.getRealLastChuPai();
			List<JSONObject> list = new ArrayList<JSONObject>();
			for (Card c : lastChuPai) {
				list.add(Card.getReturnJson(c));
			}
			json.put("realLastChuPai", list); //最后出的牌
		}

		if (isZhaDan || (room.getPlayStatus() != null && room.getPlayStatus().intValue() == Cnst.ROOM_PLAYSTATE_END)) {
			readData.put("mul", getMulJsonObject(room));
			json.put("mul", getMulJsonObject(room));
		}
		//上一手牌
		json.put("lastCards", room.getLastCards());
		json.put("playStatus", room.getPlayStatus());
		json.put("state", room.getState().intValue() == Cnst.ROOM_STATE_YJS ? Cnst.ROOM_STATE_XJS : room.getState());
		RedisUtil.updateRedisData(room, null, cid);
		MessageFunctions.interface_100104(json, room, 100104, cid);

	}

	private static boolean isOnlyLaiZi(Player player) {
		boolean myOnlyLazi = true;
		for (Card card : player.getCurrentCardList()) {
			if (!Card.isLaizi(card)) {
				myOnlyLazi = false;
				break;
			}
		}
		return myOnlyLazi;
	}

	public static JSONObject getMulJsonObject(RoomResp room) {
		JSONObject json = new JSONObject();

		long constMul = 1l;
		if (room.getPlayStatus() != null && room.getPlayStatus().intValue() == Cnst.ROOM_PLAYSTATE_END && room.getNongminChu() != null && room.getNongminChu() == 0)
			constMul = constMul * 2l;

		if (room.getPlayStatus() != null && room.getPlayStatus().intValue() == Cnst.ROOM_PLAYSTATE_END && room.getDizhuChu() != null && room.getDizhuChu() < 2)
			constMul = constMul * 2l;

		if (room.getZhandans() != null) {
			for (int i = 0; i < room.getZhandans().size(); i++) {
				constMul = constMul * 2l;
			}
		}

		if (room.getDizhu() != null) {
			for (int idx = 0; idx < room.getPlayerIds().size(); idx++) {

				if (room.getPlayerIds().get(idx) == null)
					continue;
				long tmpMul = constMul;

				// 额外判断加倍问题
				if (room.getPlayerIds().get(idx).equals(room.getDizhu())) {
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
				json.put("" + room.getPlayerIds().get(idx), tmpMul);
			}
		}
		return json;
	}

	private static void sendChuPaiInfo(Long sessionId, Integer info) {
		if (sessionId == null)
			return;
		JSONObject result = getJSONObj(200000, 1, info);
		ProtocolData pd = new ProtocolData(200000, result.toJSONString());

		IoSession se = MinaServerManager.tcpServer.getSessions().get(sessionId);
		if (se != null && se.isConnected()) {
			se.write(pd);
		}
	}

	/**
	 * 比较俩个牌的大小
	 * @return
	 */
	private static boolean computer(Card card1,Card card2){
			int realSymble = Card.getRealSymble(card1);
			if (realSymble < 3 || realSymble == 14)
				realSymble += 13;
			int other = Card.getRealSymble(card2);
			if (other < 3 || other == 14)
				other += 13;
			if (realSymble == other && other == (14 + 13)) {
				// 大小王
				int realType = Card.getRealType(card1);
				int otherType = Card.getRealType(card2);
				return realType > otherType;
			} else
				return realSymble > other;
	}
	
	/**
	 * 比较当前的牌是否能管住上次的牌 首先 这两套牌型肯定是一样的 牌型是cardType
	 * 
	 * @param cardType
	 * @param current
	 * @param last
	 * @return
	 */
	private static boolean computer(int cardType, List<ArrayList<Card>> current, List<ArrayList<Card>> last) {

		if (cardType == Cnst.CARD_TYPE_DEFAULT || cardType == Cnst.CARD_TYPE_HUOJIAN)
			return false;

		if (cardType == Cnst.CARD_TYPE_ZHANDAN) {
			// 有一个真炮管假跑问题
			boolean cL = false;// 当前牌是否是癞子
			boolean lL = false;// 之前的牌是否是癞子
			ArrayList<Card> cA = current.get(3);
			for (Card card : cA) {
				if (card.getLaizi() != 0) {
					cL = true;
					break;
				}
			}
			ArrayList<Card> lA = last.get(3);

			for (Card card : lA) {
				if (card.getLaizi() != 0) {
					lL = true;
					break;
				}
			}

			if (cL) {
				// 自己是癞子
				if (!lL)
					return false;// 管不住别人的真炸弹
				int realSymble = Card.getRealSymble(cA.get(0));
				if (realSymble < 3)
					realSymble += 13;

				int other = Card.getRealSymble(lA.get(0));
				if (other < 3)
					other += 13;
				return realSymble > other;
			} else {
				// 自己不是癞子
				if (lL)
					return true;// 别人是癞子
				int realSymble = Card.getRealSymble(cA.get(0));
				if (realSymble < 3)
					realSymble += 13;

				int other = Card.getRealSymble(lA.get(0));
				if (other < 3)
					other += 13;
				return realSymble > other;
			}

		}
		// public static final int CARD_TYPE_DAN = 3;//单 1
		if (cardType == Cnst.CARD_TYPE_DAN) {
			int realSymble = Card.getRealSymble(current.get(0).get(0));
			if (realSymble < 3 || realSymble == 14)
				realSymble += 13;
			int other = Card.getRealSymble(last.get(0).get(0));
			if (other < 3 || other == 14)
				other += 13;
			if (realSymble == other && other == (14 + 13)) {
				// 大小王
				int realType = Card.getRealType(current.get(0).get(0));
				int otherType = Card.getRealType(last.get(0).get(0));
				return realType > otherType;
			} else
				return realSymble > other;
		}
		// public static final int CARD_TYPE_DUI = 4;//对 11
		if (cardType == Cnst.CARD_TYPE_DUI) {

			int realSymble = Card.getRealSymble(current.get(1).get(0));
			if (realSymble < 3)
				realSymble += 13;
			int other = Card.getRealSymble(last.get(1).get(0));
			if (other < 3)
				other += 13;
			return realSymble > other;
		}
		if (cardType == Cnst.CARD_TYPE_SANBUDAI || cardType == Cnst.CARD_TYPE_SANDAIONE || cardType == Cnst.CARD_TYPE_SANDAIDUI || cardType == Cnst.CARD_TYPE_SANSHUN || cardType == Cnst.CARD_TYPE_FEIJIDAN || cardType == Cnst.CARD_TYPE_FEIJIDUI) {
			int realSymble = Card.getRealSymble(current.get(2).get(0));
			if (realSymble < 3)
				realSymble += 13;
			int other = Card.getRealSymble(last.get(2).get(0));
			if (other < 3)
				other += 13;
			return realSymble > other;
		}
		if (cardType == Cnst.CARD_TYPE_DANSHUN) {
			int realSymble = Card.getRealSymble(current.get(0).get(0));
			if (realSymble < 3)
				realSymble += 13;
			int other = Card.getRealSymble(last.get(0).get(0));
			if (other < 3)
				other += 13;
			return realSymble > other;
		}
		if (cardType == Cnst.CARD_TYPE_SHUANGSHUN) {
			int realSymble = Card.getRealSymble(current.get(1).get(0));
			if (realSymble < 3)
				realSymble += 13;
			int other = Card.getRealSymble(last.get(1).get(0));
			if (other < 3)
				other += 13;
			return realSymble > other;
		}
		if (cardType == Cnst.CARD_TYPE_FOURTAKE2ONE || cardType == Cnst.CARD_TYPE_FOURTAKE2DUI) {
			int realSymble = Card.getRealSymble(current.get(3).get(0));
			if (realSymble < 3)
				realSymble += 13;
			int other = Card.getRealSymble(last.get(3).get(0));
			if (other < 3)
				other += 13;
			return realSymble > other;
		}

		return false;
	}

	/**
	 * 获取牌型
	 * 
	 * @param list
	 * @param type
	 *            0非癞子局 1癞子局
	 * @return
	 */
	public static int getCardType(List<ArrayList<Card>> list, int roomType, int can4t2Flag) {
		int minDanLian = 5;
		int minDuiLian = 3;
		int minSanLian = 2;
		boolean can4T2 = can4t2Flag == 0 ? false : true;
		if (roomType == Cnst.ROOM_TYPE_2) {
			minDanLian = 4;
		}
		// 牌型
		// int CARD_TYPE_HUOJIAN = 1; //俩王
		if (list.get(1).isEmpty() && list.get(2).isEmpty() && list.get(3).isEmpty()) {
			if (list.get(0).size() == 2) {
				if (Card.getRealSymble(list.get(0).get(0)) == 14 && Card.getRealSymble(list.get(0).get(1)) == 14)
					return Cnst.CARD_TYPE_HUOJIAN;
			}
		}

		// CARD_TYPE_ZHANDAN = 2
		if (list.get(0).isEmpty() && list.get(1).isEmpty() && list.get(2).isEmpty()) {
			if (list.get(3).size() == 4) {
				return Cnst.CARD_TYPE_ZHANDAN;
			}
		}

		// CARD_TYPE_DAN = 3;//单 1
		if (list.get(1).isEmpty() && list.get(2).isEmpty() && list.get(3).isEmpty()) {
			if (list.get(0).size() == 1) {
				return Cnst.CARD_TYPE_DAN;
			}
		}

		// CARD_TYPE_DUI = 4;//对 11
		if (list.get(0).isEmpty() && list.get(2).isEmpty() && list.get(3).isEmpty()) {
			if (list.get(1).size() == 2) {
				return Cnst.CARD_TYPE_DUI;
			}
		}
		// CARD_TYPE_SANBUDAI = 5;//3不带 111
		if (list.get(0).isEmpty() && list.get(1).isEmpty() && list.get(3).isEmpty()) {
			if (list.get(2).size() == 3) {
				return Cnst.CARD_TYPE_SANBUDAI;
			}
		}

		// CARD_TYPE_SANDAIONE = 6;//3带1 可以带一对 1112
		if (list.get(3).isEmpty() && list.get(2).size() == 3) {
			if (list.get(1).size() == 2 && list.get(0).isEmpty()) {
				return Cnst.CARD_TYPE_SANDAIDUI;
			}

			if (list.get(0).size() == 1 && list.get(1).isEmpty()) {
				return Cnst.CARD_TYPE_SANDAIONE;
			}
		}

		// CARD_TYPE_DANSHUN = 8;//顺子 单瞬 56789 不能有2王
		if (list.get(1).isEmpty() && list.get(2).isEmpty() && list.get(3).isEmpty()) {
			// 判断是否是顺子
			ArrayList<Card> arrayList = list.get(0);
			if (!arrayList.isEmpty()) {
				boolean isDanShun = true;
				int min = 0;
				int num = 0;
				for (Card card : arrayList) {
					if (Card.getRealSymble(card) == 2 || Card.getRealSymble(card) == 14) {
						// 有2有王
						isDanShun = false;
						break;
					} else {
						if (min == 0) {
							// 第一个
							min = Card.getRealSymble(card);
							++num;
						} else {
							// 1和K的衔接问题
							if (min == 1 && Card.getRealSymble(card) == 13) {
								min = 13;
								++num;
							} else if (min - 1 == Card.getRealSymble(card)) {
								// 2不允许连拍 大小王也是
								if (min == 2 || Card.getRealSymble(card) == 2 || min > 13 || Card.getRealSymble(card) > 13)
									isDanShun = false;
								else {
									min = Card.getRealSymble(card);
									++num;
								}
							} else {
								isDanShun = false;
							}
						}
					}
				}
				if (num < minDanLian) {
					isDanShun = false;
				}

				if (isDanShun)
					return Cnst.CARD_TYPE_DANSHUN;
			}
		}
		// CARD_TYPE_SHUANGSHUN = 9;//双顺 778899 不能有2
		if (list.get(0).isEmpty() && list.get(2).isEmpty() && list.get(3).isEmpty()) {
			// 判断是否是顺子
			ArrayList<Card> arrayList = list.get(1);
			if (!arrayList.isEmpty()) {
				boolean isShun = true;
				int min = 0;
				int num = 0;
				for (int i = 0; i < arrayList.size(); i += 2) {
					Card card = arrayList.get(i);
					if (Card.getRealSymble(card) == 2 || Card.getRealSymble(card) == 14) {
						// 有2有王
						isShun = false;
						break;
					} else {
						if (min == 0) {
							// 第一个
							min = Card.getRealSymble(card);
							++num;
						} else {
							if (min == 1 && Card.getRealSymble(card) == 13) {
								min = 13;
								++num;
							} else if (min - 1 == Card.getRealSymble(card)) {
								// min = card.getRealSymble();
								// ++num;
								if (min == 2 || Card.getRealSymble(card) == 2 || min > 13 || Card.getRealSymble(card) > 13)
									isShun = false;
								else {
									min = Card.getRealSymble(card);
									++num;
								}
							} else {
								isShun = false;
							}
						}
					}
				}
				if (num < minDuiLian) {
					isShun = false;
				}

				if (isShun)
					return Cnst.CARD_TYPE_SHUANGSHUN;
			}
		}

		// CARD_TYPE_SANSHUN = 10;//三顺 777888999 不能有2
		if (list.get(3).isEmpty()) {
			// 判断是否是顺子
			ArrayList<Card> arrayList = list.get(2);
			if (!arrayList.isEmpty()) {
				boolean isShun = true;
				int min = 0;
				int num = 0;
				for (int i = 0; i < arrayList.size(); i += 3) {
					Card card = arrayList.get(i);
					if (Card.getRealSymble(card) == 2 || Card.getRealSymble(card) == 14) {
						// 有2有王
						isShun = false;
						break;
					} else {
						if (min == 0) {
							// 第一个
							min = Card.getRealSymble(card);
							++num;
						} else {
							if (min == 1 && Card.getRealSymble(card) == 13) {
								min = 13;
								++num;
							} else if (min - 1 == Card.getRealSymble(card)) {
								// min = card.getRealSymble();
								// ++num;
								if (min == 2 || Card.getRealSymble(card) == 2 || min > 13 || Card.getRealSymble(card) > 13)
									isShun = false;
								else {
									min = Card.getRealSymble(card);
									++num;
								}
							} else {
								isShun = false;
							}
						}
					}
				}
				if (num < minSanLian) {
					isShun = false;
				}

				if (isShun) {
					// 三顺 都不带
					if (list.get(0).isEmpty() && list.get(1).isEmpty())
						return Cnst.CARD_TYPE_SANSHUN;
					// 现在可以带牌了
					if (list.get(0).isEmpty()) {
						// 带的对 或者对当做单
						if (num == list.get(1).size())
							return Cnst.CARD_TYPE_FEIJIDAN;

						if (num == list.get(1).size() / 2) {
							return Cnst.CARD_TYPE_FEIJIDUI;
						}
					}
					if (list.get(1).isEmpty()) {
						if (num == list.get(0).size())
							return Cnst.CARD_TYPE_FEIJIDAN;
					}

					if (list.get(3).isEmpty() && list.get(1).size() + list.get(0).size() == num)
						return Cnst.CARD_TYPE_FEIJIDAN;

				}
			}
		}

		// CARD_TYPE_FOURTAKE2ONE = 12;//四代2单 不能是飞机形式 只能单带
		// CARD_TYPE_FOURTAKE2DUI = 13;//四代2对 不能是飞机形式 只能单带
		if (can4T2 && list.get(2).isEmpty() && list.get(3).size() == 4) {
			if (list.get(1).size() == 4 && list.get(0).isEmpty())
				return Cnst.CARD_TYPE_FOURTAKE2DUI;
			if (list.get(1).size() == 2 && list.get(0).isEmpty())
				return Cnst.CARD_TYPE_FOURTAKE2ONE;
			if (list.get(0).size() == 2 && list.get(1).isEmpty())
				return Cnst.CARD_TYPE_FOURTAKE2ONE;
		}

		// 55553333的情况 表示四代2对 如果是55554444 放到后面 不在这里处理 放最后处理成飞机
		if (can4T2 && list.get(0).isEmpty() && list.get(1).isEmpty() && list.get(2).isEmpty() && list.get(3).size() == 8) {
			// 先判断里面的是否是连续 如果是连续就留到最后算飞机
			boolean isShun = false;
			int a1 = Card.getRealSymble(list.get(3).get(0));
			int a2 = Card.getRealSymble(list.get(3).get(4));

			if (a1 == 1 && a2 == 13) {
				isShun = true;
			} else if (a1 - a2 == 1) {
				if (a1 != 2 && a2 != 2) {
					isShun = true;
				}
			}
			if (!isShun)
				return Cnst.CARD_TYPE_FOURTAKE2DUI;
		}
		// 44443331这种非常特殊的情况 这个牌型判断放到最后 判断 必能比任何牌型判断要靠前 思路吧4张牌的拆开 1张放入1张牌的
		// 剩下的三张牌放入3张牌
		// 一样的数组里面，然后对三张牌一样的牌重新排序 然后再用三代1的逻辑重新判断一遍 注意LIST改变 方便后面需要LIST比较大小的方法使用
		if (list.get(3).size() > 0) {
			ArrayList<Card> arrayList3 = list.get(3);
			list.set(3, new ArrayList<Card>());
			for (int i = 0; i < arrayList3.size(); i++) {
				Card card = arrayList3.get(i);
				if ((i % 4) == 0) {
					list.get(0).add(card);
				} else {
					list.get(2).add(card);
				}
			}

			Collections.sort(list.get(2));

			if (list.get(3).isEmpty()) {
				// 判断是否是顺子
				ArrayList<Card> arrayList = list.get(2);
				if (!arrayList.isEmpty()) {
					boolean isShun = true;
					int min = 0;
					int num = 0;
					for (int i = 0; i < arrayList.size(); i += 3) {
						Card card = arrayList.get(i);
						if (Card.getRealSymble(card) == 2 || Card.getRealSymble(card) == 14) {
							// 有2有王
							isShun = false;
							break;
						} else {
							if (min == 0) {
								// 第一个
								min = Card.getRealSymble(card);
								++num;
							} else {
								if (min == 1 && Card.getRealSymble(card) == 13) {
									min = 13;
									++num;
								} else if (min - 1 == Card.getRealSymble(card)) {
									// min = card.getRealSymble();
									// ++num;
									if (min == 2 || Card.getRealSymble(card) == 2 || min > 13 || Card.getRealSymble(card) > 13)
										isShun = false;
									else {
										min = Card.getRealSymble(card);
										++num;
									}
								} else {
									isShun = false;
								}
							}
						}
					}
					if (num < minSanLian) {
						isShun = false;
					}

					if (isShun) {
						// 三顺 都不带
						if (list.get(0).isEmpty() && list.get(1).isEmpty())
							return Cnst.CARD_TYPE_SANSHUN;
						// 现在可以带牌了
						if (list.get(0).isEmpty()) {
							// 带的对 或者对当做单
							if (num == list.get(1).size())
								return Cnst.CARD_TYPE_FEIJIDAN;

							if (num == list.get(1).size() / 2) {
								return Cnst.CARD_TYPE_FEIJIDUI;
							}
						}
						if (list.get(1).isEmpty()) {
							if (num == list.get(0).size())
								return Cnst.CARD_TYPE_FEIJIDAN;
						}
					}
				}
			}
		}
		return Cnst.CARD_TYPE_DEFAULT;
	}

	/**
	 * 处理list 返回有四个元素的数组 每个元素也是一个数组 前提list必须排好序 第一个元素 单张牌 第二个元素 重复2次的牌 对子 第三个元素
	 * 重复三次的牌 第四个元素 重复四次的牌
	 * 
	 * @param list
	 * @return
	 */
	public static List<ArrayList<Card>> handlerCard(List<Card> list) {
		List<ArrayList<Card>> result = new ArrayList<ArrayList<Card>>();
		ArrayList<Card> one = new ArrayList<Card>();
		ArrayList<Card> two = new ArrayList<Card>();
		ArrayList<Card> three = new ArrayList<Card>();
		ArrayList<Card> four = new ArrayList<Card>();
		Card c = null;
		Map<Integer, Integer> tmpMap = new HashMap<Integer, Integer>();
		for (int i = 0; i < list.size(); i++) {
			c = list.get(i);

			if (tmpMap.containsKey(Card.getRealSymble(c)))
				continue;

			if (Card.getRealSymble(c) == 14) {
				one.add(c);
				continue;
			}
			int num = 1;// 出现次数
			ArrayList<Card> tmpList = new ArrayList<Card>();
			tmpList.add(c);
			for (int j = i + 1; j < list.size(); j++) {
				if (Card.getRealSymble(c) == Card.getRealSymble(list.get(j))) {
					++num;
					tmpList.add(list.get(j));
				}
			}
			tmpMap.put(Card.getRealSymble(c), num);
			// 单牌
			if (num == 1) {
				one.add(c);
			} else if (num == 2) {
				two.addAll(tmpList);
			} else if (num == 3) {
				three.addAll(tmpList);
			} else {
				four.addAll(tmpList);
			}
		}

		result.add(one);
		result.add(two);
		result.add(three);
		result.add(four);

		return result;
	}

	private static void handlerJiaBei(RoomResp room, Player player, Integer action, Map<String, Object> readData, String cid) {
		int idx = 0;
		for (; idx < room.getPlayerIds().size(); idx++) {
			if (room.getPlayerIds().get(idx).longValue() == player.getUserId().longValue()) {
				break;
			}
		}
		if (action.intValue() == Cnst.ACTION_JIABEI)
			room.getMuls().set(idx, 1);
		else
			room.getMuls().set(idx, 0);

		room.setLastAction(action);
		room.setLastActionExtra(null);
		room.setLastUserId(player.getUserId());

		if (room.getMulType().intValue() == Cnst.ROOM_MUL_TYPE_2) {
			// 农民加倍 找下一个农民 找不到换地主
			// 下一个农民是否喊过加倍
			boolean dizhuChoose = false;
			int nextNMIdx = 0;// 下一个农民的索引 如果当前操作人是地主 这个字段不准确 但是也不会用到
			int nongMinNum = 0;// 农民选择加或者不加的人数
			int totalN = 0;// 农民选择了 而且选择加倍的人数
			for (int i = 1; i < 3; i++) {
				int nextIdx = (idx + i) % 3;
				if (room.getPlayerIds().get(nextIdx).longValue() != room.getDizhu().longValue()) {
					// 不是地主 但是已经选过加倍了
					if (room.getMuls().get(nextIdx) != null) {
						++nongMinNum;
						if (room.getMuls().get(nextIdx) > 0)
							++totalN;
					}

					nextNMIdx = nextIdx;
				} else {
					// 是地主 但是已经选过加倍了
					if (room.getMuls().get(nextIdx) != null)
						dizhuChoose = true;
				}
			}

			if (player.getUserId().longValue() == room.getDizhu().longValue()) {
				// 如果当前的玩家就是地主
				dizhuChoose = true;
			} else {
				++nongMinNum;
				if (action.intValue() == Cnst.ACTION_JIABEI)
					++totalN;
				;
			}
			// 如果俩农民都加喊过加倍或者不加倍了
			if (nongMinNum > 1) {
				if (totalN < 1 || dizhuChoose) {
					// 如果地主选了 或者是有至少1个农民不加倍 地主无权利加倍 直接让地主出牌
					room.setPlayStatus(Cnst.ROOM_PLAYSTATE_CHUPAI);
					ArrayList<Integer> arrayList = new ArrayList<Integer>();
					arrayList.add(Cnst.ACTION_CHUPAI);
					arrayList.add(Cnst.ACTION_GUO);
					room.setCurrentUserAction(arrayList);
					room.setCurrentUserId(room.getDizhu());
				} else {
					room.setCurrentUserId(room.getDizhu());
					ArrayList<Integer> arrayList = new ArrayList<Integer>();
					arrayList.add(Cnst.ACTION_JIABEI);
					arrayList.add(Cnst.ACTION_BUJIABEI);
					room.setCurrentUserAction(arrayList);
				}
			} else {
				// 下一个农民还没发言过
				room.setCurrentUserId(room.getPlayerIds().get(nextNMIdx));
				ArrayList<Integer> arrayList = new ArrayList<Integer>();
				arrayList.add(Cnst.ACTION_JIABEI);
				arrayList.add(Cnst.ACTION_BUJIABEI);
				room.setCurrentUserAction(arrayList);
			}
		} else {
			// 肯定是自由加倍
			int nextIdx = (idx + 1) % 3;
			if (room.getMuls().get(nextIdx) == null) {
				// 让下一个人选择加倍还是不加倍
				room.setCurrentUserId(room.getPlayerIds().get(nextIdx));
				ArrayList<Integer> arrayList = new ArrayList<Integer>();
				arrayList.add(Cnst.ACTION_JIABEI);
				arrayList.add(Cnst.ACTION_BUJIABEI);
				room.setCurrentUserAction(arrayList);
			} else {
				// 让地主出牌
				room.setPlayStatus(Cnst.ROOM_PLAYSTATE_CHUPAI);
				ArrayList<Integer> arrayList = new ArrayList<Integer>();
				arrayList.add(Cnst.ACTION_CHUPAI);
				arrayList.add(Cnst.ACTION_GUO);
				room.setCurrentUserAction(arrayList);
				room.setCurrentUserId(room.getDizhu());
			}
		}

		RedisUtil.updateRedisData(room, null, cid);
		JSONObject json = new JSONObject();
		json.put("continue", 0);
		json.put("lastAction", room.getLastAction());
		json.put("lastUserId", room.getLastUserId());
		json.put("lastActionExtra", room.getLastActionExtra());
		json.put("lastUserPaisNum", player.getCurrentCardList().size());
		json.put("currentUserAction", room.getCurrentUserAction());
		json.put("currentUserId", room.getCurrentUserId());
		json.put("playStatus", room.getPlayStatus());

		if (action.intValue() == Cnst.ACTION_JIABEI) {
			json.put("mul", getMulJsonObject(room));
			readData.put("mul", getMulJsonObject(room));
		}
		
		//readData.put("difen", room.getDiFen()); //回放底分
		Map newMap = getNewMap(readData);
		addRecord(room, newMap, cid);

		MessageFunctions.interface_100104(json, room, 100104, cid);

	}

	/**
	 * 处理叫地主环节的动作
	 * 
	 * @param room
	 * @param player
	 * @param dizhuFen
	 */
	private static boolean handlerJiaoDizhu(RoomResp room, Player player, Integer dizhuFen, Integer action, Map<String, Object> readData, String cid) {

		//1,找到叫地主的人的ID
		int idx = 0;
		for (; idx < room.getPlayerIds().size(); idx++) {
			if (room.getPlayerIds().get(idx).longValue() == player.getUserId().longValue()) {
				break;
			}
		}
		// 预设值下个动作是叫地主
		// 先处理动作

		room.getJiaoDiZhu().set(idx, action);

		//直接叫3分
		if (action.intValue() == Cnst.ACTION_DIZHU3) {
			// 地主产生
			room.setDizhu(player.getUserId());
			room.setDiFen(3);
			//设置集合中的对应的room.getRole() 0.0.1.
			for (int i = 0; i < room.getPlayerIds().size(); i++) {
				if(player.getUserId().equals(room.getPlayerIds().get(i))){
					room.getRole().set(i, 1);
				}
			}
		} else {
			// 叫的分数不够 暂时产生不了地主 但是不一定
		}

		// 继续让别人叫或者抢地主
		room.setLastAction(action);
		room.setLastActionExtra(dizhuFen);
		room.setLastUserId(player.getUserId());

		JSONObject json = new JSONObject();

		json.put("lastAction", room.getLastAction());
		json.put("lastUserId", room.getLastUserId());
		json.put("lastActionExtra", room.getLastActionExtra());

		if (room.getDizhu() == null) {
			// 检查是不是是都回应了
			int tmp = 0;
			int maxIdx = 0;
			int max = 0;
			for (int i = 0; i < room.getJiaoDiZhu().size(); i++) {
				if (room.getJiaoDiZhu().get(i) == null)
					continue;
				++tmp;
				if (room.getJiaoDiZhu().get(i) == Cnst.ACTION_DIZHU0)
					continue;
				if (room.getJiaoDiZhu().get(i) > max) {
					max = room.getJiaoDiZhu().get(i) - 1;// 转换成分数
					maxIdx = i;
				}
			}

			if (tmp == 3) {
				// 都回应完了
				if (max == 0) {
					RoomRecordUtil.clearLiuJu(room.getRoomId(), Long.valueOf(room.getCreateTime()), cid);

					// 重新发牌 或者进入大结算
					// 绝对不会>10000
					room.setGiveUpTime(RoomResp.getRealGiveUpTime(room) + 1);

					if (room.getLastNum() > 0)
						startGame(room, RedisUtil.getPlayerList(room, cid), cid);
					else {
						room.setState(Cnst.ROOM_STATE_YJS);
						room.setPlayStatus(Cnst.ROOM_PLAYSTATE_END);
						List<Player> playerList = RedisUtil.getPlayerList(room, cid);
						ArrayList<Long> arrayList = new ArrayList<Long>();
						for (Player player2 : playerList) {
							if (player2 != null) {
								arrayList.add(player2.getScore());
							} else {
								arrayList.add(null);
							}
						}
						room.setScore(arrayList);
						RedisUtil.updateRedisData(room, null, cid);
						TCPGameFunctions.updateDatabasePlayRecord(room, cid);
						json.put("state", room.getState());
						MessageFunctions.interface_100140(room, cid);
					}
					json.put("continue", 1);
					json.put("lastNum", room.getLastNum());
					json.put("playStatus", room.getPlayStatus());
					MessageFunctions.interface_100104(json, room, 100104, cid);
					return true;
				} else {
					// 地主产生
					room.setDizhu(room.getPlayerIds().get(maxIdx));
					room.setDiFen(max);
				}
			}
		}

		json.put("continue", 0);

		if (room.getDizhu() == null) {
			int nextUseIdx = (idx + 1) % 3;
			ArrayList<Integer> nextActions = new ArrayList<Integer>();

			Player nextPlayer = RedisUtil.getPlayerByUserId(room.getPlayerIds().get(nextUseIdx) + "", cid);
			nextActions.add(Cnst.ACTION_DIZHU0);
			if (room.getDaPaiJiaoMan().intValue() == Cnst.ROOM_JIAO_TYPE_2 && isHaveDaPai(nextPlayer)) {
				// 双王或者四个2必须叫满
				nextActions.add(Cnst.ACTION_DIZHU3);
			} else if (RoomResp.getRealGiveUpTime(room) > 0) {
				nextActions.add(Cnst.ACTION_DIZHU3);
			} else {
				if (action.intValue() == Cnst.ACTION_DIZHU0) {
					nextActions.add(Cnst.ACTION_DIZHU1);
					nextActions.add(Cnst.ACTION_DIZHU2);
					nextActions.add(Cnst.ACTION_DIZHU3);
				} else {
					if (action < Cnst.ACTION_DIZHU2)
						nextActions.add(Cnst.ACTION_DIZHU2);
					if (action < Cnst.ACTION_DIZHU3)
						nextActions.add(Cnst.ACTION_DIZHU3);
				}
			}

			room.setCurrentUserAction(nextActions);
			room.setCurrentUserId(room.getPlayerIds().get(nextUseIdx));
		} else {
			// 先把牌给地主
			ArrayList<Integer> giveCards = new ArrayList<Integer>();
			List<Card> currentCardList = room.getCurrentCardList();
			for (int i = 0; i < currentCardList.size(); i++) {
				giveCards.add(room.getCurrentCardList().get(i).getOrigin());
			}
			// room.setCurrentCardList(null);
			Player dizhuPlayer = null;
			if (room.getDizhu().equals(player.getUserId())) {
				dizhuPlayer = player;
				dizhuPlayer.getCurrentCardList().addAll(currentCardList);
				RedisUtil.setPlayerByUserId(dizhuPlayer, cid);
			} else {
				dizhuPlayer = RedisUtil.getPlayerByUserId(room.getDizhu() + "", cid);
				dizhuPlayer.getCurrentCardList().addAll(currentCardList);
				RedisUtil.setPlayerByUserId(dizhuPlayer, cid);
			}
			json.put("diZhuPai", giveCards);
			json.put("dizhu", room.getDizhu());
			readData.put("dizhu", room.getDizhu());

			// 可以明牌
			if (room.getMingPaiType() == Cnst.MINGPAY_TYPE_2 || room.getMingPaiType() == Cnst.MINGPAY_TYPE_3) {
				room.setPlayStatus(Cnst.ROOM_PLAYSTATE_MINGPAI);
				ArrayList<Integer> arrayList = new ArrayList<Integer>();
				arrayList.add(Cnst.ACTION_BUMINGPAI);
				arrayList.add(Cnst.ACTION_MINGPAI);
				room.setCurrentUserAction(arrayList);
				room.setCurrentUserId(dizhuPlayer.getUserId());
			} else {
				// 都不能明牌
				// 地主产生进入下一个环节
				if (room.getMulType() == Cnst.ROOM_MUL_TYPE_2) {

					int dizhuIdx = 0;
					for (; dizhuIdx < 3; dizhuIdx++) {
						if (room.getPlayerIds().get(dizhuIdx).longValue() == room.getDizhu().longValue())
							break;
					}
					// 找到地主之后的第一个农民
					room.setPlayStatus(Cnst.ROOM_PLAYSTATE_JIABEI);
					int nextUseIdx = (dizhuIdx + 1) % 3;
					ArrayList<Integer> arrayList = new ArrayList<Integer>();
					arrayList.add(Cnst.ACTION_BUJIABEI);
					arrayList.add(Cnst.ACTION_JIABEI);
					room.setCurrentUserAction(arrayList);
					room.setCurrentUserId(room.getPlayerIds().get(nextUseIdx));
				} else if (room.getMulType() == Cnst.ROOM_MUL_TYPE_3) {
					// 直接让地主加倍
					room.setPlayStatus(Cnst.ROOM_PLAYSTATE_JIABEI);
					ArrayList<Integer> arrayList = new ArrayList<Integer>();
					arrayList.add(Cnst.ACTION_BUJIABEI);
					arrayList.add(Cnst.ACTION_JIABEI);
					room.setCurrentUserAction(arrayList);
					room.setCurrentUserId(dizhuPlayer.getUserId());
				} else {
					// 直接进入出牌环节
					room.setPlayStatus(Cnst.ROOM_PLAYSTATE_CHUPAI);
					ArrayList<Integer> arrayList = new ArrayList<Integer>();
					arrayList.add(Cnst.ACTION_CHUPAI);
					room.setCurrentUserAction(arrayList);
					room.setCurrentUserId(dizhuPlayer.getUserId());
				}
			}

			int difen = room.getDiFen();
			int realGiveUpTime = RoomResp.getRealGiveUpTime(room);
			for (int i = 0; i < realGiveUpTime; i++) {
				if (difen > Integer.MAX_VALUE / 2) {
					difen = Integer.MAX_VALUE;
				} else {
					difen = difen * 2;
				}
			}
			// 暂时不能清除掉
			if (room.getGiveUpTime() != null && room.getGiveUpTime() > 0 && room.getGiveUpTime() < 10000) {
				// 这种情况不需要直接置位0 要加到10000 因为结算时候还是需要这个数据的
				room.setGiveUpTime(room.getGiveUpTime() + 10000);
			}

			json.put("diFen", difen);
			readData.put("diFen", difen);
			// JSONObject mulJsonObject = getMulJsonObject(room, null);
			json.put("mul", getMulJsonObject(room));
			readData.put("mul", getMulJsonObject(room));
		}
		RedisUtil.updateRedisData(room, null, cid);

		//readData.put("difen", room.getDiFen()); //回放底分
		
		Map record = getNewMap(readData);
		addRecord(room, record, cid);
		json.put("lastUserPaisNum", player.getCurrentCardList().size());
		json.put("currentUserAction", room.getCurrentUserAction());
		json.put("currentUserId", room.getCurrentUserId());
		json.put("playStatus", room.getPlayStatus());
		MessageFunctions.interface_100104(json, room, 100104, cid);

		return true;
	}

	/**
	 * 玩家申请解散房间
	 * 
	 * @param session
	 * @param readData
	 * @throws Exception
	 */
	public static void interface_100203(IoSession session, Map<String, Object> readData) throws Exception {
		logger.I("玩家请求解散房间,interfaceId -> 100203");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomSn"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId), cid);

		if (room.getDissolveRoom() != null) {
			return;
		}
		DissolveRoom dis = new DissolveRoom();
		dis.setDissolveTime(new Date().getTime());
		dis.setUserId(userId);
		List<Map<String, Object>> othersAgree = new ArrayList<>();
		List<Player> players = RedisUtil.getPlayerList(room, cid);
		for (Player p : players) {
			if (p == null)
				continue;
			if (!p.getUserId().equals(userId)) {
				Map<String, Object> map = new HashMap<>();
				map.put("userId", p.getUserId());
				map.put("agree", 0);// 1同意；2解散；0等待
				othersAgree.add(map);
			}
		}
		dis.setOthersAgree(othersAgree);
		room.setDissolveRoom(dis);

		Map<String, Object> info = new HashMap<>();
		info.put("dissolveTime", dis.getDissolveTime());
		info.put("userId", dis.getUserId());
		info.put("othersAgree", dis.getOthersAgree());
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		for (Player p : players) {
			if (p == null)
				continue;
			IoSession se = session.getService().getManagedSessions().get(p.getSessionId());
			if (se != null && se.isConnected()) {
				se.write(pd);
			}
		}

		for (Player p : players) {

			RedisUtil.updateRedisData(null, p, cid);
		}

		RedisUtil.setObject(Cnst.get_REDIS_PREFIX_ROOMMAP(cid).concat(roomId + ""), room, Cnst.ROOM_LIFE_TIME_DIS);

		// 解散房间超时任务开启
		startDisRoomTask(room.getRoomId(), Cnst.DIS_ROOM_TYPE_2, cid);
	}

	/**
	 * 同意或者拒绝解散房间
	 * 
	 * @param session
	 * @param readData
	 * @throws Exception
	 */

	public static void interface_100204(IoSession session, Map<String, Object> readData) throws Exception {
		logger.I("同意或者拒绝解散房间,interfaceId -> 100203");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomSn"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		Integer userAgree = StringUtils.parseInt(readData.get("userAgree"));

		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId), cid);
		if (room == null) {// 房间已经自动解散
			Map<String, Object> info = new HashMap<>();
			info.put("reqState", Cnst.REQ_STATE_4);
			JSONObject result = getJSONObj(interfaceId, 1, info);
			ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
			session.write(pd);
			return;
		}
		if (room.getDissolveRoom() == null) {
			Map<String, Object> info = new HashMap<>();
			info.put("reqState", Cnst.REQ_STATE_7);
			JSONObject result = getJSONObj(interfaceId, 1, info);
			ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
			session.write(pd);
			return;
		}
		List<Map<String, Object>> othersAgree = room.getDissolveRoom().getOthersAgree();
		for (Map<String, Object> m : othersAgree) {
			if (String.valueOf(m.get("userId")).equals(String.valueOf(userId))) {
				m.put("agree", userAgree);
				break;
			}
		}
		Map<String, Object> info = new HashMap<>();
		info.put("dissolveTime", room.getDissolveRoom().getDissolveTime());
		info.put("userId", room.getDissolveRoom().getUserId());
		info.put("othersAgree", room.getDissolveRoom().getOthersAgree());
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());

		if (userAgree != 1) {
			// 有玩家拒绝解散房间
			room.setDissolveRoom(null);
			System.out.println("这里是有玩家拒绝解散房间");
			//TODO 
			notifyDisRoomTask(room, Cnst.DIS_ROOM_TYPE_2);//取消解散房间计时
			RedisUtil.setObject(Cnst.get_REDIS_PREFIX_ROOMMAP(cid).concat(roomId + ""), room, Cnst.ROOM_LIFE_TIME_CREAT);
		}
		int agreeNum = 0;
		int rejectNunm = 0;

		for (Map<String, Object> m : othersAgree) {
			if (m.get("agree").equals(1)) {
				agreeNum++;
			} else if (m.get("agree").equals(2)) {
				rejectNunm++;
			}
		}
		RedisUtil.updateRedisData(room, null, cid);

		List<Player> players = RedisUtil.getPlayerList(room, cid);

		if (agreeNum == 2 || rejectNunm >= 1) {
			if (agreeNum == 2) {

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

				MessageFunctions.updateDatabasePlayRecord(room, cid);

				for (Player p : players) {
					if (p == null)
						continue;
					p.initPlayer(null, Cnst.PLAYER_STATE_DATING, 0l);
				}
				room.setDissolveRoom(null);
				RedisUtil.updateRedisData(room, null, cid);
				RedisUtil.setPlayersList(players, cid);
				// 关闭解散房间计时任务
				notifyDisRoomTask(room, Cnst.DIS_ROOM_TYPE_2);
				// BackFileUtil.write(null, 100103, room,null,null);//写入文件内容

				RoomRecordUtil.clearLiuJu(room.getRoomId(), Long.valueOf(room.getCreateTime()), cid);

				//写入回放文件内容
				RoomRecordUtil.save(room.getRoomId(), Long.valueOf(room.getCreateTime()), cid);

				if (room.getExtraType().intValue() == Cnst.ROOM_EXTRA_TYPE_2) {
					RedisUtil.hdel(Cnst.get_ROOM_DAIKAI_KEY(cid).concat(room.getCreateId() + ""), room.getRoomId() + "");
				}

				MessageFunctions.interface_100140(room, cid);
			}
		}

		for (Player p : players) {
			IoSession se = session.getService().getManagedSessions().get(p.getSessionId());
			if (se != null && se.isConnected()) {
				se.write(pd);
			}
		}

	}

	/**
	 * 退出房间(房主解散房间)
	 * 
	 * @param session
	 * @param readData
	 * @throws Exception
	 */
	public static void interface_100205(IoSession session, Map<String, Object> readData) throws Exception {
		logger.I("准备,interfaceId -> 100205");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomSn"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);

		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId), cid);

		if (room == null) {
			roomDoesNotExist(interfaceId, session);
			return;
		}
		boolean changeMoney = false;
		if (room.getState() == Cnst.ROOM_STATE_CREATED) {
			List<Player> players = RedisUtil.getPlayerList(room, cid);
			Map<String, Object> info = new HashMap<>();
			info.put("userId", userId);
			if (room.getCreateId().equals(userId) && room.getExtraType().intValue() == Cnst.ROOM_EXTRA_TYPE_1) {// 房主退出
				int circle = room.getCircleNum();
				info.put("type", Cnst.EXIST_TYPE_DISSOLVE);
				Integer money=0;
				if (room.getRoomType() == Cnst.ROOM_TYPE_1){
					money=Cnst.moneyMap_1.get(circle);
					
				}else{
					money=Cnst.moneyMap_2.get(circle);
				}
				if(String.valueOf(room.getRoomId()).length()==7){//将俱乐部的房卡加上
					ClubInfo redisClub = RedisUtil.getClubInfoByClubId(Cnst.get_REDIS_PREFIX_CLUBMAP(cid)+room.getClubId().toString());
					redisClub.setRoomCardNum(redisClub.getRoomCardNum()+money);
					RedisUtil.setClubInfoByClubId(Cnst.get_REDIS_PREFIX_CLUBMAP(cid)+room.getClubId().toString(), redisClub);
				}else{//将玩家的房卡加上
					for (Player p : players) {
						if (p == null)
							continue;
						if (p.getUserId().equals(userId)) {
							changeMoney = true;
								p.setMoney(p.getMoney() + money);
							break;
						}
					}
				}

				// 关闭解散房间计时任务
				notifyDisRoomTask(room, Cnst.DIS_ROOM_TYPE_1);
				RedisUtil.deleteByKey(Cnst.get_REDIS_PREFIX_ROOMMAP(cid).concat(String.valueOf(roomId)));

				for (Player p : players) {
					if (p == null)
						continue;
					p.initPlayer(null, Cnst.PLAYER_STATE_DATING, 0l);
				}
			} else {// 正常退出
				info.put("type", Cnst.EXIST_TYPE_EXIST);
				existRoom(room, players, userId);
				RedisUtil.updateRedisData(room, null, cid);

				if (room.getExtraType().intValue() == Cnst.ROOM_EXTRA_TYPE_2 && !userId.equals(room.getCreateId())) {
					Player p = null;
					for (Player player : players) {
						if (player != null) {
							if (player.getUserId().longValue() == userId.longValue()) {
								p = player;
							}
						}
					}
					MessageFunctions.interface_100112(p, room, Cnst.ROOM_NOTICE_OUT, cid);
				}
			}
			JSONObject result = getJSONObj(interfaceId, 1, info);
			ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());

			for (Player p : players) {
				if (p == null)
					continue;
				RedisUtil.updateRedisData(null, p, cid);
			}

			for (Player p : players) {
				if (p == null)
					continue;
				IoSession se = session.getService().getManagedSessions().get(p.getSessionId());
				if (se != null && se.isConnected()) {
					if (changeMoney && p.getUserId().equals(room.getCreateId())) {
						info.put("money", p.getMoney());
						JSONObject result1 = getJSONObj(interfaceId, 1, info);
						ProtocolData pd1 = new ProtocolData(interfaceId, result1.toJSONString());
						se.write(pd1);
					}
					se.write(pd);
				}
			}

		} else {
			roomIsGaming(interfaceId, session);
		}
	}

	//玩家退出房间,将玩家的位置设置为空
	private static void existRoom(RoomResp room, List<Player> players, Long userId) {
		for (Player p : players) {
			if (p == null)
				continue;
			if (p.getUserId().equals(userId)) {
				p.initPlayer(null, Cnst.PLAYER_STATE_DATING, 0l);
				break;
			}
		}
		List<Long> pids = room.getPlayerIds();
		if (pids != null) {
			for (int i = 0; i < pids.size(); i++) {
				if (userId.equals(pids.get(i))) {
					pids.set(i, null);
					break;
				}
			}
		}
	}

	/**
	 * 语音表情
	 * 
	 * @param session
	 * @param readData
	 * @throws Exception
	 */
	public static void interface_100206(IoSession session, Map<String, Object> readData) throws Exception {
		logger.I("准备,interfaceId -> 100206");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomSn"));
		String userId = String.valueOf(readData.get("userId"));
		String type = String.valueOf(readData.get("type"));
		String idx = String.valueOf(readData.get("idx"));

		Map<String, Object> info = new HashMap<>();
		info.put("roomId", roomId);
		info.put("userId", userId);
		info.put("type", type);
		info.put("idx", idx);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());

		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		List<Player> players = RedisUtil.getPlayerList(roomId, cid);
		for (Player p : players) {
			if (p == null)
				continue;
			if (!p.getUserId().equals(userId)) {
				IoSession se = session.getService().getManagedSessions().get(p.getSessionId());
				if (se != null && se.isConnected()) {
					se.write(pd);
				}
			}
		}
	}

	/**
	 * 记录 1.动作信息 叫地主 加倍 和出牌 2.三个玩家初始牌面信息 3.底牌信息 4.地主产生信息 5.小结算 6.大结算 7.底分发生变化
	 * 叫地主的分*流局 8.加倍改变 炸弹等
	 * 
	 * @param roomId
	 * @param record
	 */
	public static void addRecord(RoomResp room, Map<String, Object> record, String cid) {
		RoomRecordUtil.addRecord(room.getRoomId(), Long.valueOf(room.getCreateTime()), record, false, cid);
	}

	/**
	 * 第一次向文件中写入的数据
	 * @param room
	 * @param players
	 * @param cid
	 */
	public static void createRecord(RoomResp room, List<Player> players, String cid) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("interfaceId", "1");

		JSONArray userInfoArr = new JSONArray();

		// ///////////
		Map<String, Object> user1 = new HashMap<String, Object>();
		user1.put("userId", players.get(0).getUserId());
		user1.put("position", players.get(0).getPosition());
		user1.put("userName", players.get(0).getUserName());
		user1.put("userImg", players.get(0).getUserImg());
		user1.put("gender", players.get(0).getGender());
		user1.put("score", players.get(0).getScore());

		Map<String, Object> user2 = new HashMap<String, Object>();
		user2.put("userId", players.get(1).getUserId());
		user2.put("position", players.get(1).getPosition());
		user2.put("userName", players.get(1).getUserName());
		user2.put("userImg", players.get(1).getUserImg());
		user2.put("gender", players.get(1).getGender());
		user2.put("score", players.get(1).getScore());

		Map<String, Object> user3 = new HashMap<String, Object>();
		user3.put("userId", players.get(2).getUserId());
		user3.put("position", players.get(2).getPosition());
		user3.put("userName", players.get(2).getUserName());
		user3.put("userImg", players.get(2).getUserImg());
		user3.put("gender", players.get(2).getGender());
		user3.put("score", players.get(2).getScore());

		userInfoArr.add(user1);
		userInfoArr.add(user2);
		userInfoArr.add(user3);

		map.put("userInfo", userInfoArr);

		Map<String, Object> roomInfo = new HashMap<String, Object>();
		roomInfo.put("roomId", room.getRoomId() + "");
		roomInfo.put("createTime", room.getCreateTime());
		roomInfo.put("endTime", System.currentTimeMillis() + "");
		roomInfo.put("circleNum", room.getCircleNum() + "");
		roomInfo.put("state", room.getState() + "");
		roomInfo.put("tiShi", room.getTiShi() == null ? "0" : room.getTiShi() + "");
		roomInfo.put("roomType", room.getRoomType() + "");
		roomInfo.put("can4Take2", room.getCan4take2() + "");
		roomInfo.put("laiZi", room.getLaiZi() + "");
		roomInfo.put("dingFen", room.getDingFen() + "");
		roomInfo.put("mulType", room.getMulType() + "");
		roomInfo.put("daPaiJiaoMan", room.getDaPaiJiaoMan() + "");
		roomInfo.put("extraType", room.getExtraType() + "");
		roomInfo.put("mingPaiType", room.getMingPaiType() + "");
		roomInfo.put("diZhuType", room.getDizhuType() + "");
		map.put("roomInfo", roomInfo);
		// 简化
		map = getNewMap(map);

		//FIXME
		if(room.getTotalNum() == 0){
			RoomRecordUtil.addRecord(room.getRoomId(), Long.valueOf(room.getCreateTime()), map, true, cid);
		}else{
			RoomRecordUtil.addRecord(room.getRoomId(), Long.valueOf(room.getCreateTime()), map, false, cid);
		}
	}

	public static void interface_100209(IoSession session, Map<String, Object> readData) {
		logger.I("准备,interface_100209");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomSn"));

		Long userId = Long.valueOf((String) session.getAttribute(Cnst.USER_SESSION_USER_ID));

		if (readData.get("p_x") != null) {
			Integer x = Integer.valueOf(String.valueOf(readData.get("p_x")));
			Integer y = Integer.valueOf(String.valueOf(readData.get("p_y")));
			RedisUtil.setString(Cnst.REDIS_PREFIX_USER_ID_POSITION.concat(userId + ""), x + "_" + y + "_" + System.currentTimeMillis(), Cnst.POSITION_EXPIRE_TIME);
		}

		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);

		RoomResp room = RedisUtil.getRoomRespByRoomId(roomId + "", cid);

		if (!room.getPlayerIds().contains(userId))
			return;// 没必要理会 因为不在这个房间
		List<Long> playerIds = room.getPlayerIds();
		JSONObject json = new JSONObject();
		boolean send = false;
		for (int i = 0; i < playerIds.size(); i++) {
			if (playerIds.get(i) == null)
				continue;
			Long long1 = playerIds.get(i);
			String stringByKey = RedisUtil.getStringByKey(Cnst.REDIS_PREFIX_USER_ID_POSITION.concat(long1 + ""));
			for (int j = i + 1; j < playerIds.size(); j++) {
				if (playerIds.get(j) == null)
					continue;
				send = true;

				Long long2 = playerIds.get(j);

				if (stringByKey == null) {
					json.put(long1 + "_" + long2, -1);
					continue;
				}
				String stringByKey1 = RedisUtil.getStringByKey(Cnst.REDIS_PREFIX_USER_ID_POSITION.concat(long2 + ""));
				if (stringByKey1 == null) {
					json.put(long1 + "_" + long2, -1);
					continue;
				}

				String[] split = stringByKey.split("_");
				int x1 = Integer.valueOf(split[0]);
				int y1 = Integer.valueOf(split[1]);

				String[] split2 = stringByKey1.split("_");
				int x2 = Integer.valueOf(split2[0]);
				int y2 = Integer.valueOf(split2[1]);

				x1 = x1 - x2;
				y1 = y1 - y2;

				json.put(long1 + "_" + long2, (int) Math.floor(Math.sqrt(x1 * x1 + y1 * y1)));
			}
		}
		if (send) {
			JSONObject result = getJSONObj(interfaceId, 1, json);
			ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());

			List<Player> players = RedisUtil.getPlayerList(room, cid);
			if (players != null && players.size() > 0) {
				for (Player p : players) {
					if (p == null)
						continue;
					if (p.getRoomId() != null && p.getRoomId().equals(roomId)) {
						IoSession se = MinaServerManager.tcpServer.getSessions().get(p.getSessionId());
						if (se != null && se.isConnected()) {
							se.write(pd);
						}
					}
				}
			}
		}

	}
}
