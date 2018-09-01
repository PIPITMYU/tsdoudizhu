package com.up72.server.mina.function;

import java.util.List;

import com.up72.game.dto.resp.RoomResp;

public class Demo {
	public static void main(String[] args) {
		mo();
		
	}

	
	private static void mo() {
		RoomResp room = new RoomResp();
		room.initRoom();
		room.getRealMuls().add(0,1);
		room.getRealMuls().add(1,2);
//		room.getRealMuls().add(3,3);
		System.out.println(room.toString());
	}
}
