package com.up72.server.mina.main;

import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.jasper.tagplugins.jstl.core.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sun.org.apache.xml.internal.security.Init;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Card;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.game.service.IUserService;
import com.up72.game.service.impl.UserServiceImpl;
import com.up72.game.service.impl.UserService_loginImpl;
import com.up72.server.mina.function.GameFunctions;
import com.up72.server.mina.utils.DataLoader;
import com.up72.server.mina.utils.dcuse.GameUtil;
import com.up72.server.mina.utils.redis.MyRedis;
import com.up72.server.mina.utils.redis.RedisUtil;

public class mmm {
		public static void main2(String[] args) throws UnsupportedEncodingException {
		String string="张辽、乐进、于禁、张郃、徐晃、郝昭、王双、典韦、许诸、庞德 、郭嘉、程昱、荀彧、荀攸、贾诩、司马懿、华歆 、关羽、张飞、赵云、马超、黄忠 、魏延、姜维、马岱、刘封、严颜、王平、关平、关兴、张苞、张翼、张嶷、马忠、吴班、诸葛瞻 、诸葛亮、庞统、法正、蒋琬、费袆、姜维、李严、董允、谯周 、周瑜、鲁肃、吕蒙、陆逊 、程普、黄盖、太史慈、甘宁、凌统、周泰、韩当、徐盛、丁奉、陆抗 、张昭、张纮、顾雍、诸葛瑾、惜文、惜雪、惜玉、夏菡、夏兰、夏岚、夏青、夏彤、夏旋、霞绮、霞飞、霞辉、霞姝、霞月、霞英、霞雰、霞影、霞赩、霞文、湘云、香馨、向卉、向彤、向雪、晓燕、晓莉、晓凡、晓兰、晓曼、晓霜、笑寒、心语　心诺、心远、新梅、欣美、欣然、欣悦、欣欣、欣嘉、欣荣、欣愉、欣可、欣畅、欣跃、欣合、欣笑、欣艳、新蕾、新雪、新月、馨香、馨逸、馨荣、馨兰、馨欣、秀英、秀越、秀竹、秀妮、秀洁、秀艳、璇玑、璇子、秀丽、秀美、秀逸、秀雅、秀华、秀兰、秀颖、秀隽、秀曼、秀媛、秀筠、秀慧、秀媚、秀婉、秀艾、秀敏、心香、心愫、心宜、心怡、璇珠、雪枫、雪卉、雪曼、雪萍、雪晴、寻春、寻绿、寻芳";
		String[] arr=string.split("、");
		System.out.println(arr.length);
		String name ="";
		Random random =new Random();
		String urls="http://c.hiphotos.baidu.com/image/pic/item/32fa828ba61ea8d337009ec49e0a304e241f58d4.jpg、https://ss0.bdstatic.com/70cFvHSh_Q1YnxGkpoWK1HF6hhy/it/u=370586742,3427985766&fm=27&gp=0.jpg、https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=1603268314,2824105081&fm=11&gp=0.jpg、https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1507716583286&di=2902a45cca6c15a2d6a09239017855f7&imgtype=0&src=http%3A%2F%2Ftx3.cdn.caijing.com.cn%2F2014%2F0810%2F1407667783947.jpg、https://ss3.baidu.com/9fo3dSag_xI4khGko9WTAnF6hhy/image/h%3D220/sign=3a10fbe83f7adab422d01c41bbd5b36b/bf096b63f6246b60ce3afab4e2f81a4c510fa23e.jpg、https://ss0.bdstatic.com/70cFvHSh_Q1YnxGkpoWK1HF6hhy/it/u=977303188,3073549220&fm=200&gp=0.jpg、https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1507716666933&di=eadc5bc8852fe3beb31c697613ff6469&imgtype=0&src=http%3A%2F%2Fp3.wmpic.me%2Farticle%2F2015%2F04%2F16%2F1429148519_jebMcnBl.jpg、https://ss0.bdstatic.com/70cFuHSh_Q1YnxGkpoWK1HF6hhy/it/u=4175143789,503432233&fm=27&gp=0.jpg、https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=376789479,3651003364&fm=27&gp=0.jpg、https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1507716708826&di=62447c2b017222e48495fb00f0b954e7&imgtype=0&src=http%3A%2F%2Fpic1.win4000.com%2Fwallpaper%2F8%2F53e1fdcbab20a.jpg、https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1507716745493&di=abdbf132fa10be59418b93382240fde9&imgtype=0&src=http%3A%2F%2Fimg2.3lian.com%2F2014%2Ff2%2F169%2Fd%2F16.jpg、https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1507716767032&di=26ad9bacf24c3471cfa5865f6219aa8f&imgtype=0&src=http%3A%2F%2Fwapfile.desktx.com%2Fpc%2F161214%2Fbigpic%2F584fac2d9a3c7.jpg、https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1507716767971&di=759164c7c9fcd8eec1ac483e5c10e669&imgtype=0&src=http%3A%2F%2Fupfile2.asqql.com%2Fupfile%2F2009pasdfasdfic2009s305985-ts%2Fgif_spic%2F2013-2%2F2013216044590275_asqql_com.jpg、https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1507716770132&di=00ccb66e5a7701169c1e499531f1ac8e&imgtype=0&src=http%3A%2F%2Fwww.funfate.com%2Fwp-content%2Fuploads%2F2016%2F12%2F03%2F489074010fayafy.jpg、https://ss2.bdstatic.com/70cFvnSh_Q1YnxGkpoWK1HF6hhy/it/u=3266194635,2789998179&fm=27&gp=0.jpg、https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1507716775084&di=11b47bd9da0a18af36e4b721be650944&imgtype=0&src=http%3A%2F%2Fpic1.win4000.com%2Fwallpaper%2Ff%2F555c593c9a122.jpg、https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1507716777430&di=bdd336b59107a32f866f3f707f454a48&imgtype=0&src=http%3A%2F%2Fpic1.win4000.com%2Fwallpaper%2Fb%2F53a0ea7242684.jpg、https://ss0.bdstatic.com/70cFuHSh_Q1YnxGkpoWK1HF6hhy/it/u=662379545,3945683145&fm=27&gp=0.jpg、http://wanzao2.b0.upaiyun.com/system/pictures/24768446/original/8a0a97828f60f297.png";
		String[] urll=urls.split("、");
		String names="";
		String url ="";
		int  z=136308;
		for (int i = 0; i <=100; i++) {
			String openid ="wsw-";
			String headimgurl ="";
			int x=random.nextInt(arr.length);
			int y=random.nextInt(urll.length);
//			name=arr[x];
			name = java.net.URLEncoder.encode(arr[x],"utf-8");//转url中文
			headimgurl=urll[y];
			if(names.contains(name)) {
				name+=1;
			}
			z++;
			 long currentTimeMillis = System.currentTimeMillis();
			names+=name;
			openid+=i;
			url+="INSERT INTO  GAME_USER(user_id,open_id ,user_name,user_img,gender,total_game_num,money,user_agree,login_status,sign_up_time,last_login_time,cid) VALUES('"+z+"','"+openid+"','"+name+"','"+headimgurl+"','"+1+"','"+0+"','"+0+"','"+0+"','"+1+"','"+ currentTimeMillis+"','"+currentTimeMillis+"','"+1+"'\r\n" + 
					");\r\n";
		}
		try {
			OutputStream out = new FileOutputStream("shuChuWenJian.txt",true);
			out.write(url.getBytes());
			out.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
		public static IUserService userService = new UserServiceImpl();
		public static void main1(String[] args) throws UnsupportedEncodingException {
//			{a:"100100",F:"wsw-30",G:7}
//			创建房间：{p:"5", a:"100007", q:"1", r:"0", s:"0", t:"1", u:"0", J:"136339", aI:"1", m:"8", n:"1", o:"1"}
//			请求准备：{a:"100200",J:"136339",y:"341764"}
//			{a:"100100",F:"wsw-31",G:7}
//			加入房间：{a:"100008",J:"136340",y:"341764"}
//			请求准备：{a:"100200",J:"136340",y:"341764"}
//			{a:"100100",F:"wsw-32",G:7}
//			加入房间：{a:"100008",J:"136341",y:"341764"}
//			请求准备：{a:"100200",J:"136341",y:"341764"}
//			{a:"100100",F:"wsw-33",G:7}
//			加入房间：{a:"100008",J:"136342",y:"341764"}
//			请求准备：{a:"100200",J:"136342",y:"341764"}
			
			
			
		}
	
	
 		
		
	public static void main(String[] args) {
		DataLoader.initMybatis();
		Integer money = userService.getMoneyInit("110");
		System.out.println(money);
		
	}
	 public static void main3(String[] args) {
//		 MyRedis.initRedis();
//		 List<String> list = new ArrayList<String>();
////		 RedisUtil.setObject("1112", list, null);
//		 RedisUtil.lpush("1111", null,"111");
//		 RedisUtil.lrange("1111", 0, 1);
//		 String a = null;
//		 StringBuffer stringBuffer = new StringBuffer();
//		 stringBuffer.append("h").append("w").append("adsfa");
//		 int hashCode = new HashCodeBuilder()
//	        .append(a)
//	        .toHashCode();
//		 
//		 
//		 StringBuffer stringBuffer1 = new StringBuffer();
//		 stringBuffer1.append("h").append("w").append("adsfa");
//		 int hashCode2 = new HashCodeBuilder()
//	        .append(stringBuffer1.toString())
//	        .toHashCode();
//		 
//		 System.out.println(hashCode);
//		 System.out.println(hashCode2);
//		 Math.floor(Math.sqrt(10));
		 //1张赖子，3个A，3个K，2个9，1张8，2张7，
		 List<Card> list = new ArrayList<Card>();
		 
		 list.add(new Card(102));
		 list.add(new Card(202));
		 list.add(new Card(302));
		 list.add(new Card(101));
		 list.add(new Card(201));
		 list.add(new Card(301));
	
		 list.add(new Card(208));
		 list.add(new Card(108));
		 list.add(new Card(206));
		 list.add(new Card(106));
		 
		 Collections.sort(list);
		 
		 List<ArrayList<Card>> handlerCard = GameFunctions.handlerCard(list);
		
		 int realLastCardType = GameFunctions.getCardType(handlerCard,1,
					0);
		 
		 System.out.println(realLastCardType);
			
	}
}
