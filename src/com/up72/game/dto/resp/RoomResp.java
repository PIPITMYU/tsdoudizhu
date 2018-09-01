package com.up72.game.dto.resp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.up72.game.constant.Cnst;
import com.up72.game.model.Room;
import com.up72.server.mina.bean.DissolveRoom;

/**
 * Created by Administrator on 2017/7/8.
 */
public class RoomResp extends Room {
	
	
	private List<Card> currentCardList = new ArrayList<Card>();//房间内剩余牌集合；
    private Integer state;//本房间状态，1等待玩家入坐；2游戏中；3小结算
    private Integer lastNum;//房间剩余局数
    private Integer totalNum;//当前第几局
  
    
    private Integer diFen;//底分  = 叫的分数 * 流局次数
    private DissolveRoom dissolveRoom;//申请解散信息

    private Long lastUserId;//最后出牌的玩家
    private Integer lastAction;//最后一个人的动作
    private Integer lastActionExtra;//最后一个人的扩展动作
    
    private List<Card> realLastChuPai;//最后出的牌
    private Long realLastUserId;//最后出牌的玩家ID.不包含过的情况  
    private Integer realLastAction;//最后一个人的动作
    
    private Long currentUserId;//当前玩家UID,需要有操作的玩家
    private List<Integer> currentUserAction;//当前需要操作动作的玩家
    
    private List<ArrayList<Card>> cards;//卡牌所有人出的牌 如果是是NULL说明没发言 如果对应的size是空说明过了 如果有说明他出的牌
    
    //上面 cards每次记录的是玩家的出的牌 或者过牌 是实时的 但是这个比cards晚记录一个人，比如：  A->B->C  C操作完会立刻记录到cards里面 但是这个时候才刚刚把B的动作放入lastCards
    private List<ArrayList<Card>> lastCards; //上一次手牌
    
    private Integer winner;//0农民 1地主赢
    private List<Integer> paiNumList; //玩家剩余牌的数量
    private Long xjst;//小局开始时间
    private Integer playStatus;//游戏中房间状态
    private Integer createDisId;
    private Integer applyDisId;
    private Integer outNum;//请求大接口的玩家次数

    private Integer wsw_sole_main_id;//大接口id 暂时没用
    private Integer wsw_sole_action_id;//吃碰杠出牌发牌id
    
    private String openName;//房主id
    private List<Long> playerIds;//玩家id集合
    private List<Integer> muls;//加倍  对应uids 如果是null 说明没选择加倍或者不加倍
    private List<Integer> realMuls; // 每个玩家对应的顺序,是具体的小结算最后的实际倍数.
    private List<Integer> jiaoDiZhu;//叫地主详细数据 对应玩家 如果是为null 说明这个玩家没有发言过
    private List<Integer> zhandans;//所有人的炸弹都在这里
    private List<Long> score;//最后大结算 所有玩家的积分放在这里  这是一个累计的积分
    private List<Integer> spring ; //春天对于uids 0不是春天,1是春天
    private List<Integer> result; //对于uids 0是失败,1是赢了
    private List<Integer> role ; //对于uids 0是农民,1是地主
    
    private Integer nongminChu;//农民出牌的次数 非0就不是春天
    private Integer dizhuChu;//地主出牌次数不超过2次就算天
    private Long firstJiaoDiZhu;//第一个叫地主的人 除了这个人其他人都是抢地主
    private Integer xiaoJuNum;//每次小局，这个字段++，回放用
    
    private List<Long> xiaoJieSuanScore;//小结算的分数
    
    private List<ArrayList<Integer>> zhandanInfo;
    
    private Long firstTakePaiUser;//第一个出牌的玩家
    
    private List<Integer> mingPaiInfo;//明牌情况 null 没选择 0不明牌  1明牌
    
    //这个数据很特殊 如果流局之后的第一次产生地主 不要把这个数据直接置位0 要+10000 等重新开始游戏initRoom是否检查这个值是否>10000再置位0
    private Integer giveUpTime;//放弃次数 所有人都不交地主的的次数 每次有地主产生置位0

    private Long dizhu;
    
    private List<List<Long>> xiaoJuFen = new ArrayList<List<Long>>();
    
    private Long lastWinner;
    
    private String cid;
    
    /**
     * 轮流叫地主
     */
    private Long lastJiaoDiZhu;

	public void initRoom(){
    	this.currentCardList = new ArrayList<Card>();
    	this.playStatus = Cnst.ROOM_PLAYSTATE_JIAODIZHU;
    	muls = new ArrayList<Integer>();
    	realMuls = new ArrayList<Integer>();
    	jiaoDiZhu = new ArrayList<Integer>();
    	zhandans = new ArrayList<Integer>();
    	paiNumList = new ArrayList<Integer>();
    	firstJiaoDiZhu = null;
    	
    	lastAction = null;
    	lastUserId = null;
    	lastActionExtra = null;
    	realLastAction = null;
    	realLastChuPai = null;
    	realLastUserId = null;
 
    	currentUserAction = null;
    	currentUserId = null;
    	dizhu = null;
    	winner = null;
    	nongminChu = 0;
    	dizhuChu = 0;
    	xiaoJieSuanScore = null;
    	zhandanInfo = new ArrayList<ArrayList<Integer>>();
    	result = new ArrayList<Integer>();
    	mingPaiInfo = new ArrayList<Integer>();
    	spring = new ArrayList<Integer>();
    	role = new ArrayList<Integer>();
    	if(giveUpTime != null && giveUpTime > 9999)
    		giveUpTime = 0;
    	
    	
    	cards = new ArrayList<ArrayList<Card>>();
    	lastCards = new ArrayList<ArrayList<Card>>();
    	for (int i = 0; i < 3; i++) {
			muls.add(null);
			jiaoDiZhu.add(null);
			zhandanInfo.add(new ArrayList<Integer>());
			cards.add(null);
			lastCards.add(null);
			role.add(0);
			spring.add(0);
			result.add(0);
			paiNumList.add(0); //玩家剩余牌的数量
	    	mingPaiInfo.add(null);
		}
    	
    }
	
	
	public List<Integer> getPaiNumList() {
		return paiNumList;
	}


	public void setPaiNumList(List<Integer> paiNumList) {
		this.paiNumList = paiNumList;
	}


	public List<Integer> getRealMuls() {
		return realMuls;
	}

	public void setRealMuls(List<Integer> realMuls) {
		this.realMuls = realMuls;
	}

	public List<Integer> getRole() {
		return role;
	}

	public void setRole(List<Integer> role) {
		this.role = role;
	}

	public List<Integer> getResult() {
		return result;
	}


	public void setResult(List<Integer> result) {
		this.result = result;
	}


	public List<Integer> getSpring() {
		return spring;
	}

	public void setSpring(List<Integer> spring) {
		this.spring = spring;
	}

	public List<Card> getCurrentCardList() {
		return currentCardList;
	}
	public void setCurrentCardList(List<Card> currentCardList) {
		this.currentCardList = currentCardList;
	}
	
	public Integer getState() {
		return state;
	}
	public void setState(Integer state) {
		this.state = state;
	}
	public Integer getLastNum() {
		return lastNum;
	}
	public void setLastNum(Integer lastNum) {
		this.lastNum = lastNum;
	}
	public Integer getTotalNum() {
		return totalNum;
	}
	public void setTotalNum(Integer totalNum) {
		this.totalNum = totalNum;
	}
	public Integer getDiFen() {
		return diFen;
	}
	public void setDiFen(Integer diFen) {
		this.diFen = diFen;
	}
	public DissolveRoom getDissolveRoom() {
		return dissolveRoom;
	}
	public void setDissolveRoom(DissolveRoom dissolveRoom) {
		this.dissolveRoom = dissolveRoom;
	}
//	public List<Card> getLastChuPai() {
//		return lastChuPai;
//	}
//	public void setLastChuPai(List<Card> lastChuPai) {
//		this.lastChuPai = lastChuPai;
//	}
	public Long getLastUserId() {
		return lastUserId;
	}
	public void setLastUserId(Long lastUserId) {
		this.lastUserId = lastUserId;
	}
	public Long getXjst() {
		return xjst;
	}
	public void setXjst(Long xjst) {
		this.xjst = xjst;
	}

	public Integer getLastAction() {
		return lastAction;
	}
	public void setLastAction(Integer lastAction) {
		this.lastAction = lastAction;
	}
	
	
	public Integer getPlayStatus() {
		return playStatus;
	}
	public void setPlayStatus(Integer playStatus) {
		this.playStatus = playStatus;
	}
	public Integer getCreateDisId() {
		return createDisId;
	}
	public void setCreateDisId(Integer createDisId) {
		this.createDisId = createDisId;
	}
	public Integer getApplyDisId() {
		return applyDisId;
	}
	public void setApplyDisId(Integer applyDisId) {
		this.applyDisId = applyDisId;
	}
	public Integer getOutNum() {
		return outNum;
	}
	public void setOutNum(Integer outNum) {
		this.outNum = outNum;
	}
	public Integer getWsw_sole_main_id() {
		return wsw_sole_main_id;
	}
	public void setWsw_sole_main_id(Integer wsw_sole_main_id) {
		this.wsw_sole_main_id = wsw_sole_main_id;
	}
	public Integer getWsw_sole_action_id() {
		return wsw_sole_action_id;
	}
	public void setWsw_sole_action_id(Integer wsw_sole_action_id) {
		this.wsw_sole_action_id = wsw_sole_action_id;
	}
	public String getOpenName() {
		return openName;
	}
	public void setOpenName(String openName) {
		this.openName = openName;
	}
	
	public List<Long> getPlayerIds() {
		return playerIds;
	}
	public void setPlayerIds(List<Long> playerIds) {
		this.playerIds = playerIds;
	}
	public Integer getXiaoJuNum() {
		return xiaoJuNum;
	}
	public void setXiaoJuNum(Integer xiaoJuNum) {
		this.xiaoJuNum = xiaoJuNum;
	}
//	//初始化房间手牌
//    public void initCurrentCardList() {
//		List<Card> cards = new ArrayList<Card>();
//		if(this.roomType == Cnst.ROOM_TYPE_1)
//		{	
//			for (int i = 0; i < Cnst.CARD_ARRAY_1.length; i++) {
//				cards.add(new Card(Cnst.CARD_ARRAY_1[i]));
//			}
//		}
//		else
//		{
//			for (int i = 0; i < Cnst.CARD_ARRAY_2.length; i++) {
//				cards.add(new Card(Cnst.CARD_ARRAY_2[i]));
//			}
//		}
//		this.currentCardList = cards;
//	}
    //发牌
    public void dealCard(int num,Player player){
    	for(int i=1;i<=num;i++){
    		Card card = currentCardList.get(getRandomVal());
    		player.dealCard(card);
    		currentCardList.remove(card);
        }
    }
    //获取发牌随机数
    public int getRandomVal(){
		return (int) (Math.random() * (currentCardList.size()));
	}
    
	public Integer getLastActionExtra() {
		return lastActionExtra;
	}
	public void setLastActionExtra(Integer lastActionExtra) {
		this.lastActionExtra = lastActionExtra;
	}
	public List<Card> getRealLastChuPai() {
		return realLastChuPai;
	}
	public void setRealLastChuPai(List<Card> realLastChuPai) {
		this.realLastChuPai = realLastChuPai;
	}
	public Long getRealLastUserId() {
		return realLastUserId;
	}
	public void setRealLastUserId(Long realLastUserId) {
		this.realLastUserId = realLastUserId;
	}
	public Integer getRealLastAction() {
		return realLastAction;
	}
	public void setRealLastAction(Integer realLastAction) {
		this.realLastAction = realLastAction;
	}
	public Long getCurrentUserId() {
		return currentUserId;
	}
	public void setCurrentUserId(Long currentUserId) {
		this.currentUserId = currentUserId;
	}
	public List<Integer> getCurrentUserAction() {
		return currentUserAction;
	}
	public void setCurrentUserAction(List<Integer> currentUserAction) {
		this.currentUserAction = currentUserAction;
	}
	public List<Integer> getMuls() {
		return muls;
	}
	public void setMuls(List<Integer> muls) {
		this.muls = muls;
	}
	public List<Integer> getJiaoDiZhu() {
		return jiaoDiZhu;
	}
	public void setJiaoDiZhu(List<Integer> jiaoDiZhu) {
		this.jiaoDiZhu = jiaoDiZhu;
	}
	public Long getFirstJiaoDiZhu() {
		return firstJiaoDiZhu;
	}
	public void setFirstJiaoDiZhu(Long firstJiaoDiZhu) {
		this.firstJiaoDiZhu = firstJiaoDiZhu;
	}
	public Long getFirstTakePaiUser() {
		return firstTakePaiUser;
	}
	public void setFirstTakePaiUser(Long firstTakePaiUser) {
		this.firstTakePaiUser = firstTakePaiUser;
	}
	
	
	public Long getDizhu() {
		return dizhu;
	}
	public void setDizhu(Long dizhu) {
		this.dizhu = dizhu;
	}
	public List<Integer> getZhandans() {
		return zhandans;
	}
	public void setZhandans(List<Integer> zhandans) {
		this.zhandans = zhandans;
	}
	public Integer getNongminChu() {
		return nongminChu;
	}
	public void setNongminChu(Integer nongminChu) {
		this.nongminChu = nongminChu;
	}
	public Integer getWinner() {
		return winner;
	}
	public void setWinner(Integer winner) {
		this.winner = winner;
	}
	public List<Long> getXiaoJieSuanScore() {
		return xiaoJieSuanScore;
	}
	public void setXiaoJieSuanScore(List<Long> xiaoJieSuanScore) {
		this.xiaoJieSuanScore = xiaoJieSuanScore;
	}
	public List<Long> getScore() {
		return score;
	}
	public void setScore(List<Long> score) {
		this.score = score;
	}
	public List<ArrayList<Integer>> getZhandanInfo() {
		return zhandanInfo;
	}
	public void setZhandanInfo(List<ArrayList<Integer>> zhandanInfo) {
		this.zhandanInfo = zhandanInfo;
	}
	public List<ArrayList<Card>> getCards() {
		return cards;
	}
	public void setCards(List<ArrayList<Card>> cards) {
		this.cards = cards;
	}
	
	public Integer getGiveUpTime() {
		return giveUpTime;
	}
	
	public void setGiveUpTime(Integer giveUpTime) {
		this.giveUpTime = giveUpTime;
	}
	
	public static int getRealGiveUpTime(RoomResp room){
		if(room.getGiveUpTime() == null)
			return 0;
		return room.getGiveUpTime() % 10000;
	}
	public List<List<Long>> getXiaoJuFen() {
		return xiaoJuFen;
	}
	public void setXiaoJuFen(List<List<Long>> xiaoJuFen) {
		this.xiaoJuFen = xiaoJuFen;
	}
	
	public void addXiaoJuFen(List<Long> info){
		this.xiaoJuFen.add(info);
	}
	public List<Integer> getMingPaiInfo() {
		return mingPaiInfo;
	}
	public void setMingPaiInfo(List<Integer> mingPaiInfo) {
		this.mingPaiInfo = mingPaiInfo;
	}
	public List<ArrayList<Card>> getLastCards() {
		return lastCards;
	}
	public void setLastCards(List<ArrayList<Card>> lastCards) {
		this.lastCards = lastCards;
	}
	public Integer getDizhuChu() {
		return dizhuChu;
	}
	public void setDizhuChu(Integer dizhuChu) {
		this.dizhuChu = dizhuChu;
	}
	public Long getLastWinner() {
		return lastWinner;
	}
	public void setLastWinner(Long lastWinner) {
		this.lastWinner = lastWinner;
	}
	public Long getLastJiaoDiZhu() {
		return lastJiaoDiZhu;
	}
	public void setLastJiaoDiZhu(Long lastJiaoDiZhu) {
		this.lastJiaoDiZhu = lastJiaoDiZhu;
	}
	public String getCid() {
		return cid;
	}
	public void setCid(String cid) {
		this.cid = cid;
	}
	
}
