package com.up72.server.mina.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2017/7/8.
 */
public class InfoCount implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 8797588772609530339L;
	private List<Integer[][]> l;//牌信息
    private Long userId;//用户id
    private Long toUserId;//比如碰的谁的牌，中的谁，即被碰牌玩家
    private Integer actionType;//1吃 2碰 3杠
    private Long t;

    public List<Integer[][]> getL() {
        return l;
    }

    public void setL(List<Integer[][]> l) {
        this.l = l;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getActionType() {
        return actionType;
    }

    public void setActionType(Integer actionType) {
        this.actionType = actionType;
    }

    public Long getToUserId() {
        return toUserId;
    }

    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
    }

    public Long getT() {
        return t;
    }

    public void setT(Long t) {
        this.t = t;
    }
}
