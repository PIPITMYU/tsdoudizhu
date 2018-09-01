package com.up72.server.mina.utils.redis;

import java.util.Set;

import redis.clients.jedis.Jedis;

import com.up72.game.constant.Cnst;
import com.up72.server.mina.utils.MyLog;

public class MyRedis {
	
	private static final MyLog log = MyLog.getLogger(MyRedis.class);
  	private static RedisClient client;
  	
  	public static void initRedis(){
  		client = new RedisClient(null);
  		initCurrentProjectRedis();
		log.I("redis 初始化完成");
  	}
  	
  	public synchronized static RedisClient getRedisClient(){
  		return client;
  	}
  	
  	private static void initCurrentProjectRedis(){
        Jedis jedis = client.getJedis();
        if (jedis == null) {
            try {
				log.E("redis初始化失败");
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
        try {
        	Set<String> allKeys = jedis.keys(Cnst.PROJECT_PREFIX);
        	if (allKeys!=null&&allKeys.size()>0) {
    			for(String key:allKeys){
    				jedis.del(key);
    			}
    		}
        } catch (Exception e) {
            e.printStackTrace();
        	client.returnBrokenJedis(jedis);
        } finally {
            if (jedis!=null) {
            	client.returnJedis(jedis);
			}
        }
    }

}
