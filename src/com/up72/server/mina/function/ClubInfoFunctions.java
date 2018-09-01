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
import com.up72.game.dao.ClubMapper;
import com.up72.game.dto.resp.ClubInfo;
import com.up72.game.dto.resp.ClubUser;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.server.mina.bean.ProtocolData;
import com.up72.server.mina.main.MinaServerManager;
import com.up72.server.mina.utils.CommonUtil;
import com.up72.server.mina.utils.StringUtils;
import com.up72.server.mina.utils.redis.RedisUtil;

/**
 * 俱乐部
 */

public class ClubInfoFunctions extends TCPGameFunctions {
	
    /**
     * 扫描二维码查询俱乐部
     * "clubId":"俱乐部id",
     * userId：玩家id
     */
    public static void interface_500001(IoSession session, Map<String, Object>  readData) throws Exception{
        logger.I("准备,interfaceId -> 500001");
        Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer clubId = StringUtils.parseInt(readData.get("clubId"));
		//获取cid
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
    	Map<String, Object> info = new HashMap<>();
		// 通过clubId从redis中获取俱乐部信息  key ： 前缀+cid_clubid ------ value :俱乐部信息     
		ClubInfo redisClub = RedisUtil.getClubInfoByClubId(Cnst.get_REDIS_PREFIX_CLUBMAP(cid)+clubId.toString());
		if (null == redisClub) {// 如果为空 从数据库查询
			redisClub = ClubMapper.selectByClubId(StringUtils.parseInt(clubId),cid);// 根据俱乐部id查询
			// 保存到redis
			RedisUtil.setClubInfoByClubId(Cnst.get_REDIS_PREFIX_CLUBMAP(cid)+clubId.toString(), redisClub);
		}
		if (null != redisClub) {
			info.put("clubId", redisClub.getClubId());
			info.put("clubName", redisClub.getClubName());
			info.put("clubUserName", ClubMapper.selectCreateName(StringUtils.parseInt(redisClub.getCreateId()),cid));
			info.put("allNums", ClubMapper.allUsers(redisClub.getClubId(),cid));
			info.put("createTime", redisClub.getCreateTime());
			info.put("maxNums", redisClub.getPersonQuota());
			// 新添加的俱乐部信息
			info.put("freeStart", redisClub.getFreeStart());
			info.put("freeEnd", redisClub.getFreeEnd());
//			info.put("cid", redisClub.getCid());//不需要给玩家cid信息，自己取着用
		}
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }
    /**
     * 查询我的俱乐部
     */
    public static void interface_500002(IoSession session, Map<String, Object>  readData) throws Exception{
        logger.I("准备,interfaceId -> 500002");
        Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
        
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);

		List<Map<String, Object>> listInfo = new ArrayList<Map<String, Object>>();
		// 查询我加入的俱乐部信息
		List<ClubUser> list = ClubMapper.selectClubByUserId(userId,cid);
		if (list != null && list.size() > 0) {
			for (int a = 0; a < list.size(); a++) {
				Map<String, Object> info = new HashMap<>();
				Integer exState = ClubMapper.selectUserState(StringUtils.parseInt(list.get(a).getClubId()), StringUtils.parseLong(userId),cid);
				// 通过clubId从redis中获取俱乐部信息
				ClubInfo redisClub = RedisUtil.getClubInfoByClubId(Cnst.get_REDIS_PREFIX_CLUBMAP(cid)+list.get(a).getClubId().toString());
				if (null == redisClub) {// 如果为空 从数据库查询
					redisClub = ClubMapper.selectByClubId(list.get(a).getClubId(),cid);// 根据俱乐部id 和玩家的cid 查询
					// 保存到redis
					RedisUtil.setClubInfoByClubId(Cnst.get_REDIS_PREFIX_CLUBMAP(cid)+list.get(a).getClubId().toString(), redisClub);
				}
				if (null != redisClub) {
					info.put("exState",exState);
					info.put("clubId", redisClub.getClubId());
					info.put("clubUserName", ClubMapper.selectCreateName(StringUtils.parseInt(redisClub.getCreateId()),cid));
					info.put("clubName", redisClub.getClubName());
//					info.put("maxNums", redisClub.getPersonQuota());//--不用
					info.put("allNums", ClubMapper.allUsers(redisClub.getClubId(),cid));
					// 限免时间
					info.put("freeStart", redisClub.getFreeStart());
					info.put("freeEnd", redisClub.getFreeEnd());
//					info.put("cid", redisClub.getCid());//不需要给玩家cid信息，自己取着用
				}
				listInfo.add(info);
			}
		}
        JSONObject result = getJSONObj(interfaceId,1,listInfo);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }
    /**
     * 申请加入俱乐部
     */
    public static void interface_500000(IoSession session, Map<String, Object>  readData) throws Exception{
        logger.I("准备,interfaceId -> 500000");
    	Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		Integer clubId = StringUtils.parseInt(readData.get("clubId"));
		
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		
        Map<String, Object> info = new HashMap<>();
		ClubUser user = ClubMapper.selectUserByUserIdAndClubId(userId, clubId,cid);
		if (null != user) {//
			info.put("reqState", 3);
		} else {
			// 根据cid和玩家id查询我加入的俱乐部
			Integer count = ClubMapper.countByUserId(StringUtils.parseLong(userId),cid);
			if (null != count && count >= 3) {// 如果加入的大于3个
				info.put("reqState", 2);
			} else {
				ClubUser clubUser = new ClubUser();
				clubUser.setUserId(StringUtils.parseLong(userId));
				clubUser.setClubId(StringUtils.parseInt(clubId));
				clubUser.setStatus(0);// 默认申请中
				clubUser.setCreateTime(new Date().getTime());// 申请时间
				clubUser.setCid(Integer.valueOf(cid));//游戏所在的cid
				ClubMapper.insert(clubUser);// 保存
				info.put("reqState", 1);
			}
		}
        
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }
    
    /**
     * 申请离开俱乐部
     */
    public static void interface_500007(IoSession session, Map<String, Object>  readData) throws Exception{
        logger.I("准备,interfaceId -> 500007");
        Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		Integer clubId = StringUtils.parseInt(readData.get("clubId"));
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		if (clubId==null ||  userId== null) {
			return;
		}
        
        Map<String, Object> info = new HashMap<>();
		// 根据用户id 和通过状态 查询
		ClubUser clubUser = ClubMapper.selectUserByUserIdAndClubId(userId, clubId,cid);
		if (null != clubUser) {
			ClubInfo redisClub = RedisUtil.getClubInfoByClubId(Cnst.get_REDIS_PREFIX_CLUBMAP(cid)+clubId.toString());
			if (null == redisClub) {// 如果为空 从数据库查询
				redisClub = ClubMapper.selectByClubId(StringUtils.parseInt(clubId),cid);// 根据俱乐部id查询
				// 保存到redis
				RedisUtil.setClubInfoByClubId(Cnst.get_REDIS_PREFIX_CLUBMAP(cid)+clubId.toString(), redisClub);
			}
			if(null!=redisClub){
				//创建者不能离开俱乐部
				if(redisClub.getCreateId().equals(userId)){
					return ;
				}
			}
			if (clubUser.getStatus() == 1) {
				info.put("reqState", 1);
				clubUser.setStatus(2);// 状态 状态 0申请加入 1已通过 2申请退出
				ClubMapper.updateById(clubUser);// 修改保存记录
			} else if (clubUser.getStatus() == 2) {
				info.put("reqState", 2);
			}
		}
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }
    /**
     * 查询俱乐部详情
     */
    public static void interface_500003(IoSession session,  Map<String, Object>  readData) throws Exception{
        logger.I("准备,interfaceId -> 500003");
        Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		Integer clubId = StringUtils.parseInt(readData.get("clubId"));
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);

        Map<String, Object> info = new HashMap<>();
		// 根据用户id 和通过状态 查询
		ClubInfo clubInfo = RedisUtil.getClubInfoByClubId(Cnst.get_REDIS_PREFIX_CLUBMAP(cid)+clubId.toString());
		if (null == clubInfo) {// 如果为空 从数据库查询
			clubInfo = ClubMapper.selectByClubId(clubId,cid);// 根据俱乐部id查询
			// 保存到redis
			RedisUtil.setClubInfoByClubId(Cnst.get_REDIS_PREFIX_CLUBMAP(cid)+clubId.toString(), clubInfo);
		}
		if (null != clubInfo) {
			info.put("clubId", clubInfo.getClubId());
			info.put("clubName", clubInfo.getClubName());
			info.put("clubUserName", ClubMapper.selectCreateName(StringUtils.parseInt(clubInfo.getCreateId()),cid));
			info.put("clubMoney", clubInfo.getRoomCardNum());
			info.put("cardQuota", clubInfo.getRoomCardQuota());
			info.put("allNums", ClubMapper.allUsers(clubId,cid));
			// 限免时间
			info.put("freeStart", clubInfo.getFreeStart());
			info.put("freeEnd", clubInfo.getFreeEnd());
		}
//		
		//俱乐部不存在
		if(clubInfo==null){
			JSONObject result = getJSONObj(interfaceId, 1, "");
	        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());	
	        session.write(pd);
	        return;
		}

		// 根据俱乐部id和时间 查询今天消费房卡数
		Integer used = ClubMapper.sumMoneyByClubIdAndDate(clubId,cid);
		info.put("used", used == null ? 0 : used);
		// 根据俱乐部id和时间查询
		//1:当天的活跃人数
//		Integer actNum = ClubMapper.todayPerson(cid,Integer.valueOf(clubId.toString())).size();
		Long actNum = RedisUtil.scard(Cnst.get_REDIS_CLUB_ACTIVE_PERSON(cid).concat(clubId+"_").concat(StringUtils.getTimesmorning()+""));

		if(actNum==null ||actNum==0l){
			actNum=0l;
		}else{
			//因为设置过期时间时多了条假数据，需要删除
			actNum=actNum-1;
		}
		info.put("actNum", actNum.intValue());
		//今天的开局数
		//走缓存
		Integer juNum = RedisUtil.getObject(Cnst.get_REDIS_CLUB_TODAYKAI_NUM(cid).concat(clubId+"_").concat(StringUtils.getTimesmorning()+""), Integer.class);
		info.put("juNum", juNum == null ? 0 : juNum);
//		Integer juNum = ClubMapper.todayGames(cid,Integer.valueOf(clubId.toString()));
//		info.put("juNum", juNum == null ? 0 : juNum);
		// 根据俱乐部id和userid查询当前状态
		Integer exState = ClubMapper.selectUserState(StringUtils.parseInt(clubId), StringUtils.parseLong(userId),cid);

		// 俱乐部页面刷新 此时管理员已同意退出
		if (exState == null) {
			info.put("reqState", 0);
		} else {
			info.put("exState", exState);
		}
		/************************** 未开局的房间数 **********************************/

		JSONArray jsonArrayInfo = new JSONArray();
		//cid+clubId为key  key2 为roomId集合
		//TODO 什么时候删除房间id
		Map<String, String> roomMap = RedisUtil.hgetAll(Cnst.get_REDIS_CLUB_ROOM_LIST(cid).concat(clubId.toString()));
		if (roomMap.isEmpty()) {
			// 社么也不用做处理 似乎
		} else {

			for (String roomId : roomMap.keySet()) {
				RoomResp room = RedisUtil.getRoomRespByRoomId(roomId,cid);
				if (room == null || room.getState() != Cnst.ROOM_STATE_CREATED) {
					// 房间已解散
					RedisUtil.hdel(Cnst.get_REDIS_CLUB_ROOM_LIST(cid).concat(String.valueOf(clubId)), roomId);
				} else {
					JSONObject jsobj = new JSONObject();
					JSONObject roomobj = new JSONObject();
					jsobj.put("roomId", room.getRoomId());
					List<Long> playerIds = room.getPlayerIds();
					List<JSONObject> players=new ArrayList<JSONObject>();
					Player player;
					int num = 0;
					for (Long uid : playerIds) {
						if(uid==null){
							continue;
						}
						num++;
						JSONObject jb = new JSONObject();
						player = RedisUtil.getPlayerByUserId(uid+"", cid);
						jb.put("userName", player.getUserName());
						jb.put("userImg", player.getUserImg());
						IoSession se = MinaServerManager.tcpServer.getSessions().get(player.getSessionId());
						if(se==null){
							jb.put("state", 2);
						}else{
							jb.put("state", 1);
						}

						players.add(jb);
					}
					jsobj.put("users", players);
					jsobj.put("num", num);// 当前人数
					roomobj.put("circleNum", room.getCircleNum());
					roomobj.put("mingPaiType", room.getMingPaiInfo());
					roomobj.put("diZhuType", room.getDizhuType());
					roomobj.put("can4Take2", room.getCan4take2().intValue()==0?0:1);
					roomobj.put("mulType", room.getMulType());
					roomobj.put("dingFen", room.getDingFen());
					roomobj.put("daPaiJiaoMan", room.getDaPaiJiaoMan());
					roomobj.put("roomType", room.getRoomType());
					roomobj.put("laiZi", room.getLaiZi());
					jsobj.put("rule", roomobj);
					jsonArrayInfo.add(jsobj);
				}

			}
		}
    	  
        info.put("rooms", jsonArrayInfo);
        /**************************未开局的房间数结束**********************************/
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }
    /**
     * 查询我的战绩
     */
    public static void interface_500006(IoSession session,  Map<String, Object> readData) throws Exception{
        logger.I("准备,interfaceId -> 500006");
        Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		Integer clubId = StringUtils.parseInt(readData.get("clubId"));
		Integer date = StringUtils.parseInt(readData.get("date"));// 1 今日 2 明日
		Integer page = Integer.parseInt(String.valueOf(readData.get("page")));
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		//玩家今日局数
		Long timesmorning = StringUtils.getTimesmorning();

		Integer juNum = RedisUtil.getObject(Cnst.getREDIS_CLUB_TODAYJUNUM_ROE_USER(cid).concat(clubId+"_").concat(userId+"_").concat(timesmorning+""), Integer.class);
//		Integer juNum = ClubMapper.userTodayGames(clubId, userId,cid);
		String redisDate = null;
        if (date == 1) {
			// 今天
			redisDate = StringUtils.toString(StringUtils.getTimesmorning());
		} else {
			//昨天
			redisDate = StringUtils.toString(StringUtils.getYesMoring());
		}
//		// 根据cid 俱乐部id，人员id和时间查询   value  roomId战绩集合  
		String userKey = Cnst.get_REDIS_CLUB_PLAY_RECORD_PREFIX_ROE_USER(cid).concat(clubId+"_").concat(userId+"_") + redisDate;
       //
        Long pageSize = RedisUtil.llen(userKey);
		int start = (page - 1) * Cnst.PAGE_SIZE;
		int end = start + Cnst.PAGE_SIZE - 1;
		List<String> keys = RedisUtil.lrange(userKey, start, end);
		JSONObject info = new JSONObject();
		List<Map<String, String>> maps = new ArrayList<Map<String, String>>();
		for (String roomKey : keys) {
			Map<String, String> roomInfos = RedisUtil.hgetAll(Cnst.get_REDIS_CLUB_PLAY_RECORD_PREFIX(cid).concat(roomKey));
			maps.add(roomInfos);
		}
		info.put("infos", maps);
		//今日总成绩需要
		String key2 = Cnst.REDIS_CLUB_TODAYSCORE_ROE_USER +cid+"_"+ clubId + "_" + userId + "_" + StringUtils.getTimesmorning();
		if (RedisUtil.exists(key2)) {
			Integer score = RedisUtil.getObject(key2, Integer.class);
			info.put("score", score);
		} else {
			info.put("score", 0);
		}
		info.put("pages", pageSize == null ? 0 : pageSize % Cnst.PAGE_SIZE == 0 ? pageSize / Cnst.PAGE_SIZE : (pageSize / Cnst.PAGE_SIZE + 1));
		info.put("juNum", juNum == null ? 0 : juNum);
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
    }
    /**
     * 俱乐部创建房间
     */
	public static void interface_500004(IoSession session,Map<String, Object> readData) {
		logger.I("创建房间,interfaceId -> 500004");
		
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer clubId = StringUtils.parseInt(readData.get("clubId"));// 玩法选项
		Long userId = StringUtils.parseLong(readData.get("userId"));
		// 局数
		Integer circleNum = StringUtils.parseInt(readData.get("circleNum"));
		// 房间类型 ROOM_TYPE_1 经典 ROOM_TYPE_2 闪斗
		Integer roomType = StringUtils.parseInt(readData.get("roomType"));
		Integer mingPaiType = StringUtils.parseInt(readData.get("mingPaiType"));
		Integer diZhuType = StringUtils.parseInt(readData.get("diZhuType"));
		// 是否可以4带2
		Integer tmpCan4take2 = StringUtils.parseInt(readData.get("can4Take2"));
		Integer can4take2 = tmpCan4take2.intValue() == 0 ? 0 : 1;
		
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);

		RoomResp room = new RoomResp();
		//记录未开局的房间
		Map<String, String> roomMap = RedisUtil.hgetAll(Cnst.get_REDIS_CLUB_ROOM_LIST(cid).concat(String.valueOf(clubId)));
		if (roomMap.isEmpty()) {
			// 似乎也不用做处理
		} else {
			if (roomMap.keySet().size() >= 5) {
				JSONObject error = new JSONObject();
				error.put("reqState", 12);
				JSONObject result = getJSONObj(interfaceId, 1, error);
		        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		        session.write(pd);				
		        return;
			}
		}
		//获取玩家当前是否能继续开房间
		// 通过clubId从redis中获取俱乐部信息
		ClubInfo redisClub = RedisUtil.getClubInfoByClubId(Cnst.get_REDIS_PREFIX_CLUBMAP(cid)+clubId.toString());
		if (redisClub == null) {
			redisClub = ClubMapper.selectByClubId(StringUtils.parseInt(clubId),cid);// 根据俱乐部id查询
		}
		// 俱乐部房卡不足
		if(redisClub.getRoomCardNum()<=200){
			JSONObject error = new JSONObject();
			error.put("reqState", 14);
			JSONObject result = getJSONObj(interfaceId, 1, error);
			ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
			session.write(pd);	
			return;
		}
		// 保存到redis
		// 超过玩家每日限额
		int max = redisClub.getRoomCardQuota();
		// 获取当前玩家一天的房卡数
		Integer needMoney;
		Integer todayUse = ClubMapper.todayUse(clubId, StringUtils.parseInt(userId),cid);
		if (roomType.intValue() == Cnst.ROOM_TYPE_1) {
			needMoney = Cnst.moneyMap_1.get(circleNum);
		}else{
			needMoney=Cnst.moneyMap_2.get(circleNum);
		}
		// 房卡不能超过限制(此处需要根据人数进行修改)
		if ((todayUse + needMoney / 4) > max) {
			JSONObject error = new JSONObject();
			error.put("reqState", 13);
			JSONObject result = getJSONObj(interfaceId, 1, error);
			ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
			session.write(pd);		
			return;
		}
		//俱乐部房卡是否充足
		Long freeStart = redisClub.getFreeStart();
		Long freeEnd = redisClub.getFreeEnd();
		long currentTimeMillis = System.currentTimeMillis();
		Boolean isFree = false;
		if (currentTimeMillis >= freeStart && currentTimeMillis <= freeEnd) {// 限免时间满足
			// 不用做判断
			isFree = true;
		}
		if (!isFree) {// 如果不是限免时间。
			if (redisClub.getRoomCardNum() < needMoney) {// 俱乐部房卡不足
				playerMoneyNotEnough(interfaceId, session, roomType);
				return;
			}
		}

		
		// 积分
		if (roomType == null
				|| (roomType != Cnst.ROOM_TYPE_2 && roomType != Cnst.ROOM_TYPE_1)) {
			illegalRequest(interfaceId, session);
			return;
		}

		
		if(mingPaiType != Cnst.MINGPAY_TYPE_1 && mingPaiType != Cnst.MINGPAY_TYPE_2)
		{
			illegalRequest(interfaceId, session);
			return;
		}
		
		if(diZhuType != Cnst.DIZHU_TYPE_2 && diZhuType != Cnst.DIZHU_TYPE_3)
		{
			illegalRequest(interfaceId, session);
			return;
		}


		// 是否可以是癞子 如果是经典斗 不考虑这个选项

		Integer laizi = 0;
		if (roomType.intValue() == Cnst.ROOM_TYPE_2) {
			Integer tmplaizi = StringUtils.parseInt(readData.get("isLaiZi"));
			laizi = tmplaizi.intValue() == 0 ? 0 : 1;
		}

		// 加倍规则1不加倍 2农民优先 3都可以加倍
		Integer mul = StringUtils.parseInt(readData.get("mul"));

		if (mul.intValue() != Cnst.ROOM_MUL_TYPE_2
				&& mul.intValue() != Cnst.ROOM_MUL_TYPE_3
				&& mul.intValue() != Cnst.ROOM_MUL_TYPE_1) {
			illegalRequest(interfaceId, session);
			return;
		}

		Player p = RedisUtil.getPlayerByUserId(String.valueOf(session
				.getAttribute(Cnst.USER_SESSION_USER_ID)),cid);


		// 封顶分数
		Integer dingFeng = StringUtils.parseInt(readData.get("dingFen"));

		if (dingFeng == null) {
			illegalRequest(interfaceId, session);
			return;
		}

		if (dingFeng.intValue() != Cnst.ROOM_MAX_SCORE_2
				&& dingFeng.intValue() != Cnst.ROOM_MAX_SCORE_3
				&& dingFeng.intValue() != Cnst.ROOM_MAX_SCORE_4
				&& dingFeng.intValue() != Cnst.ROOM_MAX_SCORE_5
				&& dingFeng.intValue() != Cnst.ROOM_MAX_SCORE_1) {
			illegalRequest(interfaceId, session);
			return;
		}

		if (p.getRoomId() != null) {// 已存在其他房间
			playerExistOtherRoom(interfaceId, session);
			return;
		}
		

		Integer daPaiJiaoMan = StringUtils.parseInt(readData
				.get("daPaiJiaoMan"));

		if (daPaiJiaoMan.intValue() != Cnst.ROOM_JIAO_TYPE_1
				&& daPaiJiaoMan.intValue() != Cnst.ROOM_JIAO_TYPE_2) {
			illegalRequest(interfaceId, session);
			return;
		}
		
		room.setMingPaiType(mingPaiType);
		room.setDizhuType(diZhuType);
		
		long now = System.currentTimeMillis();
		while (true){
			room.setRoomId(CommonUtil.getGivenRamdonNum(7));//设置随机房间密码
			
			if(HallFunctions.roomIdMap.containsKey(room.getRoomId()))
				continue;// 有个正在用
			HallFunctions.roomIdMap.put(room.getRoomId(), now);
			
			Long long1 = HallFunctions.roomIdMap.get(room.getRoomId());
			if(long1 == null || long1.longValue() != now)
				continue;
			//已经存在这个房间
			if(RedisUtil.exists(Cnst.get_REDIS_PREFIX_ROOMMAP(cid).concat(room.getRoomId() + "")))
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
		room.setExtraType(1);//只有可能是房主模式
		room.setCid(cid);
		room.setClubId(clubId);
		// 初始化大接口的id
		room.setWsw_sole_action_id(1);
		room.setWsw_sole_main_id(1);

		List<Long> userIds = new ArrayList<Long>();

		Map<String, Object> info = new HashMap<>();
		Map<String, Object> userInfos = new HashMap<String, Object>();

		userIds.add(userId);
		p.setRoomId(room.getRoomId());

		// 设置用户信息
		p.setPosition(1);// 标识位置 不存在东西南北
		p.setPlayStatus(Cnst.PLAYER_STATE_IN);// 进入房间状态

		p.setJoinIndex(1);

		// 初始化 用户 初始给1000积分
		p.initPlayer(p.getRoomId(), Cnst.PLAYER_STATE_IN, 0l);

		room.setPlayerIds(userIds);

		info.put("reqState", Cnst.REQ_STATE_1);
		// 扣除房卡
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
		if (room.getRoomType() == Cnst.ROOM_TYPE_2){
			readData.put("isLaiZi", room.getLaiZi());
		}
		readData.put("dingFen", room.getDingFen());
		readData.put("daPaiJiaoMan", room.getDaPaiJiaoMan());
		
		readData.put("mingPaiType", room.getMingPaiType() + "");
		readData.put("diZhuType", room.getDizhuType() + "");
		
		readData.remove("interfaceId");

		// 记录房间信息   --存储房间的实体对象
		RedisUtil.setObject(
				Cnst.get_REDIS_PREFIX_ROOMMAP(cid).concat(room.getRoomId() + ""), room,
				Cnst.ROOM_LIFE_TIME_CREAT);

		//记录房间存储俱乐部RoomId
		RedisUtil.hset(Cnst.get_REDIS_CLUB_ROOM_LIST(cid).concat(String.valueOf(clubId)),room.getRoomId()+"","1",Cnst.ROOM_LIFE_TIME_CREAT);

		HallFunctions.roomIdMap.remove(room.getRoomId());
		
		// 更新redis数据 player roomMap
		RedisUtil.updateRedisData(null, p,cid);
		if (!isFree) {// 如果不是限免时间。
			redisClub.setRoomCardNum(redisClub.getRoomCardNum()-needMoney);
			RedisUtil.setClubInfoByClubId(Cnst.get_REDIS_PREFIX_CLUBMAP(cid)+clubId.toString(), redisClub);
		}
		// 解散房间超时任务开启
		startDisRoomTask(room.getRoomId(), Cnst.DIS_ROOM_TYPE_1,cid);
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
	public synchronized static void interface_500005(IoSession session,
			Map<String, Object> readData) throws Exception {
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomSn"));
		
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		
		Player p = RedisUtil.getPlayerByUserId(String.valueOf(session
				.getAttribute(Cnst.USER_SESSION_USER_ID)),cid);

		// 已经在其他房间里
		if (p.getRoomId() != null) {// 玩家已经在非当前请求进入的其他房间里
			playerExistOtherRoom(interfaceId, session);
			return;
		}
		// 房间不存在
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId),cid);
		if (room == null || room.getState() == Cnst.ROOM_STATE_YJS) {
			roomDoesNotExist(interfaceId, session);
			return;
		}
		
		//通过该玩家id查找他所在的俱乐部
		List<Integer> clubIds = ClubMapper.selectClubIdsByUserId(userId,cid);
		if(!clubIds.contains(room.getClubId())){
			JSONObject error = new JSONObject();
			error.put("reqState", Cnst.REQ_STATE_16);
			JSONObject result = getJSONObj(interfaceId, 1, error);
			ProtocolData pd = new ProtocolData(interfaceId,result.toJSONString());
			session.write(pd);		
			return;
		}
		
		Integer needMoney = 0;
		Integer circleNum = room.getCircleNum();
		Integer roomType = room.getRoomType();
		if (roomType.intValue() == Cnst.ROOM_TYPE_1) {
			needMoney = Cnst.moneyMap_1.get(circleNum);
		} else {
			needMoney = Cnst.moneyMap_2.get(circleNum);
		}
		//加入房间的玩家房卡是否达到上线
		Integer clubId = room.getClubId();
		ClubInfo redisClub = RedisUtil.getClubInfoByClubId(Cnst.get_REDIS_PREFIX_CLUBMAP(cid)+clubId.toString());
		// 超过每日限额
		int max = redisClub.getRoomCardQuota();
		// 获取当前玩家一天的房卡数
		Integer todayUse = ClubMapper.todayUse(clubId, StringUtils.parseInt(userId),cid);

		// 房卡不能超过限制(此处需要根据人数进行修改)
		if ((todayUse + needMoney / 4) > max) {
			JSONObject error = new JSONObject();
			error.put("reqState", 13);
			JSONObject result = getJSONObj(interfaceId,1, error);
			ProtocolData pd = new ProtocolData(interfaceId,
					result.toJSONString());
			session.write(pd);
			return;
		}
		
		// 房间人满
		List<Long> playerIds = room.getPlayerIds();

		boolean haveNull = false;
		int joinIdx = 0;
		for (Long long1 : playerIds) {
			if (long1 == null) {
				haveNull = true;
			} else
				++joinIdx;
		}

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
			ProtocolData pd = new ProtocolData(interfaceId,
					result.toJSONString());
			session.write(pd);
			return;
		}

		// 设置用户信息
//		p.setPlayStatus(Cnst.PLAYER_STATE_PREPARED);// 准备状态
		p.setRoomId(roomId);
		p.setJoinIndex(joinIdx + 1);
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
			p.setPosition(playerIds.size());
		}
		// 初始化用户
		p.initPlayer(p.getRoomId(), Cnst.PLAYER_STATE_IN, 0l);

		JSONArray players = new JSONArray();

		// 更新redis数据
		RedisUtil.updateRedisData(room, p,cid);

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
				Player otherPlayer = RedisUtil.getPlayerByUserId(String
						.valueOf(ids),cid);

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
			Player pp = RedisUtil.getPlayerByUserId(ids + "",cid);
			IoSession se = MinaServerManager.tcpServer.getSessions().get(
					pp.getSessionId());
			// 返回给房间内其他玩家
			if (se != null && se.isConnected()) {
				JSONObject result1 = getJSONObj(interfaceId, 1, userInfos);
				ProtocolData pd1 = new ProtocolData(interfaceId,
						result1.toJSONString());
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

	}
	
	
	
	
	
	
	
	
	
	
	
	
	/**
     * 产生随机的风
     */
    protected static Integer getWind(Long[] userIds){
        List<Integer> ps = new ArrayList<>();
        ps.add(Cnst.WIND_EAST);
        ps.add(Cnst.WIND_SOUTH);
        ps.add(Cnst.WIND_WEST);
        ps.add(Cnst.WIND_NORTH);
        if (userIds!=null){
            for(int i=userIds.length-1;i>=0;i--){
                if (userIds[i]!=null){
                    ps.remove(i);
                }
            }
        }
        return ps.get(CommonUtil.getRamdonInNum(ps.size()));
    }
   
}
