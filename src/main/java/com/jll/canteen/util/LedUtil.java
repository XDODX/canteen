package com.jll.canteen.util;


import com.jll.canteen.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class LedUtil {

    private static Config config;

    public Config getConfig() {
        return config;
    }

    @Autowired
    public void setConfig(Config config) {
        LedUtil.config = config;
    }

    private static final List<String> MESSAGES = new LinkedList<String>();

    private static long lastSendTime = 0;

    public static synchronized void addMessage(String message) {
        removeMessage(message);
        synchronized (MESSAGES) {
            MESSAGES.add(message);
        }
        try {
            showMessageToLED();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void removeMessage(String message) {
        synchronized (MESSAGES) {
            MESSAGES.remove(message);
        }
    }

    public static synchronized int showMessageToLED() throws Exception {
        String messageString = "";
        int autoLineNumber = config.getAutoLineNumber();
        synchronized (MESSAGES) {
            int i = 1;
            for (String str : MESSAGES) {
                messageString += str + " ";
                if (i++ % autoLineNumber == 0)
                    messageString = messageString.trim() + "\n";
            }
        }
        if (messageString.trim().length() == 0) {
            messageString = "暂无";
        }
        // 开启线程，调用LED
        new Thread() {
            private String msg = "error";

            public synchronized void start(String msg) {
                this.msg = msg;
                super.start();
            }

            ;

            @SuppressWarnings("unchecked")
            public void run() {
                if (System.currentTimeMillis() - lastSendTime < 1000) {
                    System.out.println("发送间隔太短..等待下次发送");
                    return;
                }
                System.out.println("发送LED:\r\n" + msg);
//                Class ledClass;
//                try {
//                    ledClass = Class.forName("LEDControl");
//                    Method sendContent = ledClass.getMethod("SendMulTiText", new Class[]{String.class});
//                    sendContent.invoke(ledClass.newInstance(), new Object[]{msg});
//                    lastSendTime = System.currentTimeMillis();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                LEDControl.SendMulTiText(msg);
                lastSendTime = System.currentTimeMillis();
            }
        }.start(messageString.trim());
        return 1;
    }
}
