package com.ssk.backgroud.listener;


import com.intellij.ide.AppLifecycleListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.ssk.backgroud.service.BackgroundService;

import org.jetbrains.annotations.NotNull;


public class ApplicationListener implements AppLifecycleListener {
    @Override
    public void appStarted() {
        AppLifecycleListener.super.appStarted();
        start();
    }

    public void  start(){
        BackgroundService service =
                ApplicationManager.getApplication().getService(BackgroundService.class);


        service.start();
    }
    Logger log = com.intellij.openapi.diagnostic.Logger.getInstance(ApplicationListener.class);
}
