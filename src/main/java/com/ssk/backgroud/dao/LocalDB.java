package com.ssk.backgroud.dao;

import com.intellij.ide.util.PropertiesComponent;
import com.ssk.backgroud.ImageBean;
import com.ssk.backgroud.config.BackgroundConfig;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class LocalDB implements DB{
    private static class Local{
        public static LocalDB instance = new LocalDB();
    }
    @Override
    public List<ImageBean>  getRandomImage(int number) {
        String[] strings = new String[number];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = list.get(i);
        }
        for (int i = strings.length; i < list.size(); i++) {
            int num = random.nextInt(i);
            if (num <= strings.length -1){
                strings[num] = list.get(i);
            }
        }
        List<ImageBean> images = new LinkedList<ImageBean>();
        for (int i = 0; i < strings.length; i++) {
            images.add(new ImageBean(strings[i],strings[i],0));
        }
        return  images;
    }

    @Override
    public List<ImageBean>  getTypeImage(int number,int type) {
        if (type == 0) return  getRandomImage(number);
        return new LinkedList<ImageBean>();
    }
    public static LocalDB getInstance(){
        return Local.instance;
    }
    private Random random = new Random();
    private LinkedList<String> list;
    private PropertiesComponent props;

    private void getImage(File path){
        if (!path.exists()) return;
        if (path.isDirectory()){
            File[] files = path.listFiles();
            for (File file : files) {
                getImage(file);
            }
        }else {
            if (isImage(path)) list.add(path.getAbsolutePath());
        }
    }

    private LocalDB() {
        list = new LinkedList<String>();
        props =  PropertiesComponent.getInstance();
        reset();

    }

    @Override
    public void reset(){
        list.clear();
        connect();
    }
    public List<String> getAllImages(){
        return list;
    }
    private boolean isImage(File url){
        int index = url.getAbsolutePath().lastIndexOf(".");
        if (index == -1 || index < url.getAbsolutePath().length() - 5){
            return  false;
        }
        String str  = url.getAbsolutePath().substring(index).toUpperCase();
        switch(str){
            case  ".JPEG" : return true;
            case  ".TIFF" : return true;
            case  ".PNG" : return true;
            case  ".GIF" : return true;
            case  ".RAW" : return true;
            case  ".EPS" : return true;
            case  ".SVG" : return true;
            case  ".BMP" : return true;
            case  ".JPG" : return true;
            default:return false;
        }
    }

    @Override
    public void connect() {
        File file = new File(props.getValue(BackgroundConfig.FILE_PATH));
        getImage(file);
    }

    @Override
    public boolean isClosed() {
        return list.isEmpty();
    }
}
