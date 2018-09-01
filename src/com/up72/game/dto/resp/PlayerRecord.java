package com.up72.game.dto.resp;

/**
 * Created by Administrator on 2017/7/8.
 */
public class PlayerRecord {
    private Integer id;
    private Integer roomId;
    private String startTime;
    private String endTime;
    private String firstUserId;
    private String firstUserName;
    private Integer firstUserMoneyRecord;
    private Integer firstUserMoneyRemain;


    private String secondUserId;
    private String secondUserName;
    private Integer secondUserMoneyRecord;
    private Integer secondUserMoneyRemain;


    private String thirdUserId;
    private String thirdUserName;
    private Integer thirdUserMoneyRecord;
    private Integer thirdUserMoneyRemain;

    
    private Integer clubId;// 俱乐部id
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

  
	public Integer getClubId() {
		return clubId;
	}

	public void setClubId(Integer clubId) {
		this.clubId = clubId;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getFirstUserId() {
		return firstUserId;
	}

	public void setFirstUserId(String firstUserId) {
		this.firstUserId = firstUserId;
	}

	public String getFirstUserName() {
		return firstUserName;
	}

	public void setFirstUserName(String firstUserName) {
		this.firstUserName = firstUserName;
	}

	public Integer getFirstUserMoneyRecord() {
		return firstUserMoneyRecord;
	}

	public void setFirstUserMoneyRecord(Integer firstUserMoneyRecord) {
		this.firstUserMoneyRecord = firstUserMoneyRecord;
	}

	public Integer getFirstUserMoneyRemain() {
		return firstUserMoneyRemain;
	}

	public void setFirstUserMoneyRemain(Integer firstUserMoneyRemain) {
		this.firstUserMoneyRemain = firstUserMoneyRemain;
	}

	public String getSecondUserId() {
		return secondUserId;
	}

	public void setSecondUserId(String secondUserId) {
		this.secondUserId = secondUserId;
	}

	public String getSecondUserName() {
		return secondUserName;
	}

	public void setSecondUserName(String secondUserName) {
		this.secondUserName = secondUserName;
	}

	public Integer getSecondUserMoneyRecord() {
		return secondUserMoneyRecord;
	}

	public void setSecondUserMoneyRecord(Integer secondUserMoneyRecord) {
		this.secondUserMoneyRecord = secondUserMoneyRecord;
	}

	public Integer getSecondUserMoneyRemain() {
		return secondUserMoneyRemain;
	}

	public void setSecondUserMoneyRemain(Integer secondUserMoneyRemain) {
		this.secondUserMoneyRemain = secondUserMoneyRemain;
	}

	public String getThirdUserId() {
		return thirdUserId;
	}

	public void setThirdUserId(String thirdUserId) {
		this.thirdUserId = thirdUserId;
	}

	public String getThirdUserName() {
		return thirdUserName;
	}

	public void setThirdUserName(String thirdUserName) {
		this.thirdUserName = thirdUserName;
	}

	public Integer getThirdUserMoneyRecord() {
		return thirdUserMoneyRecord;
	}

	public void setThirdUserMoneyRecord(Integer thirdUserMoneyRecord) {
		this.thirdUserMoneyRecord = thirdUserMoneyRecord;
	}

	public Integer getThirdUserMoneyRemain() {
		return thirdUserMoneyRemain;
	}

	public void setThirdUserMoneyRemain(Integer thirdUserMoneyRemain) {
		this.thirdUserMoneyRemain = thirdUserMoneyRemain;
	}

	@Override
	public String toString() {
		return "PlayerRecord [id=" + id + ", roomId=" + roomId + ", startTime="
				+ startTime + ", endTime=" + endTime + ", firstUserId="
				+ firstUserId + ", firstUserName=" + firstUserName
				+ ", firstUserMoneyRecord=" + firstUserMoneyRecord
				+ ", firstUserMoneyRemain=" + firstUserMoneyRemain
				+ ", secondUserId=" + secondUserId + ", secondUserName="
				+ secondUserName + ", secondUserMoneyRecord="
				+ secondUserMoneyRecord + ", secondUserMoneyRemain="
				+ secondUserMoneyRemain + ", thirdUserId=" + thirdUserId
				+ ", thirdUserName=" + thirdUserName
				+ ", thirdUserMoneyRecord=" + thirdUserMoneyRecord
				+ ", thirdUserMoneyRemain=" + thirdUserMoneyRemain
				+ ", clubId=" + clubId + "]";
	}

    
}
