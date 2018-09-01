package com.up72.server.mina.utils.dcuse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import com.alibaba.fastjson.JSONObject;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Param;
import com.ning.http.client.Response;

public class HttpGetMessage {
	public static String findUserIpAddress(String ip) {
		String api_url = "http://ip.taobao.com/service/getIpInfo.php?ip=";
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("ip", ip);
		String resString = "";
		JSONObject data  = null;
		try {
			resString = HttpGetMessage.simplePost(api_url, paramMap);
			JSONObject response = JSONObject.parseObject(resString);
			data = response.getJSONObject("data");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data.get("city").toString();
	}

	public static String simplePost(String url, Map<String, String> postParamMap)
			throws Exception {
		String result = null;
		try {
			List<Param> postParam = new ArrayList<Param>();
			if (null != postParamMap && !postParamMap.isEmpty()) {
				Set<String> keySet = postParamMap.keySet();
				for (String key : keySet) {
					String v = null == postParamMap.get(key) ? ""
							: postParamMap.get(key);
					postParam.add(new Param(key, v));
				}
			}
			@SuppressWarnings("resource")
			AsyncHttpClient client = new AsyncHttpClient();
			Future<Response> f = client.preparePost(url)
					.setFormParams(postParam).execute();

			result = f.get().getResponseBody("utf-8");
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		return result;
	}
	public static void main(String[] args) {
		System.out.println(HttpGetMessage.findUserIpAddress("202.99.166.4"));
	}
}
