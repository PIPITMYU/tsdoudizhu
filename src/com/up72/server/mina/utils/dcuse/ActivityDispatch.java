package com.up72.server.mina.utils.dcuse;

import org.apache.mina.core.session.IoSession;

import com.alibaba.fastjson.JSONObject;
import com.up72.game.service.IUserService;
import com.up72.game.service.impl.UserServiceImpl;
import com.up72.server.mina.bean.ProtocolData;


public class ActivityDispatch {
	private static ActivityDispatch instance = new ActivityDispatch();
	private ActivityDispatch(){}
	public static ActivityDispatch getInstance(){
		return instance;
	}
	public static IUserService userService = new UserServiceImpl();
	//调取用户是否完整参与一局 并判断是否为唐山IP
	public void findUserTotolGame(IoSession session,ProtocolData data){
		JSONObject obj = JSONObject.parseObject(data.getJsonString());
		Long userId = obj.getLong("b");
		JSONObject info = new JSONObject();
		//查找用户ip
		String ip = userService.findIpByUserId(userId);
		if(ip != null){
			String city = HttpGetMessage.findUserIpAddress(ip);
			if(city != null && city.trim().equals("唐山市")){
				info.put("b", 1);
			}else{
				info.put("b", 0);
			}
		}else{
			info.put("b", -1);
		}		
		//查找用户完整的局数
		Integer num = userService.findTotalGameNum(userId);
		if(num != null && num > 0){
			info.put("a", 1);
		}else{
			info.put("a", 0);
		}
		ProtocolData pd  = new ProtocolData();
		pd.setJsonString(info.toJSONString());
		session.write(pd);
	}
}	
