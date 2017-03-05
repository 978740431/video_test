package com.iflytek.view;

import com.iflytek.cloud.speech.*;
import com.iflytek.util.DebugLog;
import com.iflytek.util.DrawableUtils;
import com.iflytek.util.JsonParser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

public class AsrSpeechView extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L;

    // 语法文件
    private final static String mGrammar = "#ABNF 1.0 UTF-8;\nlanguage zh-CN;\nmode voice;\nroot $main;\n$main =$place1 $place2 $place3 ;\n$place1 = 夏天;\n$place2 = 开 | 关;\n$place3 = 客厅灯 | 卧室灯 | 卧室门 | 厨房灯 | 花洒 | 空调;";

    private JButton jbtnRecognizer;
    private JLabel labelWav;
    private JTextArea resultArea;

    private String mGrammarId = null;
    private SpeechRecognizer mAsr = null;
    ServerSocket serverSocket = null;

    /**
     * 初始化按钮. 初始化按钮图片背景、大小、鼠标点击事件
     */
    public AsrSpeechView() {
        ImageIcon img = new ImageIcon("res/mic_01.png");
        labelWav = new JLabel(img);
        labelWav.setBounds(0, 0, img.getIconWidth(),
                img.getIconHeight() * 4 / 5);

        jbtnRecognizer = addButton("res/button.png", "开始识别", 0, 320, 330, -1,
                "res/button");
        jbtnRecognizer.add(labelWav, BorderLayout.WEST);
        jbtnRecognizer.setEnabled(false);

        resultArea = new JTextArea("");
        resultArea.setBounds(30, 100, 540, 400);
        resultArea.setOpaque(false);
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setForeground(Color.BLACK);
        Font font = new Font("宋体", Font.BOLD, 21);
        resultArea.setFont(font);
        resultArea.append(mGrammar);

        setOpaque(false);
        setLayout(null);
        add(jbtnRecognizer);
        add(resultArea);

        // 初始化识别对象
        mAsr = SpeechRecognizer.createRecognizer();

        jbtnRecognizer.addActionListener(this);

        //构建语法树
        uploadUuildGrammar();

        try {
            Thread.sleep(2000);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //开始接收说的话
        startAcceptContent();
    }

    public JButton addButton(String imgName, String btnName, int x, int y,
                             int imgWidth, int imgHeight, String iconPath) {

        JButton btn;
        ImageIcon img = new ImageIcon(imgName);
        btn = DrawableUtils.createImageButton(btnName, img, "center");
        int width = imgWidth, height = imgHeight;
        if (width == 1)
            width = img.getIconWidth();
        else if (width == -1)
            width = img.getIconHeight() * 4 / 5;

        if (height == 1)
            height = img.getIconWidth();
        else if (height == -1)
            height = img.getIconHeight() * 4 / 5;

        btn.setBounds(x, y, width, height);

        DrawableUtils.setMouseListener(btn, iconPath);

        return btn;
    }

    private void uploadUuildGrammar(){
        //自己实现的
        //上传语法
        // 指定引擎类型
        mAsr.setParameter(SpeechConstant.ENGINE_TYPE, "cloud");
        mAsr.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
        int ret = mAsr.buildGrammar("abnf", mGrammar, grammarListener);
        if (ret != ErrorCode.SUCCESS)
            DebugLog.Log("语法构建失败,错误码：" + ret);
    }


    private void startAcceptContent(){
        //自己实现的，开始监听
        resultArea.setText("");
        // 语法识别步骤：1、上传语法成功文件，2、语法识别。
        //语音识别
        if (mGrammarId != null) {
            // 设置云端返回结果为json格式（默认返回josn格式，可设置为xml）
            mAsr.setParameter(SpeechConstant.RESULT_TYPE, "json");
            mAsr.setParameter(SpeechConstant.ASR_AUDIO_PATH, "./iflytek_asr.pcm");
            // 设置云端识别使用的语法id
            mAsr.setParameter(SpeechConstant.CLOUD_GRAMMAR, mGrammarId);
            if (!mAsr.isListening())
                mAsr.startListening(recognizerListener);
            else {
                mAsr.stopListening();
                asrSpeechInitUI();
            }
        }
        //开始监听后创建socket，用来和esp8266交互
        createSocketSever();
    }

    /**
     * 按钮监听器实现
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        /*resultArea.setText("");
        // 语法识别步骤：1、上传语法成功文件，2、语法识别。
            //语音识别
            if (mGrammarId != null) {
                // 设置云端返回结果为json格式（默认返回josn格式，可设置为xml）
                mAsr.setParameter(SpeechConstant.RESULT_TYPE, "json");
                mAsr.setParameter(SpeechConstant.ASR_AUDIO_PATH, "./iflytek_asr.pcm");
                // 设置云端识别使用的语法id
                mAsr.setParameter(SpeechConstant.CLOUD_GRAMMAR, mGrammarId);
                if (!mAsr.isListening())
                    mAsr.startListening(recognizerListener);
                else {
                    mAsr.stopListening();
                    asrSpeechInitUI();
                }
            }*/


//        createSocketSever();

    }

    private void createSocketSever() {
        try {
            if (serverSocket == null) {
                serverSocket = new ServerSocket(8067);
                System.out.println("服务端启动成功,端口:8067");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 构建语法监听器
     */
    private GrammarListener grammarListener = new GrammarListener() {
        @Override
        public void onBuildFinish(String grammarId, SpeechError error) {
            if (error == null) {
                mGrammarId = grammarId;
                resultArea.setText("语法构建成功");
                jbtnRecognizer.setEnabled(true);
                DebugLog.Log("语法构建成功：" + grammarId);
            } else {
                DebugLog.Log("语法构建失败,错误码：" + error.getErrorCode());
                resultArea.setText("语法构建失败,错误码：" + error.getErrorDescription(true));
            }
        }
    };

    private RecognizerListener recognizerListener = new RecognizerListener() {

        /**
         * 获取识别结果. 获取RecognizerResult类型的识别结果，并对结果进行累加，显示到Area里
         */
        @Override
        public void onResult(RecognizerResult results, boolean islast) {
            // 结果返回为默认json格式,用JsonParser工具类解析
            String text = JsonParser.parseGrammarResult(results
                    .getResultString());
            resultArea.append(text);
            System.out.println("语音识别结果:"+text);
            Socket accept = null;
            try {
                accept = serverSocket.accept();
                SocketAddress remoteSocketAddress = accept.getRemoteSocketAddress();
                System.out.println("有一个客户端接入:" + remoteSocketAddress);
                OutputStream outputStream = accept.getOutputStream();
                String[] split = text.split("\n");
                String first = "";
                int index = 1;
                for (String s : split) {
                    System.out.println("分解后的结果:" + s);
                    if (index == 1) {
                        first = s;
                    }
                    index++;
                }
                if (first.contains("开")) {
                    outputStream.write("b".getBytes());
                    System.out.println("输出到客户端成功:输出内容为b,打开");
                } else if (first.contains("关")) {
                    outputStream.write("a".getBytes());
                    System.out.println("输出到客户端成功:输出内容为a,关闭");
                }


            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    accept.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (islast) {
                asrSpeechInitUI();
            }

        }

        @Override
        public void onVolumeChanged(int volume) {
            if (volume == 0)
                volume = 1;
            else if (volume >= 6)
                volume = 6;
            labelWav.setIcon(new ImageIcon("res/mic_0" + volume + ".png"));
        }

        @Override
        public void onError(SpeechError error) {
            if (null != error) {
                DebugLog.Log("onError Code：" + error.getErrorCode());
                AsrSpeechView.this.resultArea.setText(error.getErrorDescription(true));
            }
            asrSpeechInitUI();
        }

        @Override
        public void onEvent(int eventType, int arg1, int agr2, String msg) {

        }

        @Override
        public void onBeginOfSpeech() {
            ((JLabel) jbtnRecognizer.getComponent(0)).setText("请说话...");
        }

        @Override
        public void onEndOfSpeech() {
            ((JLabel) jbtnRecognizer.getComponent(0)).setText("等待结果");
        }
    };

    /**
     * 识别结束，恢复初始状态
     * 改成直接调用请说话
     */
    public void asrSpeechInitUI() {
        startAcceptContent();
        /*labelWav.setIcon(new ImageIcon("res/mic_01.png"));
        ((JLabel) jbtnRecognizer.getComponent(0)).setText("开始识别");*/
    }
}
