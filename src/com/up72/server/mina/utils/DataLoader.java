package com.up72.server.mina.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.datasource.pooled.PooledDataSourceFactory;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.up72.game.constant.Cnst;

public class DataLoader {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);
    public static String weixinOpenId = "";

    private static String jdbcUrl_work = ProjectInfoPropertyUtil.getProperty("jdbcUrl_work", "1.5");
    public static String jdbcName_work = ProjectInfoPropertyUtil.getProperty("jdbcName_work", "1.5");
    private static String jdbcPass_work = ProjectInfoPropertyUtil.getProperty("jdbcPass_work", "1.5");
    

    //线上测试ip：47.93.61.29   数据库登陆信息：root/up72@2037
    private static String jdbcUrl_login = ProjectInfoPropertyUtil.getProperty("jdbcUrl_login", "1.5");
    private static String jdbcName_login = ProjectInfoPropertyUtil.getProperty("jdbcName_login", "1.5");
    private static String jdbcPass_login = ProjectInfoPropertyUtil.getProperty("jdbcPass_login", "1.5");
    
    private static final String DB_ENVIRONMENT_WORK = "work";
    private static final String DB_ENVIRONMENT_LOGIN = "login";

    public static void initMybatis() {
        logger.info("init mybatis");
        if (ProjectInfoPropertyUtil.getProperty("develop", "0").equals("0")) {//非测试环境
			Cnst.isTest =false;
		}else{//测试环境
			Cnst.SERVER_IP = jdbcUrl_work.substring(jdbcUrl_work.indexOf("//")+2,jdbcUrl_work.lastIndexOf(":"));
			Cnst.HTTP_URL = "http://".concat(Cnst.SERVER_IP).concat(":").concat(ProjectInfoPropertyUtil.getProperty("httpUrlPort", "8086")).concat("/");
		}
        logger.info("请求地址为："+Cnst.HTTP_URL);
        try {
            Properties properties = new Properties();
            properties.setProperty("driver", "com.mysql.jdbc.Driver");
            properties.setProperty("url", jdbcUrl_work);
            properties.setProperty("username", jdbcName_work);
            properties.setProperty("password", jdbcPass_work);
            

            properties.setProperty("poolPingEnabled", "true");
            properties.setProperty("poolPingQuery", "select 1");
            
            PooledDataSourceFactory pooledDataSourceFactory = new PooledDataSourceFactory();
            pooledDataSourceFactory.setProperties(properties);
            DataSource dataSource = pooledDataSourceFactory.getDataSource();

            TransactionFactory transactionFactory = new JdbcTransactionFactory();
            Environment environment = new Environment(DB_ENVIRONMENT_WORK, transactionFactory, dataSource);

            String resource = "com/up72/server/mina/utils/mybatis-config.xml";
            InputStream inputStream;
            inputStream = Resources.getResourceAsStream(resource);

            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream, DB_ENVIRONMENT_WORK);

            sqlSessionFactory.getConfiguration().setEnvironment(environment);
            MyBatisUtils.setSqlSessionFactory(sqlSessionFactory);

            inputStream.close();
            inputStream = null;
            
            

//            Properties properties_login = new Properties();
//            properties_login.setProperty("driver", "com.mysql.jdbc.Driver");
//            properties_login.setProperty("url", jdbcUrl_login);
//            properties_login.setProperty("username", jdbcName_login);
//            properties_login.setProperty("password", jdbcPass_login);
//            
//
//            properties_login.setProperty("poolPingEnabled", "true");
//            properties_login.setProperty("poolPingQuery", "select 1");
//            
//            PooledDataSourceFactory pooledDataSourceFactory_login = new PooledDataSourceFactory();
//            pooledDataSourceFactory_login.setProperties(properties_login);
//            DataSource dataSource_login = pooledDataSourceFactory_login.getDataSource();
//
//            TransactionFactory transactionFactory_login = new JdbcTransactionFactory();
//            Environment environment_login = new Environment(DB_ENVIRONMENT_LOGIN, transactionFactory_login, dataSource_login);
//
//            String resource_login = "com/up72/server/mina/utils/mybatis-config-login.xml";
//            InputStream inputStream_login;
//            inputStream_login = Resources.getResourceAsStream(resource_login);
//
//            SqlSessionFactory sqlSessionFactory_login = new SqlSessionFactoryBuilder().build(inputStream_login, DB_ENVIRONMENT_LOGIN);
//
//            sqlSessionFactory_login.getConfiguration().setEnvironment(environment_login);
//            MyBatisUtils_login.setSqlSessionFactory(sqlSessionFactory_login);
//            
//
//            inputStream_login.close();
//            inputStream_login = null;
            
            
            
            
            

        } catch (IOException e) {
            logger.error("mybatisUtils 初始化 SqlSessionFactory 报错 ！ ", e);
        }
    }

}
