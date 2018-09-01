package com.up72.game.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoomRecord {
	private int roomId;
	private long createTime;
	private List<Map> records = new ArrayList<Map>();

	
	public int getRoomId() {
		return roomId;
	}


	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}


	public long getCreateTime() {
		return createTime;
	}


	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public List<Map> getRecords() {
		return records;
	}


	public void setRecords(List<Map> records) {
		this.records = records;
	}


	@Override
	public boolean equals(Object obj) {
		if(obj instanceof RoomRecord)
		{
			RoomRecord o = (RoomRecord) obj;
			return o.getRoomId() == this.roomId;
		}
		return false;
	}
}
