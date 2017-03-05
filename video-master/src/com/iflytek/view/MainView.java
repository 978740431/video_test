package com.iflytek.view;

/* The Java Version MSC Project
 * All rights reserved.
 *
 * Licensed under the Iflytek License, Version 2.0.1008.1034 (the "License");
 * you may not use this file except in compliance with the License(appId).
 * You may obtain an AppId of this application at
 *
 *      http://www.voiceclouds.cn
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Document we provide for details.
 */

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.iflytek.cloud.speech.SpeechUtility;
import com.iflytek.util.Version;

/**
 * MscDemo It's a Sample using MSC SDK, include tts, isr. you can just press
 * button to use it.
 *
 * @author cyhu 2012-06-14
 */
@SuppressWarnings("serial")
public class MainView extends JFrame implements ActionListener {
    private JPanel mContentPanel;
    /**
     * 界面初始化.
     */
    public MainView() {
        // 初始化
        StringBuffer param = new StringBuffer();
        param.append("appid=" + Version.getAppid());
        SpeechUtility.createUtility(param.toString());
        // 设置界面大小，背景图片，就是整个GUI面板
        ImageIcon background = new ImageIcon("res/index_bg.png");
        JLabel label = new JLabel(background);
        label.setBounds(0, 0, background.getIconWidth(),
                background.getIconHeight());
        getLayeredPane().add(label, new Integer(Integer.MIN_VALUE));

        int frameWidth = background.getIconWidth();
        int frameHeight = background.getIconHeight();

        setSize(frameWidth, frameHeight);
        setResizable(false);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //创建语音识别面板
        mContentPanel = new JPanel(new BorderLayout());
        mContentPanel.setOpaque(false);
        mContentPanel.add(new AsrSpeechView());

        setLocationRelativeTo(null);
        setContentPane(mContentPanel);
        setVisible(true);
    }

    /**
     * Demo入口函数.
     *
     * @param args
     */
    public static void main(String args[]) {
        new MainView();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //创建语音识别的界面，就是那个有上传和开始识别按钮的界面
        mContentPanel.add(new AsrSpeechView());
        mContentPanel.revalidate();
        mContentPanel.repaint();
    }


}