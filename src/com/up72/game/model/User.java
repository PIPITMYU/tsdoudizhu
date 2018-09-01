/*
 * Powered By [up72-framework]
 * Web Site: http://www.up72.com
 * Since 2006 - 2017
 */

package com.up72.game.model;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 
 * 
 * @author up72
 * @version 1.0
 * @since 1.0
 */
public class User implements java.io.Serializable{

//    private Long id;
    public Long userId;
    private String openId;
    private String userName;
    private String userImg;
    private String gender;
    private String totalGameNum;
    private Integer money;//房卡数
    private Integer userAgree;//1同意
    private Integer loginStatus;
    private String signUpTime;
    private String lastLoginTime;
    private String cId;

//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserImg() {
        return userImg;
    }

    public void setUserImg(String userImg) {
        this.userImg = userImg;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getTotalGameNum() {
        return totalGameNum;
    }

    public void setTotalGameNum(String totalGameNum) {
        this.totalGameNum = totalGameNum;
    }

    public Integer getMoney() {
        return money;
    }

    public void setMoney(Integer money) {
        this.money = money;
    }

    public Integer getUserAgree() {
        return userAgree;
    }

    public void setUserAgree(Integer userAgree) {
        this.userAgree = userAgree;
    }

    public Integer getLoginStatus() {
        return loginStatus;
    }

    public void setLoginStatus(Integer loginStatus) {
        this.loginStatus = loginStatus;
    }

    public String getSignUpTime() {
        return signUpTime;
    }

    public void setSignUpTime(String signUpTime) {
        this.signUpTime = signUpTime;
    }

    public String getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(String lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getUserId())
            .toHashCode();
    }

    public boolean equals(Object obj) {
        if(obj instanceof User == false) return false;
        if(this == obj) return true;
        User other = (User)obj;
        
        if(other.getUserId() == null && this.userId == null)
        	return true;
        
        if(this.userId != null && this.userId.equals(other.getUserId()))
        	return true;
        
        return false;
    }

    public String getCId() {
        return cId;
    }

    public void setCId(String cId) {
        this.cId = cId;
    }

    
}

