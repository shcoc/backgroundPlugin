package com.ssk.backgroud;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jr.ob.JSON;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.download.DownloadableFileDescription;
import com.intellij.util.download.DownloadableFileService;
import com.intellij.util.download.impl.DownloadableFileDescriptionImpl;
import com.intellij.util.io.IOUtil;
import com.ssk.backgroud.config.BackgroundConfig;
import com.ssk.backgroud.config.Form;
import com.ssk.backgroud.dao.DB;
import com.ssk.backgroud.dao.LocalDB;
import com.ssk.backgroud.dao.SquiteDb;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ImageUtil {
    private static final PropertiesComponent props =
           PropertiesComponent.getInstance();
    public static ImageBean getRandomImage(){
        ImageBean imageBean =  getDatabase().getRandomImage(1).get(0);
        if(imageBean.getType()>0) downloadFile(imageBean);
        return imageBean;
    }

    private static DB getDatabase(){
        if(Form.LOCAL.equals(props.getValue(BackgroundConfig.LOCATION))) return LocalDB.getInstance();
        else if(Form.CACHE.equals(props.getValue(BackgroundConfig.LOCATION))) return SquiteDb.getInstance();
        else {
            downladDb();
            return SquiteDb.getInstance();
        }
    }
    public static void  downladDb(){

    }

    private static String getFileName(ImageBean imageBean){
        int index = imageBean.getUrl().lastIndexOf("/");
        if (index == -1 || index < 7){
            return imageBean.getPath();
        }
        int end = imageBean.getUrl().lastIndexOf(".");
        if (end == -1 || end > imageBean.getUrl().length() - 6){
            end = imageBean.getUrl().length();
        }
        return imageBean.getUrl().substring(index+1,end);
    }
    private static String getFileExtension(ImageBean imageBean){
        int index = imageBean.getUrl().lastIndexOf(".");
        if (index == -1 || index > imageBean.getUrl().length() - 6){
            return ".jpg";
        }
        return imageBean.getUrl().substring(index+1);
    }
    public static void downloadFile(ImageBean imageBean){
        DownloadableFileDescription downloadableFileDescription =
                new DownloadableFileDescriptionImpl(imageBean.getUrl(),
                        getFileName(imageBean),
                        getFileExtension(imageBean));

        DownloadableFileService downloadService = ApplicationManager.getApplication()
                .getService(DownloadableFileService.class);

        String path =    downloadService.createDownloader(
                        new ArrayList<DownloadableFileDescription>(){{
                            add(downloadableFileDescription);
                        }},imageBean.getUrl()).downloadFilesWithProgress(BackgroundConfig.BACKGROUD+ "/image/",null,null)
                .get(0).getPath();
        imageBean.setPath(path);
        DB db = getDatabase();
        if (db instanceof SquiteDb){
            try {
                ((SquiteDb) db).updatePath(imageBean);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public static void buildDb(){
        SquiteDb db = SquiteDb.getInstance();
        deleteDatabase();
        db.reset();
        LocalDB localDB = LocalDB.getInstance();
        List<String> list = localDB.getAllImages();
        String sql = "INSERT INTO image(path, url, type) VALUES (?,?,?)";
        for (String image : list) {
            try {
               db.execute(sql, image, image, 0);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        File versionFile = new File(BackgroundConfig.BACKGROUD+"/.conf.ini");
        if (versionFile.exists()) versionFile.delete();
        try(OutputStream outputStream = new FileOutputStream(versionFile);) {
            IOUtils.write("{\n" +
                    "  \"version\" : 1,\n" +
                    "  \"url\" : \"localhost\"\n" +
                    "}",outputStream,"utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void deleteDatabase(){
        SquiteDb db = SquiteDb.getInstance();
        db.close();
        File dbFile = new File(db.getPath());
        if (dbFile.exists()) dbFile.delete();
    }
    public static  void  resetDatabase(){
        getDatabase().reset();

    }
}
