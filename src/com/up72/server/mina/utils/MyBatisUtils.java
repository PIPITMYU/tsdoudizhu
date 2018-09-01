package com.up72.server.mina.utils;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

public class MyBatisUtils {
	

    private static final MyLog logger = MyLog.getLogger(MyBatisUtils.class);
    protected static SqlSessionFactory sqlSessionFactory;
    protected static ThreadLocal<SqlSession> sessions = new ThreadLocal<>();

    public static void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        MyBatisUtils.sqlSessionFactory = sqlSessionFactory;
    }

    public static SqlSession getSession() {
        SqlSession session = sessions.get();
        try {
            logger.I("sqlSession null , open a new sqlSesson and set to thread locad ! ");
            session = sqlSessionFactory.openSession();
            sessions.set(session);
        } catch (Exception e) {
            logger.E("mybatis get sqlSession 抛异常", e);
        }
        return session;
    }

    public static void closeSessionAndCommit() {
        SqlSession session = sessions.get();
        if (session != null) {
            logger.I("close Session and commit sql Transaction ! ");
            try {
                session.commit();
                session.close();
            } catch (Exception e) {
                logger.E("myBatis 提交 事务 并 关闭 连接 失败! ", e);
            } finally {
                sessions.remove();
            }
        } else {
            logger.D("current thread do not has open sql session.");
        }
    }

}
