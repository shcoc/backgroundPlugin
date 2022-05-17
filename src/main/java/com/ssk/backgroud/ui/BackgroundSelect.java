/*
 * Created by JFormDesigner on Wed Jul 28 21:55:50 CST 2021
 */

package com.ssk.backgroud.ui;

import java.awt.event.*;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.*;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.components.*;
import com.ssk.backgroud.ImageUtil;
import com.ssk.backgroud.config.Form;
import com.ssk.backgroud.enumbean.TimeEnum;
import com.ssk.backgroud.config.BackgroundConfig;
import com.ssk.backgroud.service.BackgroundService;
import org.apache.commons.io.FileExistsException;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * @author unknown
 */
public class BackgroundSelect extends JPanel implements Configurable {

    Logger log = com.intellij.openapi.diagnostic.Logger.getInstance(BackgroundSelect.class);

    public BackgroundSelect() throws FileExistsException {
        initComponents();
       // removePropertyChangeListener(getPropertyChangeListeners()[0]);
        setBorder(BorderFactory.createEmptyBorder());
        init();
        setVisible(true);
    }

    private void init() throws FileExistsException {
        final FileChooserDescriptor chooserDescriptor =
                FileChooserDescriptorFactory.createSingleFolderDescriptor();
        chooserDescriptor.setTitle("backgroud dir");
        path.addBrowseFolderListener(new TextBrowseFolderListener(chooserDescriptor) {
            @Override
            public void run() {
                    super.run();
                log.info(path.getText());
            }
        });
        number.setModel(new SpinnerNumberModel(0, 0, 60, 1));
        DefaultComboBoxModel<TimeEnum> comboBoxModel = new DefaultComboBoxModel<TimeEnum>();
        comboBoxModel.addElement(TimeEnum.SECONDS);
        comboBoxModel.addElement(TimeEnum.MINUTE);
        comboBoxModel.addElement(TimeEnum.HOURS);
        unit.setModel(comboBoxModel);
        props =  PropertiesComponent.getInstance();
        //---- image ----
        image = new ButtonGroup();
        image.add(network);
        image.add(cache);
        image.add(this.local);

        //---- windown ----
        windown = new ButtonGroup();
        windown.add(frame);
        windown.add(edit);
        windown.add(all);

        File file = new File(System.getProperty("user.home"),".ideaBackground");
        if (file.exists()){
            if (!file.isDirectory()){
                log.error(file.getAbsolutePath() + " not  is directory");
                throw new FileExistsException(file.getAbsolutePath() + " Not a directory");
            }
        }else {
            file.mkdirs();
        }
        local.addChangeListener(new ChangeListener(){
            @Override
            public void stateChanged(ChangeEvent e) {
                if (local.isSelected()){
                    selectLabel.setText("文件夹");
                    path.setEnabled(true);
                    bulideCache.setEnabled(true);
                }
            }
        });
        cache.addChangeListener(new ChangeListener(){
            @Override
            public void stateChanged(ChangeEvent e) {
                if (cache.isSelected()){
                    selectLabel.setText("文件夹");
                    path.setEnabled(false);
                    bulideCache.setEnabled(false);
                }
            }
        });
        network.addChangeListener(new ChangeListener(){
            @Override
            public void stateChanged(ChangeEvent e) {
                if (network.isSelected()){
                    selectLabel.setText("数据库地址");
                    path.setEnabled(true);
                    bulideCache.setEnabled(false);
                }
            }
        });
        network.setText(Form.NETWORK);
        cache.setText(Form.CACHE);
        local.setText(Form.LOCAL);

        frame.setText(Form.FRAME);
        edit.setText(Form.EDIT);
        all.setText(Form.ALL);

        opacity.setModel(
                new DefaultBoundedRangeModel(0,5,0,100)
        );
        opacityNum.setText(opacity.getValue()+"");
        opacity.addChangeListener(new ChangeListener(){
            @Override
            public void stateChanged(ChangeEvent e) {
               if (e.getSource() instanceof JSlider){
                   JSlider jSlider = (JSlider)e.getSource();
                   opacityNum.setText(jSlider.getValue()+"");
               }
            }
        });
        
    }

    private PropertiesComponent props;
    private ButtonGroup image;
    private ButtonGroup windown;
    
    
    
    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Background+";
    }

    @Override
    public @Nullable JComponent createComponent() {
        reset();
        return this;
    }

    @Override
    public boolean isModified() {
        String url = props.getValue(BackgroundConfig.FILE_PATH);
        String local = props.getValue(BackgroundConfig.LOCATION);
        String circle = props.getValue(BackgroundConfig.CIRCULATION_METHOD);
        String timeEnum = props.getValue(BackgroundConfig.TIME);
        String area = props.getValue(BackgroundConfig.AREA);
        String opacity =props.getValue(BackgroundConfig.OPACITY) == null ? "100":props.getValue(BackgroundConfig.OPACITY) ;
        return !(path.getText().equals(url) && getUrlLocation().equals(local) &&
                unit.getSelectedItem().toString().equals(circle) &&
                number.getValue().toString().equals(timeEnum) &&
                getArea().equals(area) && (Integer.valueOf(opacity).intValue() == this.opacity.getValue()));
    }

    @Override
    public void apply() throws ConfigurationException {
        String url = path.getText();
        String local = getUrlLocation();
        String circle = unit.getSelectedItem().toString();
        String timeEnum = number.getValue().toString();
        String area = getArea();
        String opacity = this.opacity.getValue() + "";
        props.setValue(BackgroundConfig.FILE_PATH,url);
        props.setValue(BackgroundConfig.AREA,area);
        props.setValue(BackgroundConfig.TIME,timeEnum);
        props.setValue(BackgroundConfig.LOCATION,local);
        props.setValue(BackgroundConfig.CIRCULATION_METHOD,circle);
        props.setValue(BackgroundConfig.OPACITY,opacity);
        ImageUtil.resetDatabase();
        BackgroundService service =
                ApplicationManager.getApplication().getService(BackgroundService.class);
        service.restart();
    }
    private String getUrlLocation(){
        if (local.isSelected()) return local.getText();
        else if (cache.isSelected()) return cache.getText();
        else if (network.isSelected()) return network.getText();
        else  return  local.getText();
    }
    private String getArea(){
        if (edit.isSelected()) return edit.getText();
        else if (frame.isSelected()) return frame.getText();
        else  return  all.getText();
    }
    @Override
    public void reset() {
        String url = props.getValue(BackgroundConfig.FILE_PATH);
        String local = props.getValue(BackgroundConfig.LOCATION);
        TimeEnum circle = TimeEnum.HOURS.getTime(props.getValue(BackgroundConfig.CIRCULATION_METHOD));
        String timeEnum = props.getValue(BackgroundConfig.TIME);
        String area = props.getValue(BackgroundConfig.AREA);
        String opcity = props.getValue(BackgroundConfig.OPACITY);
        path.setText(url==null?"":url);
        setImage(local);
        setWindown(area);
        number.setValue(Integer.valueOf(timeEnum == null?"0":timeEnum));
        unit.setSelectedItem(circle==null? TimeEnum.MINUTE:circle);
        opacity.setValue(Integer.valueOf(opcity == null?"100":opcity));
        
        //TODO 数据库部分
  //      network.setEnabled(false);
//       cache.setEnabled(false);
//       clearCache.setEnabled(false);
//       bulideCache.setEnabled(false);
        
    }

    private void  setImage(String image){

        this.image.clearSelection();
        if (local.getText().equals(image)) local.setSelected(true);
        else if (cache.getText().equals(image)) cache.setSelected(true);
        else if (network.getText().equals(image)) network.setSelected(true);
        else local.setSelected(true);
    }
    private void  setWindown(String windown){
        this.windown.clearSelection();
        if (edit.getText().equals(windown)) edit.setSelected(true);
        else if (frame.getText().equals(windown))  frame.setSelected(true);
        else  all.setSelected(true);
    }
    @Override
    public void disposeUIResources() {
        Configurable.super.disposeUIResources();
    }

    private void bulideCacheActionPerformed(ActionEvent e) {
        // TODO add your code here
        ImageUtil.buildDb();
    }

    private void clearCacheActionPerformed(ActionEvent e) {
        // TODO add your code here
        ImageUtil.deleteDatabase();
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JLabel title;
    private TextFieldWithBrowseButton path;
    private JBLabel selectLabel;
    private JBLabel location;
    private JPanel bPanelWithEmptyText1;
    private JBRadioButton network;
    private JBRadioButton cache;
    private JBRadioButton local;
    private JBLabel bLabel1;
    private JPanel panel1;
    private JSpinner number;
    private ComboBox unit;
    private JBLabel bLabel2;
    private JPanel panel2;
    private JBRadioButton frame;
    private JBRadioButton edit;
    private JBRadioButton all;
    private JBLabel bLabel3;
    private JButton clearCache;
    private JButton bulideCache;
    private JBLabel bLabel4;
    private JBLabel bLabel5;
    private JBLabel bLabel6;
    private JBLabel bLabel7;
    private JSlider opacity;
    private JBLabel opacityNum;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        title = new JLabel();
        path = new TextFieldWithBrowseButton();
        selectLabel = new JBLabel();
        location = new JBLabel();
        bPanelWithEmptyText1 = new JPanel();
        network = new JBRadioButton();
        cache = new JBRadioButton();
        local = new JBRadioButton();
        bLabel1 = new JBLabel();
        panel1 = new JPanel();
        number = new JSpinner();
        unit = new ComboBox();
        bLabel2 = new JBLabel();
        panel2 = new JPanel();
        frame = new JBRadioButton();
        edit = new JBRadioButton();
        all = new JBRadioButton();
        bLabel3 = new JBLabel();
        clearCache = new JButton();
        bulideCache = new JButton();
        bLabel4 = new JBLabel();
        bLabel5 = new JBLabel();
        bLabel6 = new JBLabel();
        bLabel7 = new JBLabel();
        opacity = new JSlider();
        opacityNum = new JBLabel();

        //======== this ========
        setBorder(null);
        setLayout(null);

        //---- title ----
        title.setText("\u80cc\u666f\u56fe\u7247\u6587\u4ef6\u9009\u62e9");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(title.getFont().getSize() + 2f));
        add(title);
        title.setBounds(0, 5, 400, 30);
        add(path);
        path.setBounds(100, 45, 275, 35);

        //---- selectLabel ----
        selectLabel.setText("\u6587\u4ef6\u5939");
        selectLabel.setLabelFor(path);
        selectLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(selectLabel);
        selectLabel.setBounds(30, 45, 70, 35);

        //---- location ----
        location.setText("\u56fe\u7247\u4f4d\u7f6e");
        add(location);
        location.setBounds(40, 92, 50, 20);

        //======== bPanelWithEmptyText1 ========
        {
            bPanelWithEmptyText1.setLayout(null);

            //---- network ----
            network.setText("\u7f51\u7edc(\u6570\u636e\u5e93)");
            bPanelWithEmptyText1.add(network);
            network.setBounds(new Rectangle(new Point(170, 0), network.getPreferredSize()));

            //---- cache ----
            cache.setText("\u7f13\u5b58(\u6570\u636e\u5e93)");
            bPanelWithEmptyText1.add(cache);
            cache.setBounds(70, 0, 100, 28);

            //---- local ----
            local.setText("\u672c\u5730");
            bPanelWithEmptyText1.add(local);
            local.setBounds(new Rectangle(new Point(5, 0), local.getPreferredSize()));
        }
        add(bPanelWithEmptyText1);
        bPanelWithEmptyText1.setBounds(100, 90, 270, 25);

        //---- bLabel1 ----
        bLabel1.setText("\u5faa\u73af\u65b9\u5f0f");
        add(bLabel1);
        bLabel1.setBounds(40, 130, 65, 30);

        //======== panel1 ========
        {
            panel1.setLayout(null);
            panel1.add(number);
            number.setBounds(0, 0, 100, 35);

            //---- unit ----
            unit.setFont(unit.getFont().deriveFont(unit.getFont().getSize() + 5f));
            unit.setModel(new DefaultComboBoxModel<>(new String[] {
                "\u6d4b\u8bd5"
            }));
            panel1.add(unit);
            unit.setBounds(110, 0, 100, 35);
        }
        add(panel1);
        panel1.setBounds(105, 130, 265, 35);

        //---- bLabel2 ----
        bLabel2.setText("\u56fe\u7247\u533a\u57df");
        add(bLabel2);
        bLabel2.setBounds(40, 185, 55, 25);

        //======== panel2 ========
        {
            panel2.setLayout(null);

            //---- frame ----
            frame.setText("\u7a97\u53e3");
            panel2.add(frame);
            frame.setBounds(0, 0, 60, 31);

            //---- edit ----
            edit.setText("\u7f16\u8f91\u5668");
            panel2.add(edit);
            edit.setBounds(65, 0, 75, 31);

            //---- all ----
            all.setText("\u5168\u90e8");
            panel2.add(all);
            all.setBounds(145, 0, 75, 30);
        }
        add(panel2);
        panel2.setBounds(105, 180, 275, 35);

        //---- bLabel3 ----
        bLabel3.setText("\u7f13\u5b58\u7ba1\u7406");
        add(bLabel3);
        bLabel3.setBounds(40, 265, bLabel3.getPreferredSize().width, 25);

        //---- clearCache ----
        clearCache.setText("\u6e05\u9664\u7f13\u5b58");
        clearCache.addActionListener(e -> clearCacheActionPerformed(e));
        add(clearCache);
        clearCache.setBounds(new Rectangle(new Point(105, 260), clearCache.getPreferredSize()));

        //---- bulideCache ----
        bulideCache.setText("\u5efa\u7acb\u7f13\u5b58\u6570\u636e\u5e93");
        bulideCache.addActionListener(e -> bulideCacheActionPerformed(e));
        add(bulideCache);
        bulideCache.setBounds(new Rectangle(new Point(205, 260), bulideCache.getPreferredSize()));

        //---- bLabel4 ----
        bLabel4.setText("\u63d0\u793a\uff1a");
        add(bLabel4);
        bLabel4.setBounds(40, 290, 50, 30);

        //---- bLabel5 ----
        bLabel5.setText("1. \u7f13\u5b58\u6570\u636e\u5e93\u9ed8\u8ba4\u4f4d\u7f6e\u4e3a");
        add(bLabel5);
        bLabel5.setBounds(new Rectangle(new Point(60, 325), bLabel5.getPreferredSize()));

        //---- bLabel6 ----
        bLabel6.setText("2. \u6570\u636e\u5e93\u683c\u5f0f");
        add(bLabel6);
        bLabel6.setBounds(new Rectangle(new Point(60, 350), bLabel6.getPreferredSize()));

        //---- bLabel7 ----
        bLabel7.setText("\u900f\u660e\u5ea6");
        add(bLabel7);
        bLabel7.setBounds(40, 220, 50, 30);

        //---- opacity ----
        opacity.setMajorTickSpacing(4);
        add(opacity);
        opacity.setBounds(105, 220, 200, 27);

        //---- opacityNum ----
        opacityNum.setText("100");
        add(opacityNum);
        opacityNum.setBounds(315, 220, 35, 25);

        setPreferredSize(new Dimension(405, 425));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }





}
