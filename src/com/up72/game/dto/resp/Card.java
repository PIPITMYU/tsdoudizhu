package com.up72.game.dto.resp;

import java.io.Serializable;

import com.alibaba.fastjson.JSONObject;
import com.up72.game.constant.Cnst;

public class Card implements Comparable<Card>,Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1741273786105836983L;

	private int origin;//三位数牌数
	
	private int type;//黑红梅方
	
	private int symble;//AKQJ987654321 牌点
	
	private int laizi = 0;//如果是癞子牌，那就是这个癞子变成的什么牌 和origin一样 0代表不是癞子
	
	public int getSymble() {
		return symble;
	}

	public void setSymble(int symble) {
		this.symble = symble;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getOrigin() {
		return origin;
	}

	public void setOrigin(int origin) {
		this.origin = origin;
	}
	

	
	public Card(int src){
		this.origin = src;
		this.type = src/100;
		this.symble = src%100;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + origin;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Card other = (Card) obj;
		if (origin != other.getOrigin())
			return false;
		return true;
	}

	@Override
	public int compareTo(Card o) {	
		int my = this.laizi;
		if(my == 0)
			my = this.origin;
		
		int other = o.getLaizi();
		if(other == 0)
			other = o.getOrigin();
		
		int m1 = my % 100;
		int o1 = other % 100;
		
		//如果是大小王
		if(m1 == 14)
			m1 += 2;
		if(o1 == 14)
			o1 += 2;
		//如果是1 2
		if(m1 < 3)
			m1 += 13;
		if(o1 < 3)
			o1 += 13;
		
		if(m1 != o1)
			return o1 - m1;
		
		int t1 = my / 100;
		int t2 = other / 100;
		return t1 - t2;
	}

	@Override
	public String toString() {
		return this.origin+"";
	}
	
	public Card(){
		
	}

	public int getLaizi() {
		return laizi;
	}

	public void setLaizi(int laizi) {
		this.laizi = laizi;
	}
	
	public static boolean isLaizi(Card c){
		return (c.getOrigin() % 100) == 15;
	}
	
	public static int getRealType(Card c){
		if(c.getLaizi() != 0)
		{
			return c.getLaizi() / 100;
		}
		else{
			return c.getOrigin() / 100;
		}
	}
	
	public static int getRealSymble(Card card){
		if(card.getLaizi() != 0)
		{
			return card.getLaizi() % 100;
		}
		else{
			return card.getOrigin() % 100;
		}
	}
	
	//是否是合法的癞子
	public static boolean isLegalLaizi(Card card){
		if(!isLaizi(card))
			return false;
		int tmp = card.getLaizi() % 100;
		if(tmp < 1 || tmp > 13)
			return false;
		tmp = card.getLaizi() / 100;
		if(tmp < 1 || tmp > 4)
			return false;
		
		for (int i = 0; i < Cnst.CANNOT_BECOME.length; i++) {
			if(card.getLaizi() == Cnst.CANNOT_BECOME[i])
				return false;
		}
		
		return true;
	}
	
	public static JSONObject getReturnJson(Card card){
		JSONObject json = new JSONObject();
		json.put("origin", card.getOrigin());
		json.put("laizi", card.getLaizi());
		return json;
	}
}
