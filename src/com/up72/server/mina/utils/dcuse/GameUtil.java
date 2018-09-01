package com.up72.server.mina.utils.dcuse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.up72.game.dto.resp.Card;

public class GameUtil {
//    //判断是否有轰
//    public static boolean checkHong(List<Card> playCard,Integer num){
//    	Map<Integer,Integer> mapCard = new HashMap<Integer, Integer>();
//		for(int i=0;i<num;i++){
//			int sortNum = playCard.get(i).getSortNum();
//			if(mapCard.get(sortNum)==null){
//				mapCard.put(sortNum, 1);
//			}else{
//				mapCard.put(sortNum, mapCard.get(sortNum)+1);
//			}
//		}
//		Iterator<Map.Entry<Integer, Integer>> it = mapCard.entrySet().iterator();
//		while (it.hasNext()) {
//			Map.Entry<Integer, Integer> entry = it.next();
//			//轰
//			if(entry.getValue()==3 || entry.getValue()==4){
//				return true;
//			}
//		} 
//		return false;
//    }
    //数组转List
    public static List<Long> changeList(Long[] ids){
    	List<Long> list = new ArrayList<Long>();
    	for(Long l:ids){
    		list.add(l);
    	}
    	return list;
    }
    //判断是否有独储
    public static boolean checkDuChu(List<Card> playCard,Integer colorChu){
    	int count = 0; 
    	if(colorChu == 1 || colorChu ==3){
    		for(int i=0;i<playCard.size();i++){
        		Card card = playCard.get(i);
        		if(card.getSymble() == 1 && (card.getType()==2 || card.getType()==4)){
        			count++;
        		}
        	}
    	}
    	if(colorChu == 2 || colorChu ==4){
    		for(int i=0;i<playCard.size();i++){
        		Card card = playCard.get(i);
        		if(card.getSymble() == 1 && (card.getType()==1 || card.getType()==3)){
        			count++;
        		}
        	}
    	}
    	if(count==2){
    		return true;
    	}
    	return false;
    }
    //检测sessionId
    public static boolean checkSessionId(String sessionId){
    	if(sessionId == null || sessionId.trim().equals(sessionId)){
    		return false;
    	}
    	return true;
    }
}
