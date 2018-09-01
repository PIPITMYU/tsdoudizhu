package com.up72.server.mina.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 项目信息文件获取工具类
 * @author sbb
 * 
 */
public class ProjectInfoPropertyUtil {
	private static final Logger logger = LoggerFactory.getLogger(ProjectInfoPropertyUtil.class);
    private static Properties props;
    static{
        loadProps();
    }
    
    // 单例模式实现初始化加载properties文件
    private synchronized static void loadProps(){
        logger.info("开始加载properties文件内容.......");
        props = new Properties();
        InputStream in = null;
        try {
        	//第一种，通过类加载器进行获取properties文件流
        	 in = ProjectInfoPropertyUtil.class.getClassLoader().getResourceAsStream("projectInfo.properties");
            //第二种，通过类进行获取properties文件流
        	//in = ProjectInfoPropertyUtil.class.getResourceAsStream("/projectInfo.properties");
            props.load(in);
        } catch (FileNotFoundException e) {
            logger.error("projectInfo.properties文件未找到");
        } catch (IOException e) {
            logger.error("出现IOException");
        } finally {
            try {
                if(null != in) {
                    in.close();
                }
            } catch (IOException e) {
                logger.error("projectInfo.properties文件流关闭出现异常");
            }
        }
        logger.info("加载properties文件内容完成...........");
//        logger.info("properties文件内容：" + props);
    }
    // 获取配置中的属性
    public static String getProperty(String key){
    	// 单例配置实例未被创建，将重新创建实例
        if(null == props) {
            loadProps();
        }
        return props.getProperty(key);
    }
    // 获取配置中的属性,如果没有找到将给该值赋予默认值
    public static String getProperty(String key, String defaultValue) {
    	// 单例配置实例未被创建，将重新创建实例
        if(null == props) {
            loadProps();
        }
        return props.getProperty(key, defaultValue);
    }
    
    // test 读取配置方法
    public static void main(String[] args) {
    	String version = ProjectInfoPropertyUtil.getProperty("project_version");
    	System.out.println(version);
    }
}