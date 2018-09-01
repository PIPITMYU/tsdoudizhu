package com.up72.game.dto.resp;

import com.up72.game.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2017/6/26.
 */
public class Player extends User {

	private Integer roomId;// 房间密码，也是roomSn
	private Integer state;// out离开状态（断线）;inline正常在线；
	private List<Card> currentCardList;// 用户手中当前的牌
//	private List<Card> startCardList;//用户最初手牌
	private Integer position;// 位置信息；详见Cnst
	private String ip;//所在服务器ip 需与加入房间ip一致
	private Long score;// 玩家积分；初始为1000，待定
	private String notice;// 跑马灯信息
	private Integer playStatus;// dating用户在大厅中; in刚进入房间，等待状态; prepared准备状态; gameing over
	private Integer joinIndex;// 加入顺序
	private Long sessionId;
	private Integer winType;//赢的类型
	private Long updateTime;//更新时间 登录时候 如果超过3天就更新一次用户数据
	
	
	public void initPlayer(Integer roomId,Integer playStatus,Long score){
		if(roomId == null){
			this.position = null;
			this.joinIndex = null;			
		}
		this.roomId = roomId;
		this.winType = null;
		this.currentCardList = new ArrayList<Card>();
		this.playStatus = playStatus;
		this.score = score;
	}
	public Integer getRoomId() {
		return roomId;
	}


	public void setRoomId(Integer roomId) {
		this.roomId = roomId;
	}


	public Integer getState() {
		return state;
	}


	public void setState(Integer state) {
		this.state = state;
	}


	public List<Card> getCurrentCardList() {
		return currentCardList;
	}


	public void setCurrentCardList(List<Card> currentCardList) {
		this.currentCardList = currentCardList;
	}




	public Integer getPosition() {
		return position;
	}


	public void setPosition(Integer position) {
		this.position = position;
	}
	
	public String getIp() {
		return ip;
	}


	public void setIp(String ip) {
		this.ip = ip;
	}



	public Long getScore() {
		return score;
	}
	public void setScore(Long score) {
		this.score = score;
	}
	public String getNotice() {
		return notice;
	}


	public void setNotice(String notice) {
		this.notice = notice;
	}


	public Integer getPlayStatus() {
		return playStatus;
	}


	public void setPlayStatus(Integer playStatus) {
		this.playStatus = playStatus;
	}


	public Integer getJoinIndex() {
		return joinIndex;
	}


	public void setJoinIndex(Integer joinIndex) {
		this.joinIndex = joinIndex;
	}


	public Long getSessionId() {
		return sessionId;
	}


	public void setSessionId(Long sessionId) {
		this.sessionId = sessionId;
	}


	public Integer getWinType() {
		return winType;
	}


	public void setWinType(Integer winType) {
		this.winType = winType;
	}
	
	//发牌
	public void dealCard(Card card){
		this.currentCardList.add(card);
	}
	public Long getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Long updateTime) {
		this.updateTime = updateTime;
	}
}
