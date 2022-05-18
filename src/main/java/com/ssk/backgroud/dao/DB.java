package com.ssk.backgroud.dao;

import com.ssk.backgroud.ImageBean;

import java.util.List;

public interface DB{
    public List<ImageBean> getRandomImage(int number);
    public List<ImageBean>  getTypeImage(int number,int type);
    public void reset();

    public void  connect();

    public  boolean  isClosed();
}