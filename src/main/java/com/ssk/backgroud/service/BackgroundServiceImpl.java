package com.ssk.backgroud.service;


import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.impl.IdeBackgroundUtil;
import com.intellij.util.download.DownloadableFileDescription;
import com.intellij.util.download.DownloadableFileService;
import com.intellij.util.download.impl.DownloadableFileDescriptionImpl;
import com.ssk.backgroud.ImageBean;
import com.ssk.backgroud.ImageUtil;
import com.ssk.backgroud.config.Form;
import com.ssk.backgroud.enumbean.TimeEnum;
import com.ssk.backgroud.config.BackgroundConfig;
import com.ssk.backgroud.ui.BackgroundSelect;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BackgroundServiceImpl implements BackgroundService{
    Logger log = com.intellij.openapi.diagnostic.Logger.getInstance(BackgroundSelect.class);
    private ScheduledThreadPoolExecutor executorService;
    private RunnableScheduledFuture future;
    private PropertiesComponent props;
    private Task task;
    private volatile boolean running = false;
    public BackgroundServiceImpl() {
        this.executorService = new ScheduledThreadPoolExecutor(1);
        task = new Task();
        props = PropertiesComponent.getInstance();

    }
    class Task implements Runnable{

        @Override
        public void run() {
            String area = props.getValue(BackgroundConfig.AREA);
            String opacity =props.getValue(BackgroundConfig.OPACITY) == null ? "100":props.getValue(BackgroundConfig.OPACITY) ;
            props.setValue(IdeBackgroundUtil.FRAME_PROP,null);
            props.setValue(IdeBackgroundUtil.EDITOR_PROP,null);
            props.setValue(IdeBackgroundUtil.TARGET_PROP,null);
            ImageBean path = ImageUtil.getRandomImage();
            String url = new BackgroundValueBuilder(path.getPath())
                    .Opacity(Integer.valueOf(opacity)).build();
            if (Form.ALL.equals(area)){
                props.setValue(IdeBackgroundUtil.EDITOR_PROP,url);
                props.setValue(IdeBackgroundUtil.FRAME_PROP,url);
                props.setValue(IdeBackgroundUtil.TARGET_PROP,url);
            } else {
                props.setValue(Form.FRAME.equals(area)?IdeBackgroundUtil.FRAME_PROP:
                        Form.EDIT.equals(area)?IdeBackgroundUtil.EDITOR_PROP:
                                IdeBackgroundUtil.TARGET_PROP,url);
            }
            log.info(url);
        }


    }
    public enum Flip{
        FLIP_HV("flipHV"),FLIP_H("flipH"),FLIP_V("flipV");
        private String name;
        Flip(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        public Flip getFlip(String name) {
            Flip[] flips = Flip.values();
            for (Flip flip : flips) {
                if (flip.toString().equals(name)) return flip;
            }
            return null;
        }
    }
    class BackgroundValueBuilder{
        private String url;
        private Integer opacity;
        private IdeBackgroundUtil.Fill fillType;
        private IdeBackgroundUtil.Anchor anchor;
        private Flip flip;
        public BackgroundValueBuilder(String url) {
            this.url = url;
        }
        public BackgroundValueBuilder Opacity(Integer number){
            this.opacity = number;
            return this;
        }
        public BackgroundValueBuilder FillType(IdeBackgroundUtil.Fill fill){
            this.fillType = fill;
            return this;
        }
        public BackgroundValueBuilder Anchor(IdeBackgroundUtil.Anchor anchor){
            this.anchor = anchor;
            return this;
        }
        public BackgroundValueBuilder Flip(Flip flip){
            this.flip = flip;
            return this;
        }
        public String build(){
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(this.url);
            stringBuilder.append(",");
            stringBuilder.append(opacity==null?"20":opacity);
            stringBuilder.append(",");
            stringBuilder.append(getFill());
            stringBuilder.append(",");
            stringBuilder.append(getAnchor());
            stringBuilder.append(flip == null?"":(","+getFill()));
            return stringBuilder.toString();
        }
        private String getFill(){
           return fillType == IdeBackgroundUtil.Fill.SCALE ? "SCALE" :
                   fillType == IdeBackgroundUtil.Fill.PLAIN ? "PLAIN" :
                           fillType == IdeBackgroundUtil.Fill.TILE ? "TILE":"SCALE";
        }
        private String getAnchor(){
            return anchor == IdeBackgroundUtil.Anchor.BOTTOM_CENTER ? "BOTTOM_CENTER" :
                    anchor == IdeBackgroundUtil.Anchor.BOTTOM_LEFT ? "BOTTOM_LEFT" :
                            anchor == IdeBackgroundUtil.Anchor.BOTTOM_RIGHT ? "BOTTOM_RIGHT" :
                                    anchor == IdeBackgroundUtil.Anchor.CENTER ? "CENTER" :
                                            anchor == IdeBackgroundUtil.Anchor.MIDDLE_LEFT ? "MIDDLE_LEFT" :
                                                    anchor == IdeBackgroundUtil.Anchor.MIDDLE_RIGHT ? "MIDDLE_RIGHT" :
                                                            anchor == IdeBackgroundUtil.Anchor.MIDDLE_RIGHT ? "MIDDLE_RIGHT" :
                                                                    anchor == IdeBackgroundUtil.Anchor.TOP_CENTER ? "TOP_CENTER" :
                                                                            anchor == IdeBackgroundUtil.Anchor.TOP_LEFT ? "TOP_LEFT" :
                                                                                    anchor == IdeBackgroundUtil.Anchor.TOP_RIGHT ? "TOP_RIGHT" : "CENTER";
        }
    }


    @Override
    public synchronized void stop() {
        if (!running) return;
        if (future!=null) {
            executorService.remove(future);
            future.cancel(true);
            future = null;
        }
        running = false;
    }

    @Override
    public synchronized void start() {
        if (running) return;
        TimeEnum circle = TimeEnum.HOURS.getTime(props.getValue(BackgroundConfig.CIRCULATION_METHOD));
        String timeEnum = props.getValue(BackgroundConfig.TIME)==null ? "0" : props.getValue(BackgroundConfig.TIME);
        Integer timeNumber = Integer.valueOf(timeEnum);
        if (timeNumber == 0){
            future =(RunnableScheduledFuture)  executorService.schedule(task,0,TimeUnit.MINUTES);
        }else {
            future = (RunnableScheduledFuture) executorService.scheduleAtFixedRate(task,
                    0, timeNumber,
                    circle == null ? TimeUnit.MINUTES : circle.getUnit());
        }
        running = true;
    }

    @Override
    public void restart() {
        stop();
        start();
    }
}
