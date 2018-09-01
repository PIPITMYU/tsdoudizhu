/*
 * Powered By [up72-framework]
 * Web Site: http://www.up72.com
 * Since 2006 - 2017
 */

package com.up72.game.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;




/**
 * 
 * 
 * @author up72
 * @version 1.0
 * @since 1.0
 */
public class Room implements java.io.Serializable{

   
	private Long id;
    private Integer roomId;
    private Long createId;
    private String createTime;
    private Integer isPlaying;
    
    private Integer tiShi;//提示
    private Integer clubId;//俱乐部id
    
    private String ip;//当前房间所在服务器的ip
    
    private Integer can4take2;//是否允许4带2
    private Integer laiZi;//是否是赖子的局
    private Integer DingFen;//int.maxvalue标识无上限  其他分数代表正常上限

    private Integer mulType;//加倍规则
    private Integer daPaiJiaoMan;//双王或者四个2叫满
    //买的总局数
    private Integer circleNum;
    //房间类型 
    private Integer roomType;
    //扩展类型
    private Integer extraType;
	
    
    /**
     * 明牌类型
     */
    private Integer mingPaiType;
    
    /**
     * 地主产生类型
     */
    private Integer dizhuType;
    
	public Long getCreateId() {
		return createId;
	}

	public void setCreateId(Long createId) {
		this.createId = createId;
	}

	public Integer getIsPlaying() {
        return isPlaying;
    }

    public void setIsPlaying(Integer isPlaying) {
        this.isPlaying = isPlaying;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public Integer getClubId() {
		return clubId;
	}

	public void setClubId(Integer clubId) {
		this.clubId = clubId;
	}

	public int hashCode() {
        return new HashCodeBuilder()
            .append(getId())
            .toHashCode();
    }

    public boolean equals(Object obj) {
        if(obj instanceof Room == false) return false;
        if(this == obj) return true;
        Room other = (Room)obj;
        return new EqualsBuilder()
            .append(getId(),other.getId())
            .isEquals();
    }

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}


	public Integer getTiShi() {
		return tiShi;
	}

	public void setTiShi(Integer tiShi) {
		this.tiShi = tiShi;
	}

	public Integer getCan4take2() {
		return can4take2;
	}

	public void setCan4take2(Integer can4take2) {
		this.can4take2 = can4take2;
	}

	public Integer getLaiZi() {
		return laiZi;
	}

	public void setLaiZi(Integer laiZi) {
		this.laiZi = laiZi;
	}

	public Integer getDingFen() {
		return DingFen;
	}

	public void setDingFen(Integer dingFen) {
		DingFen = dingFen;
	}


	public Integer getMulType() {
		return mulType;
	}

	public void setMulType(Integer mulType) {
		this.mulType = mulType;
	}

	public Integer getDaPaiJiaoMan() {
		return daPaiJiaoMan;
	}

	public void setDaPaiJiaoMan(Integer daPaiJiaoMan) {
		this.daPaiJiaoMan = daPaiJiaoMan;
	}

	public Integer getCircleNum() {
		return circleNum;
	}

	public void setCircleNum(Integer circleNum) {
		this.circleNum = circleNum;
	}

	public Integer getRoomType() {
		return roomType;
	}

	public void setRoomType(Integer roomType) {
		this.roomType = roomType;
	}

	public Integer getExtraType() {
		return extraType;
	}

	public void setExtraType(Integer extraType) {
		this.extraType = extraType;
	}

	public Integer getMingPaiType() {
		return mingPaiType;
	}

	public void setMingPaiType(Integer mingPaiType) {
		this.mingPaiType = mingPaiType;
	}

	public Integer getDizhuType() {
		return dizhuType;
	}

	public void setDizhuType(Integer dizhuType) {
		this.dizhuType = dizhuType;
	}

	
}

