package com.ssk.backgroud.action;


import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.ssk.backgroud.ui.BackgroundSelect;


public class BackgroudSettingAction extends DumbAwareAction {

    Logger log = com.intellij.openapi.diagnostic.Logger.getInstance(BackgroudSettingAction.class);
    @Override
    public void actionPerformed(AnActionEvent e) {
        ShowSettingsUtil.getInstance().showSettingsDialog(e.getProject(), BackgroundSelect.class);

    }


}
