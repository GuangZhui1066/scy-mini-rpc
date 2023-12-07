package com.minirpc.nameservice;

import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import com.minirpc.api.NameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 使用 MySQL 存储注册路由信息
 * 相比于本地文件的实现，支持多机注册和调用
 *
 * 创建数据库 MiniRPC
 * 创建数据表 name_service_data
 *
 *      CREATE TABLE `name_service_data` (
 *          `service_name` varchar(255) DEFAULT NULL,
 *          `uri` varchar(255) DEFAULT NULL
 *      ) ENGINE=InnoDB DEFAULT CHARSET=utf8
 */
public class MySQLNameService implements NameService {

    private static final Logger logger = LoggerFactory.getLogger(MySQLNameService.class);


    @Override
    public void connect(URI nameServiceUri) {
    }

    @Override
    public void registerService(String serviceName, URI uri) throws IOException {
        Connection conn = getConnection();
        try {
            //判断是否已存在
            if (isHasSerivce(serviceName, conn)) {
                updateServiceStatus(serviceName, conn, uri);
                //更新操作
                return;
            }

            String sql =
                "insert into name_service_data " +
                "(service_name, uri) " +
                "values ('" +
                    serviceName + "', '" + uri + "'" +
                ")";

            Statement statement = conn.createStatement();
            statement.execute(sql);
            statement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            releaseConnection(conn);
        }
    }

    public boolean isHasSerivce(String serviceName, Connection conn) throws SQLException {
        String sql=
            "select * " +
            "from name_service_data " +
            "where service_name = '" + serviceName + "'"
            ;

        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(sql);

        if (rs.next()) {
            return true;
        }

        rs.close();
        statement.close();
        return false;
    }

    public void updateServiceStatus(String serviceName, Connection conn, URI uri) throws SQLException {
        String sql =
            "update name_service_data " +
            "set uri = '" + uri + "' " +
            "where service_name = '" + serviceName + "'"
            ;

        Statement statement =conn.createStatement();
        int rs = statement.executeUpdate(sql);

        if (rs > 0) {
            logger.info("MySQLNameService. service(" + serviceName + ") uri updated.");
        }

        statement.close();
    }

    @Override
    public URI lookupService(String serviceName) throws IOException {
        URI providerUri = null;
        Connection conn = getConnection();
        try {
            String sql =
                "select * " +
                "from name_service_data" + " " +
                "where service_name = '" + serviceName + "'"
                ;

            Statement statement =conn.createStatement();
            ResultSet rs = statement.executeQuery(sql);

            while (rs.next()) {
                providerUri = URI.create(rs.getString("uri"));
            }
            if (providerUri == null) {
                throw new RuntimeException("MySQLNameService. service(" + serviceName + ") was offline");
            }

            rs.close();
            statement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            releaseConnection(conn);
        }

        return providerUri;
    }


    @Override
    public void close() throws IOException {

    }


    /**
     * 获取数据库连接
     */
    public Connection getConnection() {
        Connection conn = null;

        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://localhost:3306/MiniRPC?useSSL=false&useUniCode=true&characterEncoding=utf8";
        String user = "root";
        String password = "";

        Properties connectionProps = new Properties();
        connectionProps.setProperty("user", user);
        connectionProps.setProperty("password", password);

        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, connectionProps);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * 释放数据库连接
     */
    public static void releaseConnection(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
