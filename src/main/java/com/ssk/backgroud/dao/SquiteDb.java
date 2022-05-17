package com.ssk.backgroud.dao;

import com.intellij.openapi.diagnostic.Logger;
import com.ssk.backgroud.ImageBean;
import com.ssk.backgroud.ui.BackgroundSelect;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import java.io.*;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class SquiteDb implements DB {
    @Override
    public List<ImageBean> getRandomImage(int number) {
        return getTypeImage(number,-1);
    }

    @Override
    public List<ImageBean> getTypeImage(int number, int type) {
        String maxSql = "SELECT MAX(id) FROM image WHERE type >= ?";
        List<ImageBean> list = new LinkedList<ImageBean>();
        try (PreparedStatement preparedStatement = buildPreparedStatement("maxSql",type)){
            ResultSet resultSet = preparedStatement.executeQuery();
            int maxColumn =  resultSet.next()? 0:resultSet.getInt(0);
            String sql = "SELECT * FROM image WHERE type >= ? AND id >= ?" ;
            try (PreparedStatement preparedStatementResult =
                         buildPreparedStatement(sql,type,random.nextInt(maxColumn))){
                 resultSet = preparedStatement.executeQuery();
                 while(resultSet.next()){
                     ImageBean imageBean = new ImageBean();
                     imageBean.setId(resultSet.getInt("id"));
                     imageBean.setPath(resultSet.getString("path"));
                     imageBean.setUrl(resultSet.getString("url"));
                     imageBean.setType(resultSet.getInt("type"));
                     list.add(imageBean);
                 }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return list;
    }

    public  synchronized void updatePath(ImageBean imageBean) throws SQLException {
        String sql = "UPDATE image SET path = ? and type = 0 WHERE id = ?";
        execute(sql,imageBean.getPath(),imageBean.getId());
    }
    @Override
    public void reset() {
        try {
            close();
            initDb();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public void close() {
        try {
            connectionLocal.get().close();
            connectionLocal.remove();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static SquiteDb getInstance() {
        return Squite.instance;
    }
    private static class Squite{
        public static final SquiteDb instance = new SquiteDb();
    }

    private SquiteDb() {
        File t = new File(System.getProperty("user.home"), ".ideaBackground");;
        url = new File(System.getProperty("user.home"), ".ideaBackground").getAbsolutePath();
        path = url + "/image.db";
        url = "jdbc:sqlite:" + path;
        try {
            initDb();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    static Logger log = com.intellij.openapi.diagnostic.Logger.getInstance(BackgroundSelect.class);
    protected  String url = "";
    protected  String path = "";
    protected  String driverName = "org.sqlite.JDBC";
    private  final ConnectionLocal connectionLocal = new ConnectionLocal();
    private  class ConnectionLocal extends ThreadLocal<Connection> {
        @Override
        protected Connection initialValue() {
            try {
                return DriverManager.getConnection(url);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return null;
        }
    }
    private Random random = new Random();

    protected  void initDb() throws ClassNotFoundException {
        log.info("init 加载驱动:" + driverName);
        Class.forName(driverName);
        log.info("init :{}" + "加载数据库");
        if (new File(path).exists()) return;
        log.info("init 数据库不存在");
        createDatabase(path);
    }
    public  void createDatabase(String path){
        Connection connection = connectionLocal.get();
        try (Statement statement = connection.createStatement()) {
            List<String> sql = readSql();
            for (String s : sql) {
                if (log.isDebugEnabled())
                    log.debug("sql:{}", s);
                statement.addBatch(s);
            }
            statement.executeBatch();
        } catch (SQLException e) {
            new File(path).delete();
            e.printStackTrace();
        }
    }
    private  List<String> readSql() {
        File file = new File(com.ssk.backgroud.listener.ApplicationListener.class.getClassLoader()
                .getResource("").getFile());
        List<String> list = new LinkedList<String>();
        if (file.isDirectory()) {
            for (File listFile : file.listFiles((FilenameFilter) new SuffixFileFilter(".sql"))) {
                StringBuilder stringBuilder = new StringBuilder();
                if (log.isDebugEnabled())
                    log.debug("sql文件:{}", listFile.getName());
                try (FileInputStream fileInputStream = new FileInputStream(listFile);
                     BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));) {
                    String string = null;
                    while ((string = bufferedReader.readLine()) != null) {
                        if (!string.startsWith("--"))
                            stringBuilder.append(string);
                    }
                    String[] sql = stringBuilder.toString().split(";");
                    for (int i = 0; i < sql.length; i++) {
                        list.add(sql[i]);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }
    public  Connection getConnection() {
        return connectionLocal.get();
    }

    public  synchronized void execute(String sql, Object... params) throws SQLException {
        if (log.isDebugEnabled())
            log.debug("sql:{} , params:{}", sql, Arrays.deepToString(params));
        PreparedStatement preparedStatement = buildPreparedStatement(sql,params);
        preparedStatement.execute();
        preparedStatement.close();
    }

     public synchronized PreparedStatement buildPreparedStatement(String sql, Object... params) throws SQLException{
         if (log.isDebugEnabled())
             log.debug("sql:{} , params:{}", sql, Arrays.deepToString(params));
         Connection connection = getConnection();
         List list = new LinkedList<Map<String, Object>>();
         PreparedStatement preparedStatement = connection.prepareStatement(sql);
             for (int i = 0; i < params.length; i++) {
                 preparedStatement.setObject(i + 1, params[i]);
             }
            return preparedStatement;
     }
    public  synchronized List<Map<String, Object>> query(String sql, Object... params) {
        if (log.isDebugEnabled())
            log.debug("sql:{} , params:{}", sql, Arrays.deepToString(params));
        List list = new LinkedList<Map<String, Object>>();
        try (PreparedStatement preparedStatement
                     = buildPreparedStatement(sql,params)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            List<String> column = new LinkedList<String>();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int cnt = metaData.getColumnCount();
            for (int i = 0; i < cnt; i++) {
                column.add(metaData.getColumnLabel(i));
            }
            while (resultSet.next()) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                for (String s : column) {
                    map.put(s, resultSet.getObject(s));
                }
                list.add(map);
            }
            return list;
        } catch (SQLException throwables) {
            throwables.printStackTrace();

        }
        return list;
    }

    public  synchronized boolean exits(String sql, Object... params) {
        if (log.isDebugEnabled())
            log.debug("sql:{} , params:{}", sql, Arrays.deepToString(params));
        Connection connection = getConnection();
        try (PreparedStatement preparedStatement
                     = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }
            return preparedStatement.executeQuery().next();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public String getPath() {
        return path;
    }

}


