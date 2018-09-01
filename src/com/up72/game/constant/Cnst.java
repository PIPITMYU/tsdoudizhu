package com.up72.game.constant;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.up72.server.mina.utils.ProjectInfoPropertyUtil;

/**
 * 常量
 */
public class Cnst {
	private static Log log = LogFactory.getLog(Cnst.class);
	// 获取项目版本信息
    public static final String version = ProjectInfoPropertyUtil.getProperty("project_version", "1.5");
    public static Boolean isTest = true;//是否是测试环境
    
    public static final long updateDiffTime = 3l * 24l * 3600l * 1000l;
    
    public static final Integer POSITION_EXPIRE_TIME = 8 * 3600;

    public static final String p_name = ProjectInfoPropertyUtil.getProperty("p_name", "wsw_X1");
    public static final String o_name = ProjectInfoPropertyUtil.getProperty("o_name", "u_consume");
    public static final String gm_url = ProjectInfoPropertyUtil.getProperty("gm_url", "");
    
    //回放配置
    public static final String BACK_FILE_PATH = ProjectInfoPropertyUtil.getProperty("backFilePath", "1.5");
    public static final String FILE_ROOT_PATH = ProjectInfoPropertyUtil.getProperty("fileRootPath", "1.5");
    public static final String USER_SERVICE_URL = ProjectInfoPropertyUtil.getProperty("getUser_url", "http://127.0.0.1:80/userService/returnUser");
    public static String SERVER_IP = getLocalAddress();
    public static String HTTP_URL = "http://".concat(Cnst.SERVER_IP).concat(":").concat(ProjectInfoPropertyUtil.getProperty("httpUrlPort", "8086")).concat("/");
    public static String getLocalAddress(){
		String ip = "";
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			log.error("ERROR", e);
		}
		return ip;
	}
    
    //redis配置
    public static final String REDIS_HOST = ProjectInfoPropertyUtil.getProperty("redis.host", "");
    public static final String REDIS_PORT = ProjectInfoPropertyUtil.getProperty("redis.port", "");
    public static final String REDIS_PASSWORD = ProjectInfoPropertyUtil.getProperty("redis.password", "");

    //mina的端口
    public static final String MINA_PORT = ProjectInfoPropertyUtil.getProperty("mina.port", "");
    //mina
    public static final int Session_Read_BufferSize = 2048 * 10;
    public static final int Session_life = 60;
    public static final int WriteTimeOut = 500;
    
    public static final String rootPath = ProjectInfoPropertyUtil.getProperty("rootPath", "");

    public static final long HEART_TIME = 9000;//心跳时间，前端定义为8s，避免网络问题延时，后端计算是以9s计算
    public static final int MONEY_INIT = 10;//初始赠送给用户的房卡数
    //开房选项中的是否
    public static final int YES = 1;
    public static final int NO = 0;
    

    public static final long ROOM_OVER_TIME = 5*60*60*1000;//房间定时24小时解散
    public static final long ROOM_CREATE_DIS_TIME = 40*60*1000;//创建房间之后，40分钟解散
    public static final long ROOM_DIS_TIME = 5*60*1000;//玩家发起解散房间之后，5分钟自动解散
    public static final String CLEAN_3 = "0 0 3 * * ?";
    public static final String CLEAN_5 = "0 0 5 * * ?";
    public static final String CLEAN_EVERY_HOUR = "0 0 0/1 * * ?";
    public static final String COUNT_EVERY_TEN_MINUTE = "0 0/1 * * * ?";
    public static final long BACKFILE_STORE_TIME = 3*24*60*60*1000;//回放文件保存时间
    
    
    //测试时间
//    public static final long ROOM_OVER_TIME = 30*1000;  //这里有bug 房间超过40分钟,会自动解散.
//    public static final long ROOM_CREATE_DIS_TIME = 30*1000;
//    public static final long ROOM_OVER_TIME = 30*1000;//
//    public static final long ROOM_CREATE_DIS_TIME = 5*1000;
//    public static final long ROOM_DIS_TIME = 10*1000;
//	  public static final String CLEAN_3 = "0/5 * * * * ?";
//	  public static final String CLEAN_EVERY_HOUR = "0/5 * * * * ?";
//    public static final String COUNT_EVERY_TEN_MINUTE = "0/1 * * * * ?";
//    public static final long BACKFILE_STORE_TIME = 60*1000;//回放文件保存时间
    
    

    public static final int ROOM_LIFE_TIME_CREAT = (int) (ROOM_OVER_TIME/1000);//创建时，2400s，redis用
    public static final int ROOM_LIFE_TIME_DIS = (int) (ROOM_DIS_TIME/1000);//解散房间时，300s，redis用
    public static final int ROOM_LIFE_TIME_COMMON = (int) (ROOM_CREATE_DIS_TIME/1000);//正常开局存活时间，redis用
    public static final int OVERINFO_LIFE_TIME_COMMON = (int) (10*60);//大结算 overInfo 存活时间
    public static final int PLAYOVER_LIFEE_TIME = 3*24*60*60;//战绩保存时间
    
    public static final Integer DAI_KAI_RECORD_EXPIRE = 60 * 60 * 48;//代开房间记录存活时间
    
    public static final int DIS_ROOM_RESULT = 1;

    public static final int DIS_ROOM_TYPE_1 = 1;//创建房间40分钟解散类型
    public static final int DIS_ROOM_TYPE_2 = 2;//玩家点击解散房间类型

    public static final int PAGE_SIZE = 10;
    
    public static final int DI_XIAN_PAI = 14;//房间默认的底线牌

    //风向表示
    public static final int WIND_EAST = 1;
    public static final int WIND_SOUTH = 2;
    public static final int WIND_WEST = 3;
    public static final int WIND_NORTH = 4;

    public static final String USER_SESSION_USER_ID = "user_id";
    public static final String USER_SESSION_CID = "cid";
    public static final String USER_SESSION_IP = "ip";
    
    public static final int MINGPAY_TYPE_1 = 1;//都不能明牌
    public static final int MINGPAY_TYPE_2 = 2;//只能地主明牌
    public static final int MINGPAY_TYPE_3 = 3;//自由明牌（目前没有这个）
    
    
    public static final int DIZHU_TYPE_1 = 1;//自由产生地主
    public static final int DIZHU_TYPE_2 = 2;//轮有产生地主
    public static final int DIZHU_TYPE_3 = 3;//优先上一局赢得做地主
    
    //房间信息中的state
    // 1等待玩家入坐；2游戏中；3小结算  在1状态 房主可以随意解散房间 否则不允许解散房间
    public static final int ROOM_STATE_CREATED = 1;
    public static final int ROOM_STATE_GAMIING = 2;
    public static final int ROOM_STATE_XJS = 3;
    public static final int ROOM_STATE_YJS = 4;
    //房间中的playState state = 2时有效
    public static final int ROOM_PLAYSTATE_START = 1;//开始
    public static final int ROOM_PLAYSTATE_JIAODIZHU = 2;//叫地主
    public static final int ROOM_PLAYSTATE_MINGPAI = 3;//明牌
    public static final int ROOM_PLAYSTATE_JIABEI = 4;//加倍环节
    public static final int ROOM_PLAYSTATE_CHUPAI = 5;//出牌环节
    public static final int ROOM_PLAYSTATE_END = 6;//小局结束

    //房间类型
    public static final int ROOM_TYPE_1 = 1;//经典
    public static final int ROOM_TYPE_2 = 2;//闪斗
    
    public static final int ROOM_EXTRA_TYPE_1 = 1;//默认模式
    public static final int ROOM_EXTRA_TYPE_2 = 2;//代开模式
    
    public static final int ROOM_MUL_TYPE_1 = 1;//不加倍
    public static final int ROOM_MUL_TYPE_2 = 2;//农民加倍
    public static final int ROOM_MUL_TYPE_3 = 3;//都可以加倍
    
    
    public static final int ROOM_NOTICE_IN = 1; //在代开的房间中
    public static final int ROOM_NOTICE_OUT = 2; //不在代开的房间中
    public static final int ROOM_NOTICE_DAIKAI_IN = 3; //代开中的房间  在线
    public static final int ROOM_NOTICE_DAIKAI_OUT = 4; //代开中的房间玩家状态 离线
   
    public static final int ROOM_JIAO_TYPE_1 = 1;//随意叫
    public static final int ROOM_JIAO_TYPE_2 = 2;//双王或者四个2较满
    
    //小局结算时的
    public static final int OVER_TYPE_1 = 1;//春天地主赢
    public static final int OVER_TYPE_2 = 2;//非春天地主赢
    public static final int OVER_TYPE_3 = 3;//非春天农民赢
    
    //封顶分数
    public static final int ROOM_MAX_SCORE_1 = Integer.MAX_VALUE;
    public static final int ROOM_MAX_SCORE_2 = 24;
    public static final int ROOM_MAX_SCORE_3 = 48;
    public static final int ROOM_MAX_SCORE_4 = 96;
    public static final int ROOM_MAX_SCORE_5 = 192;
    
    //牌型
    public static final int CARD_TYPE_DEFAULT = 100; //没有牌的牌型 任何人都可以管住
    public static final int CARD_TYPE_HUOJIAN = 1; //俩王
    public static final int CARD_TYPE_ZHANDAN = 2;//炸弹
    public static final int CARD_TYPE_DAN = 3;//单 1
    public static final int CARD_TYPE_DUI = 4;//对 11
    public static final int CARD_TYPE_SANBUDAI = 5;//3不带 111
    public static final int CARD_TYPE_SANDAIONE = 6;//3带1 可以带一对  1112
    public static final int CARD_TYPE_SANDAIDUI = 7;//3带1对 可以带一对  1112
    public static final int CARD_TYPE_DANSHUN = 8;//顺子 单瞬 56789 不能有2王
    public static final int CARD_TYPE_SHUANGSHUN = 9;//双顺 778899 不能有2
    public static final int CARD_TYPE_SANSHUN = 10;//三顺 777888999 不能有2
    public static final int CARD_TYPE_FEIJIDAN = 11;//飞机带的都单  只能是三顺777888999 +567 或者556677 不能混成5667
    public static final int CARD_TYPE_FEIJIDUI = 12;//飞机到的都是对 只能是三顺777888999 +567 或者556677 不能混成5667
    public static final int CARD_TYPE_FOURTAKE2ONE = 13;//四代2单  不能是飞机形式 只能单带
    public static final int CARD_TYPE_FOURTAKE2DUI = 14;//四代2对  不能是飞机形式 只能单带
    //开房的局数对应消耗的房卡数  经典
    public static final Map<Integer,Integer> moneyMap_1 = new HashMap<>();
    static {
    	moneyMap_1.put(5,3);
        moneyMap_1.put(10,3);
        moneyMap_1.put(20,6);
    }
    
    //闪斗局消耗的卡数
    public static final Map<Integer,Integer> moneyMap_2 = new HashMap<>();
    static {
    	moneyMap_2.put(5,3);
    	moneyMap_2.put(10,3);
    	moneyMap_2.put(20,6);
    }
    
    //玩家在线状态 state 
    public static final int PLAYER_LINE_STATE_INLINE = 1;//"inline"
    public static final int PLAYER_LINE_STATE_OUT = 2;//"out"

    //玩家状态
    public static final int PLAYER_STATE_DATING = 1;//"dating"
    public static final int PLAYER_STATE_IN = 2;//"in"
    public static final int PLAYER_STATE_PREPARED = 3;//"prepared"
    public static final int PLAYER_STATE_GAME = 4;//"game"
    public static final int PLAYER_STATE_over = 5;//"over"
    
    //输赢结果
    public static final int GAME_RESULT_QUANJIAN = 1;
    public static final int GAME_RESULT_BANJIAN = 2;
    public static final int GAME_RESULT_HEJU = 3;
    public static final int GAME_RESULT_BBANJIAN = 4;
    public static final int GAME_RESULT_BQUANJIAN = 5;

    //请求状态
    public static final int REQ_STATE_FUYI = -1;//敬请期待
    public static final int REQ_STATE_0 = 0;//非法请求
    public static final int REQ_STATE_1 = 1;//正常
    public static final int REQ_STATE_2 = 2;//余额不足
    public static final int REQ_STATE_3 = 3;//已经在其他房间中
    public static final int REQ_STATE_4 = 4;//房间不存在
    public static final int REQ_STATE_5 = 5;//房间人员已满
    public static final int REQ_STATE_6 = 6;//游戏中，不能退出房间
    public static final int REQ_STATE_7 = 7;//有玩家拒绝解散房间
    public static final int REQ_STATE_8 = 8;//玩家不存在（代开模式中，房主踢人用的）
    public static final int REQ_STATE_9 = 9;//接口id不符合，需请求大接口
    public static final int REQ_STATE_10 = 10;//代开房间创建成功
    public static final int REQ_STATE_11 = 11;//已经代开过10个了，不能再代开了
    public static final int REQ_STATE_12 = 12;//房间存在超过24小时解散的提示
    public static final int REQ_STATE_13 = 13;//房间40分钟未开局解散提示
    public static final int REQ_STATE_14 = 14;//ip不一致
    public static final int REQ_STATE_15 = 15;//有人没有准备
    public static final int REQ_STATE_16 = 16;//你不在此俱乐部内,无权加入

    //动作列表
    public static final int ACTION_MINGPAI = 10;//出牌
    public static final int ACTION_BUMINGPAI = 9;//不明牌
    public static final int ACTION_CHUPAI = 8;//出牌
    public static final int ACTION_BUJIABEI = 7;//不加倍
    public static final int ACTION_JIABEI = 6;//加倍
    public static final int ACTION_DIZHU0 = 5;//不叫
    public static final int ACTION_DIZHU3 = 4;//地主叫3分
    public static final int ACTION_DIZHU2 = 3;//地主叫2分
    public static final int ACTION_DIZHU1 = 2;//地主叫1分
    public static final int ACTION_GUO = 1;//过

    //牌局底分
    public static final int SCORE_BASE = 1;

    //退出类型
    public static final int EXIST_TYPE_EXIST = 1;//"exist"
    public static final int EXIST_TYPE_DISSOLVE = 2;//"dissolve";

    // 项目根路径
    public static String ROOTPATH = "";
    
    public static final String splitStr = "_";
    
    public static final String REDIS_PREFIX = ProjectInfoPropertyUtil.getProperty("redis.prefix","");
    /**
     * 房间信息
     */
    public static final String REDIS_PREFIX_ROOMMAP = REDIS_PREFIX + "_ROOM_MAP_";//房间信息  
    
    public static String get_REDIS_PREFIX_ROOMMAP(String cId){
    	if(cId == null || "".equals(cId))
    		return null;
    	return REDIS_PREFIX_ROOMMAP.concat(cId).concat(splitStr);
    }
 
    
    /**
     * openId - userId 对照表
     */
    public static final String REDIS_PREFIX_OPENIDUSERMAP = REDIS_PREFIX + "_OPENID_USERID_MAP_";//openId-user数据
    
    public static String get_REDIS_PREFIX_OPENIDUSERMAP(String cId){
    	if(cId == null || "".equals(cId))
    		return null;
    	
    	return REDIS_PREFIX_OPENIDUSERMAP.concat(cId).concat(splitStr);
    }
    
    /**
     * userId  - Player对象对照表
     */
    public static final String REDIS_PREFIX_USER_ID_USER_MAP = REDIS_PREFIX + "_USER_ID_USER_MAP_";//通过userId获取用户
    
    public static String get_REDIS_PREFIX_USER_ID_USER_MAP(String cId){
    	if(cId == null || "".equals(cId))
    		return null;
    	return REDIS_PREFIX_USER_ID_USER_MAP.concat(cId).concat(splitStr);
    }
    
    /**
     * userId - (x,y) 玩家坐标最招标
     */
    public static final String REDIS_PREFIX_USER_ID_POSITION = REDIS_PREFIX + "_USER_ID_POSITION_";//通过玩家坐标
    
    /**
     * 通知  没有对照
     */
    public static final String NOTICE_KEY = REDIS_PREFIX + "_NOTICE_KEY";
    
    public static String get_NOTICE_KEY(String cId){
    	if(cId == null || "".equals(cId))
    		return null;
    	return NOTICE_KEY.concat(splitStr).concat(cId);
    }
    
    /**
     * 所有需要重启以后删除的对照表
     */
    public static final String PROJECT_PREFIX = REDIS_PREFIX + "_*";
    
    /**
     * IP - NUM 对应IP在线人数
     */
    public static final String REDIS_ONLINE_NUM_COUNT = REDIS_PREFIX + "_ONLINE_NUM_";
    
    public static String get_REDIS_ONLINE_NUM_COUNT(String cId){
    	if(cId == null || "".equals(cId))
    		return null;
    	return REDIS_ONLINE_NUM_COUNT.concat(cId).concat(splitStr);
    }
    
    /**
     * hash结构 全服一个  filed - value 是 userId - timer 玩家ID和时间戳对照表
     */
    public static final String REDIS_HEART_PREFIX = REDIS_PREFIX + "_HEART_USERS_MAP_";
    
    public static String get_REDIS_HEART_PREFIX(String cId){
    	if(cId == null || "".equals(cId))
    		return null;
    	return REDIS_HEART_PREFIX.concat(cId);
    }
    
    /**
     * roomId - 回放记录    对照表
     */
    public static final String HUIFANG = REDIS_PREFIX + "_HUIFANG_STRING_";
    
    public static String get_HUIFANG(String cId){
    	if(cId == null || "".equals(cId))
    		return null;
    	return HUIFANG.concat(cId).concat(splitStr);
    }
    
    
    /**
     * 玩家代开的记录  hashMap 结构 key为 房间ID value为1 没有意义
     */
    public static final String ROOM_DAIKAI_KEY = REDIS_PREFIX + "_DAIKAI_KEY_";
	
	public static String get_ROOM_DAIKAI_KEY(String cId){
		if(cId == null || "".equals(cId))
    		return null;
		return ROOM_DAIKAI_KEY.concat(cId).concat(splitStr);
	}
    //TSDDZ
    public static final String REDIS_RECORD_PREFIX = ProjectInfoPropertyUtil.getProperty("redis.record_prefix","");
    
    //这个字段不清理，存放玩家战绩，定时任务定期清理内容
    /**
     * 战绩记录 key为room-createTime  value为整个房间记录
     */
    public static final String REDIS_PLAY_RECORD_PREFIX = REDIS_RECORD_PREFIX + "_PLAY_RECORD_";//房间战绩
    
    
    public static String get_REDIS_PLAY_RECORD_PREFIX(String cId){
    	if(cId == null || "".equals(cId))
    		return null;
    	return REDIS_PLAY_RECORD_PREFIX.concat(cId).concat(splitStr);
    }
    
    /**
     * list结构  key为 userId value是列表 表示自己玩过的房间-createTime
     */
    public static final String REDIS_PLAY_RECORD_PREFIX_ROE_USER = REDIS_RECORD_PREFIX + "_PLAY_RECORD_FOR_USER_";//玩家字段
    
    public static String get_REDIS_PLAY_RECORD_PREFIX_ROE_USER(String cId){
    	if(cId == null || "".equals(cId))
    		return null;
    	return REDIS_PLAY_RECORD_PREFIX_ROE_USER.concat(cId).concat(splitStr);
    }
    /**
     * list 结构 key为userId value roomId-createTime 
     */
    public static final String REDIS_PLAY_RECORD_PREFIX_ROE_DAIKAI = REDIS_RECORD_PREFIX + "_PLAY_RECORD_FOR_DAIKAI_";//代开房间
    
    public static String get_REDIS_PLAY_RECORD_PREFIX_ROE_DAIKAI(String cId){
    	if(cId == null || "".equals(cId))
    		return null;
    	return REDIS_PLAY_RECORD_PREFIX_ROE_DAIKAI.concat(cId).concat(splitStr);
    }
    
    /**
     * 临时存储大结算数据 key为roomId-createTime
     */
    public static final String REDIS_PLAY_RECORD_PREFIX_OVERINFO = REDIS_RECORD_PREFIX + "_PLAY_RECORD_OVERINFO_";//大结算
    
    public static String get_REDIS_PLAY_RECORD_PREFIX_OVERINFO(String cId){
    	if(cId == null || "".equals(cId))
    		return null;
    	return REDIS_PLAY_RECORD_PREFIX_OVERINFO.concat(cId).concat(splitStr);
    }
    
    /**
     * 存放俱乐部的信息  key:cid+clubid ---  value:club实体
     */
    public static String get_REDIS_PREFIX_CLUBMAP(String cid) {
    	if(cid == null || "".equals(cid))
    		return null;
    	return REDIS_CLUB_CLUBMAP.concat(cid).concat(splitStr);
	}
    /*
     * 俱乐部未开房间信息 key:cid+clubid ---  value: roomid
     */
    public static String get_REDIS_CLUB_ROOM_LIST(String cid) {
    	if(cid == null || "".equals(cid))
    		return null;
    	return REDIS_CLUB_ROOM_LIST.concat(cid).concat(splitStr);
    }
    /*
     * 玩家信息 key:cid userId,clubId,时间 ---  value:roomid实体
     */
    public static String get_REDIS_CLUB_PLAY_RECORD_PREFIX_ROE_USER(String cid) {
    	if(cid == null || "".equals(cid))
    		return null;
    	return REDIS_CLUB_PLAY_RECORD_PREFIX_ROE_USER.concat(cid).concat(splitStr);
    }
    /*
     * 玩家房间信息 key:
     */
    public static String get_REDIS_CLUB_PLAY_RECORD_PREFIX(String cid) {
    	if(cid == null || "".equals(cid))
    		return null;
    	return REDIS_CLUB_PLAY_RECORD_PREFIX.concat(cid).concat(splitStr);
    }
    /*
     * 俱乐部活跃人数   
     */
    public static String get_REDIS_CLUB_ACTIVE_PERSON(String cid) {
    	if(cid == null || "".equals(cid))
    		return null;
    	return REDIS_CLUB_ACTIVE_PERSON.concat(cid).concat(splitStr);
    }
    
    /*
     * 俱乐部今天开局数
     */
    public static String get_REDIS_CLUB_TODAYKAI_NUM(String cid) {
    	if(cid == null || "".equals(cid))
    		return null;
    	return REDIS_CLUB_TODAYKAI_NUM.concat(cid).concat(splitStr);
    }
    
    
    /*
     * 俱乐部玩家今日局数
     */
    public static String getREDIS_CLUB_TODAYJUNUM_ROE_USER(String cid) {
    	if(cid == null || "".equals(cid))
    		return null;
    	return REDIS_CLUB_TODAYJUNUM_ROE_USER.concat(cid).concat(splitStr);
    }
    
    //俱乐部
    public static final String REDIS_CLUB_CLUBMAP = REDIS_RECORD_PREFIX+"_CLUB_MAP_";//俱乐部信息
    public static final String REDIS_CLUB_ROOM_LIST = REDIS_RECORD_PREFIX+"_CLUB_MAP_LIST_";//存放俱乐部未开局房间信息
    //key：cid +clubId+当天0点的时间   value: Set<userId> 
    public static final String REDIS_CLUB_PLAY_RECORD_PREFIX = REDIS_RECORD_PREFIX+"_CLUB_PLAY_RECORD_";//房间战绩
    public static final String REDIS_CLUB_PLAY_RECORD_PREFIX_ROE_USER = REDIS_RECORD_PREFIX+"_CLUB_PLAY_RECORD_FOR_USER_";//玩家字段
    
    public static final String REDIS_CLUB_TODAYSCORE_ROE_USER = REDIS_RECORD_PREFIX+"_CLUB_TODAYSCORE_FOR_USER_";//存放俱乐部玩家当天的分数
    public static final String REDIS_CLUB_TODAYJUNUM_ROE_USER = REDIS_RECORD_PREFIX+"_CLUB_TODAYJUNUM_FOR_USER_";//统计玩家今日局数
    public static final String REDIS_CLUB_ACTIVE_PERSON = REDIS_RECORD_PREFIX+"_CLUB_ACTIVE_PERSON_";//存放俱乐部当天活跃人数
    public static final String REDIS_CLUB_TODAYKAI_NUM = REDIS_RECORD_PREFIX+"_CLUB_TODAYKAI_NUM_";//今天开局数

    public static final int REDIS_CLUB_DIE_TIME = 3*24*60*60;//玩家战绩和俱乐部每天活跃保存时间
    public static final int REDIS_CLUB_PLAYERJUNUM_TIME =3* 24*60*60;//玩家今日局数和昨日局数保存时间
    
    
    public static Map<String,String> ROUTE_MAP = new ConcurrentHashMap<String, String>();
    static{
    	ROUTE_MAP.put("a","interfaceId");
    	ROUTE_MAP.put("b","state");
    	ROUTE_MAP.put("c","message");
    	ROUTE_MAP.put("d","info");
    	ROUTE_MAP.put("e","others");
    	ROUTE_MAP.put("f","page");
    	ROUTE_MAP.put("g","infos");
    	ROUTE_MAP.put("h","roomId");
    	ROUTE_MAP.put("i","createTime");
    	ROUTE_MAP.put("j","userInfos");
    	ROUTE_MAP.put("k","pages");
    	ROUTE_MAP.put("l","connectionInfo");
    	ROUTE_MAP.put("m","circleNum");
    	ROUTE_MAP.put("n","roomType");
    	ROUTE_MAP.put("o","type");
    	ROUTE_MAP.put("p","xuanNum");
    	ROUTE_MAP.put("q","chuaiCircle");
    	ROUTE_MAP.put("r","chengChu");
    	ROUTE_MAP.put("s","shuangShun");
    	ROUTE_MAP.put("t","A23");
    	ROUTE_MAP.put("u","gongDan");
    	ROUTE_MAP.put("v","reqState");
    	ROUTE_MAP.put("w","playerNum");
    	ROUTE_MAP.put("x","money");
    	ROUTE_MAP.put("y","roomSn");
    	ROUTE_MAP.put("z","playerInfo");
    	ROUTE_MAP.put("A","position");
    	ROUTE_MAP.put("B","openName");
    	ROUTE_MAP.put("C","openImg");
    	ROUTE_MAP.put("D","roomInfo");
    	ROUTE_MAP.put("E","score");
    	ROUTE_MAP.put("F","openId");
    	ROUTE_MAP.put("G","cId");
    	ROUTE_MAP.put("H","wsw_sole_main_id");
    	ROUTE_MAP.put("I","wsw_sole_action_id");
    	ROUTE_MAP.put("J","userId");
    	ROUTE_MAP.put("K","lastNum");
    	ROUTE_MAP.put("L","totalNum");
    	ROUTE_MAP.put("M","lastPai");
    	ROUTE_MAP.put("N","lastUserId");
    	ROUTE_MAP.put("O","xjst");
    	ROUTE_MAP.put("P","dissolveRoom");
    	ROUTE_MAP.put("Q","dissolveTime");
    	ROUTE_MAP.put("R","othersAgree");
    	ROUTE_MAP.put("S","agree");
    	ROUTE_MAP.put("T","currentUser");
    	ROUTE_MAP.put("U","playStatus");
    	ROUTE_MAP.put("V","version");
    	ROUTE_MAP.put("W","gender");
    	ROUTE_MAP.put("X","ip");
    	ROUTE_MAP.put("Y","joinIndex");
    	ROUTE_MAP.put("Z","userAgree");
    	ROUTE_MAP.put("aa","notice");
    	ROUTE_MAP.put("ab","roomInfos");
    	ROUTE_MAP.put("ac","ChuColor");
    	ROUTE_MAP.put("ad","ChuPlayers");
    	ROUTE_MAP.put("ae","diFen");
    	ROUTE_MAP.put("af","actionUser");
    	ROUTE_MAP.put("ag","action");
    	ROUTE_MAP.put("ah","actionPai");
    	ROUTE_MAP.put("ai","pais");
    	ROUTE_MAP.put("aj","isYao");
    	ROUTE_MAP.put("ak","idx");
    	ROUTE_MAP.put("al","continue");
    	ROUTE_MAP.put("am","nextActionUserId");
    	ROUTE_MAP.put("an","nextAction");
    	ROUTE_MAP.put("ao","paiNum");
    	ROUTE_MAP.put("ap","nextChuPlayerId");
    	ROUTE_MAP.put("aq","endNum");// 几跑 paoIndex
    	ROUTE_MAP.put("ar","totalFan");
    	ROUTE_MAP.put("as","Chu");
    	ROUTE_MAP.put("at","finalScore");
    	ROUTE_MAP.put("au","fanInfo");
    	ROUTE_MAP.put("av","quanJ");
    	ROUTE_MAP.put("aw","banJ");
    	ROUTE_MAP.put("ax","heJ");
    	ROUTE_MAP.put("ay","chuNum");
    	ROUTE_MAP.put("az","duChuNum");
    	ROUTE_MAP.put("aA","XiaoJuNum");// xjn
    	ROUTE_MAP.put("aB","lastChuPai");
    	ROUTE_MAP.put("aC","lastActionUser");//lastActionPlayer
    	ROUTE_MAP.put("aD","lastAction");
    	ROUTE_MAP.put("aE","lastActionPai");
    	ROUTE_MAP.put("aF","currActionUser");//currActionPlayer
    	ROUTE_MAP.put("aG","currAction");
    	ROUTE_MAP.put("aH","currActionPai");
    	ROUTE_MAP.put("aI","tiShi");
    	ROUTE_MAP.put("aJ","paiInfos");//paiInfos
    	ROUTE_MAP.put("aK","chuaiPais");
    	ROUTE_MAP.put("aL","juNum");
    	ROUTE_MAP.put("aM","backUrl");
    	ROUTE_MAP.put("aN","sessionId");
    	ROUTE_MAP.put("aO","bQuanJ");
    	ROUTE_MAP.put("aP","bBanJ");
    	ROUTE_MAP.put("aQ","winType");
    	ROUTE_MAP.put("aR","anotherUsers");
    	ROUTE_MAP.put("aS","openName");
    	///////新加
    	ROUTE_MAP.put("Bc","can4Take2");
    	ROUTE_MAP.put("Bd","isLaiZi");
    	ROUTE_MAP.put("Be","dingFen");
    	ROUTE_MAP.put("Bf","mulType");
    	ROUTE_MAP.put("Bg","muls");
    	ROUTE_MAP.put("Bh","jiaodizhu");
    	ROUTE_MAP.put("Bi","dizhu");
    	ROUTE_MAP.put("Bj","lastActionExtra");
    	ROUTE_MAP.put("Bk","realLastAction");
    	ROUTE_MAP.put("Bl","realLastUser");
    	ROUTE_MAP.put("Bm","realLastChuPai");
    	ROUTE_MAP.put("Bn","currentUserId");
    	ROUTE_MAP.put("Bo","currentUserAction");
    	ROUTE_MAP.put("Bp","result");
    	ROUTE_MAP.put("Bq","role");
    	ROUTE_MAP.put("Br","currentMul");
    	ROUTE_MAP.put("Bs","spring");
    	ROUTE_MAP.put("Bt","zhadan");
    	ROUTE_MAP.put("Bu","winNum");
    	ROUTE_MAP.put("Bv","failNum");
    	ROUTE_MAP.put("Bw","daPaiJiaoMan");
    	ROUTE_MAP.put("Bx","actionPlayer");
    	ROUTE_MAP.put("By","actionExtra");
    	ROUTE_MAP.put("Bz","origin");
    	ROUTE_MAP.put("cA","laizi");
    	ROUTE_MAP.put("cB","mul");
    	ROUTE_MAP.put("cC","giveUpTime");
    	ROUTE_MAP.put("cD","diZhuPai");
    	ROUTE_MAP.put("cE","uid");
    	ROUTE_MAP.put("cF","chuPaiInfo");
    	ROUTE_MAP.put("cG","winPais");
    	ROUTE_MAP.put("cH","extraType");	
    	ROUTE_MAP.put("cI","firstUserName");
    	ROUTE_MAP.put("cJ","firstUserImg");
    	ROUTE_MAP.put("cK","firstUserMoneyRecord");
    	ROUTE_MAP.put("cL","secondUserName");
    	ROUTE_MAP.put("cM","secondUserImg");
    	ROUTE_MAP.put("cN","secondUserMoneyRecord");
    	ROUTE_MAP.put("cO","thirdUserName");
    	ROUTE_MAP.put("cP","thirdUserImg");
    	ROUTE_MAP.put("cQ","thirdUserMoneyRecord");
    	ROUTE_MAP.put("cR","xiaoJuInfo");
    	ROUTE_MAP.put("cS","mingPaiInfo");
    	ROUTE_MAP.put("cT","lastCards");
    	ROUTE_MAP.put("cU","mingPai");
    	ROUTE_MAP.put("cV","mingPais");
    	ROUTE_MAP.put("cW","leaveUserId");
    	ROUTE_MAP.put("cX","mingPaiType");
    	ROUTE_MAP.put("cY","diZhuType");
    	ROUTE_MAP.put("cZ", "roomIp");
    	
    	
    	//有关俱乐部的
    	ROUTE_MAP.put("da", "clubId");
    	ROUTE_MAP.put("db", "clubName");
    	ROUTE_MAP.put("dc", "clubUserName");
    	ROUTE_MAP.put("dd", "allNums");
    	ROUTE_MAP.put("de", "createTime");
    	ROUTE_MAP.put("df", "maxNums");
    	ROUTE_MAP.put("dg", "freeStart");
    	ROUTE_MAP.put("dh", "freeEnd");
    	ROUTE_MAP.put("di", "clubMoney");
    	ROUTE_MAP.put("dj", "cardQuota");
    	ROUTE_MAP.put("dk", "used");
    	ROUTE_MAP.put("dl", "juNum");
    	ROUTE_MAP.put("dm", "actNum");
    	ROUTE_MAP.put("dn", "exState");
    	ROUTE_MAP.put("do", "users");
    	ROUTE_MAP.put("dp", "realMuls");
    	
    	//关于战绩
    	ROUTE_MAP.put("dq", "firstUserId");
    	ROUTE_MAP.put("dr", "secondUserId");
    	ROUTE_MAP.put("ds", "thirdUserId");
    	ROUTE_MAP.put("dt", "date");
    	
    	ROUTE_MAP.put("dv","paiNumList");
    	
    	////////////////////////////////////////////
    	ROUTE_MAP.put("interfaceId","a");
    	ROUTE_MAP.put("state","b");
    	ROUTE_MAP.put("message","c");
    	ROUTE_MAP.put("info","d");
    	ROUTE_MAP.put("others","e");
    	ROUTE_MAP.put("page","f");
    	ROUTE_MAP.put("infos","g");
    	ROUTE_MAP.put("roomId","h");
    	ROUTE_MAP.put("createTime","i");
    	ROUTE_MAP.put("userInfos","j");
    	ROUTE_MAP.put("pages","k");
    	ROUTE_MAP.put("connectionInfo","l");
    	ROUTE_MAP.put("circleNum","m");
    	ROUTE_MAP.put("roomType","n");
    	ROUTE_MAP.put("type","o");
    	ROUTE_MAP.put("xuanNum","p");
    	ROUTE_MAP.put("chuaiCircle","q");
    	ROUTE_MAP.put("chengChu","r");
    	ROUTE_MAP.put("shuangShun","s");
    	ROUTE_MAP.put("A23","t");
    	ROUTE_MAP.put("gongDan","u");
    	ROUTE_MAP.put("reqState","v");
    	ROUTE_MAP.put("playerNum","w");
    	ROUTE_MAP.put("money","x");
    	ROUTE_MAP.put("roomSn","y");
    	ROUTE_MAP.put("playerInfo","z");
    	ROUTE_MAP.put("position","A");
    	ROUTE_MAP.put("userName","B");
    	ROUTE_MAP.put("userImg","C");
    	ROUTE_MAP.put("roomInfo","D");
    	ROUTE_MAP.put("score","E");
    	ROUTE_MAP.put("openId","F");
    	ROUTE_MAP.put("cId","G");
    	ROUTE_MAP.put("wsw_sole_main_id","H");
    	ROUTE_MAP.put("wsw_sole_action_id","I");
    	ROUTE_MAP.put("userId","J");
    	ROUTE_MAP.put("lastNum","K");
    	ROUTE_MAP.put("totalNum","L");
    	ROUTE_MAP.put("lastPai","M");
    	ROUTE_MAP.put("lastUserId","N");
    	ROUTE_MAP.put("xjst","O");
    	ROUTE_MAP.put("dissolveRoom","P");
    	ROUTE_MAP.put("dissolveTime","Q");
    	ROUTE_MAP.put("othersAgree","R");
    	ROUTE_MAP.put("agree","S");
    	ROUTE_MAP.put("currentUser","T");
    	ROUTE_MAP.put("playStatus","U");
    	ROUTE_MAP.put("version","V");
    	ROUTE_MAP.put("gender","W");
    	ROUTE_MAP.put("ip","X");
    	ROUTE_MAP.put("joinIndex","Y");
    	ROUTE_MAP.put("userAgree","Z");
    	ROUTE_MAP.put("notice","aa");
    	ROUTE_MAP.put("roomInfos","ab");
    	ROUTE_MAP.put("chuColors","ac");//
    	ROUTE_MAP.put("chuPlayers","ad");
    	ROUTE_MAP.put("diFen","ae");
    	ROUTE_MAP.put("actionUser","af");
    	ROUTE_MAP.put("action","ag");
    	ROUTE_MAP.put("actionPai","ah");
    	ROUTE_MAP.put("pais","ai");
    	ROUTE_MAP.put("isYao","aj");
    	ROUTE_MAP.put("idx","ak");
    	ROUTE_MAP.put("continue","al");
    	ROUTE_MAP.put("nextActionUserId","am");//nextActionUserId
    	ROUTE_MAP.put("nextAction","an");
    	ROUTE_MAP.put("paiNum","ao");
    	ROUTE_MAP.put("nextChuPlayerId","ap");
    	ROUTE_MAP.put("endNum","aq");//几跑
    	ROUTE_MAP.put("totalFan","ar");
    	ROUTE_MAP.put("Chu","as");
    	ROUTE_MAP.put("finalScore","at");
    	ROUTE_MAP.put("fanInfo","au");
    	ROUTE_MAP.put("quanJ","av");
    	ROUTE_MAP.put("banJ","aw");
    	ROUTE_MAP.put("heJ","ax");
    	ROUTE_MAP.put("chuNum","ay");
    	ROUTE_MAP.put("duChuNum","az");
    	ROUTE_MAP.put("XiaoJuNum","aA");
    	ROUTE_MAP.put("lastChuPai","aB");
    	ROUTE_MAP.put("lastActionUser","aC");//lastActionPlayer
    	ROUTE_MAP.put("lastAction","aD");
    	ROUTE_MAP.put("lastActionPai","aE");
    	ROUTE_MAP.put("currActionUser","aF");//currActionPlayer
    	ROUTE_MAP.put("currAction","aG");
    	ROUTE_MAP.put("currActionPai","aH");
    	ROUTE_MAP.put("tiShi","aI");
    	ROUTE_MAP.put("paiInfos","aJ");//paiInfos
    	ROUTE_MAP.put("chuaiPais","aK");
    	ROUTE_MAP.put("juNum","aL");
    	ROUTE_MAP.put("backUrl","aM");
    	ROUTE_MAP.put("sessionId","aN");
    	ROUTE_MAP.put("bQuanJ","aO");
    	ROUTE_MAP.put("bBanJ","aP");
    	ROUTE_MAP.put("winType","aQ");
    	ROUTE_MAP.put("anotherUsers","aR");
    	ROUTE_MAP.put("openName","aS");
    	ROUTE_MAP.put("eastUserName","aT");
    	ROUTE_MAP.put("eastUserImg","aU");
    	ROUTE_MAP.put("eastUserMoneyRecord","aV");
    	ROUTE_MAP.put("southUserName","aW");
    	ROUTE_MAP.put("southUserImg","aX");
    	ROUTE_MAP.put("southUserMoneyRecord","aY");
    	ROUTE_MAP.put("westUserName","aZ");
    	ROUTE_MAP.put("westUserImg","ba");
    	ROUTE_MAP.put("westUserMoneyRecord","bb");
    	ROUTE_MAP.put("northUserName","bc");
    	ROUTE_MAP.put("northUserImg","bd");
    	ROUTE_MAP.put("northUserMoneyRecord","be");

    	ROUTE_MAP.put("id","Ba");
    	ROUTE_MAP.put("playerIds","Bb");
    	
    	//-------------------------------------------------
    	////////////////////////新加
    	ROUTE_MAP.put("can4Take2","Bc");
    	ROUTE_MAP.put("isLaiZi","Bd");
    	ROUTE_MAP.put("dingFen","Be");
    	ROUTE_MAP.put("mulType","Bf");
    	ROUTE_MAP.put("muls","Bg");
    	ROUTE_MAP.put("jiaodizhu","Bh");
    	ROUTE_MAP.put("dizhu","Bi");
    	ROUTE_MAP.put("lastActionExtra","Bj");
    	ROUTE_MAP.put("realLastAction","Bk");
    	ROUTE_MAP.put("realLastUser","Bl");
    	ROUTE_MAP.put("realLastChuPai","Bm");
    	ROUTE_MAP.put("currentUserId","Bn");
    	ROUTE_MAP.put("currentUserAction","Bo");
    	ROUTE_MAP.put("result","Bp");
    	ROUTE_MAP.put("role","Bq");
    	ROUTE_MAP.put("currentMul","Br");
    	ROUTE_MAP.put("spring","Bs");
    	ROUTE_MAP.put("zhadan","Bt");
    	ROUTE_MAP.put("winNum","Bu");
    	ROUTE_MAP.put("failNum","Bv");
    	ROUTE_MAP.put("daPaiJiaoMan","Bw");
    	ROUTE_MAP.put("actionPlayer","Bx");
    	ROUTE_MAP.put("actionExtra","By");
    	ROUTE_MAP.put("origin","Bz");
    	ROUTE_MAP.put("laizi","cA");
    	ROUTE_MAP.put("mul","cB");
    	ROUTE_MAP.put("giveUpTime","cC");
    	ROUTE_MAP.put("diZhuPai","cD");
    	ROUTE_MAP.put("uid","cE");
    	ROUTE_MAP.put("chuPaiInfo","cF");
    	ROUTE_MAP.put("winPais","cG");
    	ROUTE_MAP.put("extraType","cH");
    	
    	
    	ROUTE_MAP.put("firstUserName","cI");
    	ROUTE_MAP.put("firstUserImg","cJ");
    	ROUTE_MAP.put("firstUserMoneyRecord","cK");
    	ROUTE_MAP.put("secondUserName","cL");
    	ROUTE_MAP.put("secondUserImg","cM");
    	ROUTE_MAP.put("secondUserMoneyRecord","cN");
    	ROUTE_MAP.put("thirdUserName","cO");
    	ROUTE_MAP.put("thirdUserImg","cP");
    	ROUTE_MAP.put("thirdUserMoneyRecord","cQ");
    	
    	ROUTE_MAP.put("xiaoJuInfo","cR");
    	ROUTE_MAP.put("mingPaiInfo","cS");
    	ROUTE_MAP.put("lastCards","cT");
    	
    	ROUTE_MAP.put("mingPai","cU");
    	ROUTE_MAP.put("mingPais","cV");
    	ROUTE_MAP.put("leaveUserId","cW");
    	ROUTE_MAP.put("mingPaiType","cX");
    	ROUTE_MAP.put("diZhuType","cY");
    	ROUTE_MAP.put("roomIp", "cZ");
    	
    	
    	
    	//俱乐部的
    	ROUTE_MAP.put("clubId", "da");
    	ROUTE_MAP.put("clubName", "db");
    	ROUTE_MAP.put("clubUserName", "dc");
    	ROUTE_MAP.put("allNums", "dd");
    	ROUTE_MAP.put("createTime", "de");
    	ROUTE_MAP.put("maxNums", "df");
    	ROUTE_MAP.put("freeStart", "dg");
    	ROUTE_MAP.put("freeEnd", "dh");
    	ROUTE_MAP.put("clubMoney", "di");
    	ROUTE_MAP.put("cardQuota", "dj");
    	ROUTE_MAP.put("used", "dk");
    	ROUTE_MAP.put("juNum", "dl");
    	ROUTE_MAP.put("actNum", "dm");
    	ROUTE_MAP.put("exState", "dn");
    	ROUTE_MAP.put("users", "do");
    	
    	//回放单加的字段.
    	ROUTE_MAP.put("realMuls","dp");
    	
    	//关于战绩
    	ROUTE_MAP.put("firstUserId", "dq");
    	ROUTE_MAP.put("secondUserId", "dr");
    	ROUTE_MAP.put("thirdUserId", "ds");
    	ROUTE_MAP.put("date", "dt");
    	
    	ROUTE_MAP.put("paiNumList","dv");
    	
    }

    
    
    /**
     * 经典卡组
     */
    public final static int[] CARD_ARRAY_1 = { 101, 201, 301, 401, 102, 202, 302, 402, 103, 203, 303,
    	403, 104, 204, 304, 404, 105, 205, 305, 405, 106, 206, 306, 406, 107, 207, 307, 407, 108, 208,
    	308, 408, 109, 209, 309, 409, 110, 210, 310, 410, 111, 211, 311, 411, 112, 212, 312, 412, 113, 
    	213, 313, 413, 114, 214};
//    public final static int[] CARD_ARRAY_1 = { 101, 201, 301, 401, 102, 202, 302, 402, 103, 203, 303,
//    	403, 104};
    
    /**
     * 闪斗卡组
     */
    public final static int[] CARD_ARRAY_2 = { 101, 201, 301, 401, 102, 202, 302, 402, 106, 206, 306, 406, 107, 207, 307, 407, 108, 208,
    	308, 408, 109, 209, 309, 409, 110, 210, 310, 410, 111, 211, 311, 411, 112, 212, 312, 412, 113, 
    	213, 313, 413, 114, 214, 115};
//    public final static int[] CARD_ARRAY_2 = { 101, 201, 301, 401, 102, 202, 302, 402, 106, 206, 306, 406, 107, 207, 307, 407, 108, 208,
//    	308, 408};
    
    /**
     * 闪斗癞子不能变成
     */
    public final static int[] CANNOT_BECOME = { 103, 203, 303,
    	403, 104, 204, 304, 404, 105, 205, 305, 405 ,114, 214 };
	
	
	
	
}
