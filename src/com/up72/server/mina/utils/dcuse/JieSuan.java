package com.up72.server.mina.utils.dcuse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.server.mina.function.GameFunctions;
import com.up72.server.mina.function.MessageFunctions;
import com.up72.server.mina.function.TCPGameFunctions;
import com.up72.server.mina.utils.RoomRecordUtil;
import com.up72.server.mina.utils.redis.RedisUtil;

public class JieSuan {
	/**
	 * 调用本方法前 请把所有的数据存入Redis
	 * 
	 * @param roomId
	 */
	public static void xiaoJieSuan(RoomResp room,String cid) {
		List<Player> players = RedisUtil.getPlayerList(room,cid);

		//加倍  对应uids 如果是null 说明没选择加倍或者不加倍
		List<Integer> muls = room.getMuls();

		long giveUp = 1l;
		int realGiveUpTime = RoomResp.getRealGiveUpTime(room);
		for (int j = 0; j < realGiveUpTime; j++) {
			giveUp = giveUp * 2l;
		}

		// 先算输的人 和他能给的最多分
		long difen = room.getDiFen();
		difen = difen * giveUp;
		// 检查春天
		if (room.getNongminChu() == null || room.getNongminChu() == 0)
			difen = difen * 2l;
		
		if(room.getDizhuChu() != null && room.getDizhuChu() < 2)
			difen = difen * 2l;
		
		// 检查炸弹
		if (room.getZhandans() != null) {
			for (int j = 0; j < room.getZhandans().size(); j++) {
				difen = difen * 2l;
			}
		}

		ArrayList<Long> arrayList = new ArrayList<Long>();
		arrayList.add(0l);
		arrayList.add(0l);
		arrayList.add(0l);
		
		ArrayList<Long> arrayList1 = new ArrayList<Long>();
		arrayList1.add(0l);
		arrayList1.add(0l);
		arrayList1.add(0l);

		List<Integer> mingPaiInfo = room.getMingPaiInfo();
		if (room.getWinner() == 1) {
			// 地主胜利
			for (int i = 0; i < players.size(); i++) {
				Player p1 = players.get(i);
				long p1m = 1l;
				if (muls.get(i) != null && muls.get(i) == 1) //是否加倍
					p1m = 2l;
				
				if(mingPaiInfo.get(i) != null && mingPaiInfo.get(i) == 1) //是否名牌
					p1m *= 2;
				
				for (int j = i + 1; j < players.size(); j++) {
					Player p2 = players.get(j);
					long p2m = 1l;
					if (muls.get(j) != null && muls.get(j) == 1)
						p2m = 2l;

					if(mingPaiInfo.get(j) != null && mingPaiInfo.get(j) == 1)
						p2m *= 2;
					
					long tmpDifen = difen * p1m * p2m;

					if (tmpDifen > room.getDingFen())
						tmpDifen = room.getDingFen();
					if (p1.getUserId().equals(room.getDizhu())) {
						p1.setScore(p1.getScore() + tmpDifen);
						p2.setScore(p2.getScore() - tmpDifen);
						arrayList.set(i, arrayList.get(i) + tmpDifen);
						arrayList.set(j, arrayList.get(j) - tmpDifen);
						
						
						arrayList1.set(i, arrayList1.get(i) + tmpDifen);
						arrayList1.set(j, arrayList1.get(j) - tmpDifen);
						
					} else if (p2.getUserId().equals(room.getDizhu())) {
						p1.setScore(p1.getScore() - tmpDifen);
						p2.setScore(p2.getScore() + tmpDifen);
						arrayList.set(i, arrayList.get(i) - tmpDifen);
						arrayList.set(j, arrayList.get(j) + tmpDifen);
						
						
						arrayList1.set(i, arrayList1.get(i) - tmpDifen);
						arrayList1.set(j, arrayList1.get(j) + tmpDifen);
					}
				}
			}
		} else {
			// 农民胜利
			for (int i = 0; i < players.size(); i++) {
				Player p1 = players.get(i);
				long p1m = 1l;
				if (muls.get(i) != null && muls.get(i) == 1)
					p1m = 2l;
				
				if(mingPaiInfo.get(i) != null && mingPaiInfo.get(i) == 1)
					p1m *= 2;
				
				for (int j = i + 1; j < players.size(); j++) {
					Player p2 = players.get(j);
					long p2m = 1l;
					if (muls.get(j) != null && muls.get(j) == 1)
						p2m = 2l;

					if(mingPaiInfo.get(j) != null && mingPaiInfo.get(j) == 1)
						p2m *= 2;
					
					long tmpDifen = difen * p1m * p2m;

					if (tmpDifen > room.getDingFen())
						tmpDifen = room.getDingFen();
					if (p1.getUserId().equals(room.getDizhu())) {
						p1.setScore(p1.getScore() - tmpDifen);
						p2.setScore(p2.getScore() + tmpDifen);
						arrayList.set(i, arrayList.get(i) - tmpDifen);
						arrayList.set(j, arrayList.get(j) + tmpDifen);
						
						arrayList1.set(i, arrayList1.get(i) - tmpDifen);
						arrayList1.set(j, arrayList1.get(j) + tmpDifen);
						
					} else if (p2.getUserId().equals(room.getDizhu())) {
						p1.setScore(p1.getScore() + tmpDifen);
						p2.setScore(p2.getScore() - tmpDifen);

						arrayList.set(i, arrayList.get(i) + tmpDifen);
						arrayList.set(j, arrayList.get(j) - tmpDifen);
						
						arrayList1.set(i, arrayList1.get(i) + tmpDifen);
						arrayList1.set(j, arrayList1.get(j) - tmpDifen);
					}
				}
			}
		}
		room.setXiaoJieSuanScore(arrayList);
		
		room.addXiaoJuFen(arrayList1);
		
		room.setTotalNum(room.getTotalNum() + 1);
		// 初始化房间
		for (Player p : players) {
			p.setPlayStatus(Cnst.PLAYER_STATE_over);
		}
		room.setState(Cnst.ROOM_STATE_XJS);
		room.setPlayStatus(Cnst.ROOM_PLAYSTATE_END);

		RedisUtil.setPlayersList(players,cid);
		
		{
			List<Player> ps = RedisUtil.getPlayerList(room,cid);
			
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("interfaceId", "3");
//			map.put("score", arrayList1); //当前分数
			map.put("score", room.getXiaoJieSuanScore());
			map.put("userId", room.getPlayerIds());
			map.put("difen", room.getDiFen());
			map.put("winType", room.getWinner());
			map.put("mul", room.getMuls());
			
			
			//回放的加倍数
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
				Player p = players.get(idx);
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
				room.getRealMuls().add(idx,(int)tmpMul);
			}
			map.put("realMuls", room.getRealMuls());
			
			map.put("spring", room.getSpring());
			
			//设置赢家
			for (int i = 0; i < room.getPlayerIds().size(); i++) {
				if(room.getLastWinner().equals(room.getPlayerIds().get(i))){
					List<Integer> result = room.getResult();
					result.set(i, 1);
					room.setResult(result);
				}
			}
			map.put("result", room.getResult());

			
			map.put("role", room.getRole());
			map.put("mingPai", room.getMingPaiInfo());//明牌情况 null 没选择 0不明牌  1明牌
			map.put("zhadan", room.getZhandans());
			map = GameFunctions.getNewMap(map);
			GameFunctions.addRecord(room, map,cid);
			GameFunctions.createRecord(room, players, cid);
			
		}
		
		if (room.getLastNum() == 0) {
			// 最后一局 大结算
			// room = RedisUtil.getRoomRespByRoomId(roomId);
			room.setState(Cnst.ROOM_STATE_YJS);

			ArrayList<Long> scors = new ArrayList<Long>();
			for (Player player2 : players) {
				if (player2 != null) {
					scors.add(player2.getScore());
				} else {
					scors.add(null);
				}
			}
			room.setScore(scors);
			RedisUtil.updateRedisData(room, null,cid);
			
			// 这里更新数据库吧
			TCPGameFunctions.updateDatabasePlayRecord(room,cid);
			MessageFunctions.interface_100140(room,cid);
			
			//数据记录保存到文件里面
			RoomRecordUtil.save(room.getRoomId(), Long.valueOf(room.getCreateTime()),cid);
			
			if(room.getExtraType().intValue() == Cnst.ROOM_EXTRA_TYPE_2)
			{
				RedisUtil.hdel(Cnst.get_ROOM_DAIKAI_KEY(cid).concat(room.getCreateId() + ""), room.getRoomId() + "");
			}
		} else
			RedisUtil.updateRedisData(room, null,cid);
	}
}
