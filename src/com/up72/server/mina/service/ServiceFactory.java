package com.up72.server.mina.service;

import com.up72.game.service.IUserService;
import com.up72.game.service.impl.UserServiceImpl;

public class ServiceFactory {

	public static IUserService $UserService(){
		IUserService userService = new UserServiceImpl();
		return  userService;
	}
}
