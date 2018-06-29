package com.jll.canteen.util;


import com.jll.canteen.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LEDControl {


    private static Config config;

    public Config getConfig() {
        return config;
    }

    @Autowired
    public void setConfig(Config config) {
        LEDControl.config = config;
    }

    static {
//        System.loadLibrary("ListenPlayDll");
    }

    public native static int AddProgram(int pno, int jno, int playTime);

    public native static int AddControl(int pno, int DBColor);

    public native static int SetSerialPortPara(int pno, int comno, int baud);

    public native static int AddLnTxtArea(int pno, int jno, int qno, int left, int top, int width, int height, String LnFileName, int PlayStyle, int Playspeed,
                                          int times);

    public native static int AddFileArea(int pno, int jno, int qno, int left, int top, int width, int height);

    public native static int AddFile(int pno, int jno, int qno, int mno, String fileName, int width, int height, int playstyle, int QuitStyle, int playspeed,
                                     int delay, int MidText);

    // 计时
    public native static int AddTimerArea(int pno, int jno, int qno, int left, int top, int width, int height, int fontColor, String fontName, int fontSize,
                                          int fontBold, int mode, int format, int year, int week, int month, int day, int hour, int minute, int second);

    // 数字时钟
    public native static int AddDClockArea(int pno, int jno, int qno, int left, int top, int width, int height, int fontColor, String fontName, int fontSize,
                                           int fontBold, int mode, int format, int spanMode, int hour, int minute);

    public native static int SetNetPara();

    public native static int SendControl(int PNO, int SendType, int hwd);

    public native static int SetOrderPara(int pno, String diskName);

    public native static int AddFileString(int pno, int jno, int qno, int mno, String str, String fontname, int fontsize, int fontcolor, boolean bold,
                                           boolean italic, boolean underline, int align, int width, int height, int playstyle, int QuitStyle, int playspeed, int delay, int MidText);

    public native static int SetTransMode(int TransMode, int ConType);

    public native static int SetNetworkPara(int pno, String ip);

    public native static void StartSend();

    public native static int SetProgramTimer(int pno, int jno, int TimingModel, int WeekSelect, int startSecond, int startMinute, int startHour, int startDay,
                                             int startMonth, int startWeek, int startYear, int endSecond, int endMinute, int endHour, int endDay, int endMonth, int endWeek, int endYear);

    public native static int AddLnTxtString(int pno, int jno, int qno, int left, int top, int width, int height, String text, String fontname, int fontsize,
                                            int fontcolor, boolean bold, boolean italic, boolean underline, int PlayStyle, int Playspeed, int times);

    public native static int AddQuitText(int pno, int jno, int qno, int left, int top, int width, int height, int FontColor, String fontName, int fontSize,
                                         boolean b, boolean c, boolean d, String text);

    public native static int AddDClockArea(int pno, int jno, int qno, int left, int top, int width, int height, int fontColor, String fontName, int fontSize,
                                           int fontBold, int Italic, int Underline, int year, int week, int month, int day, int hour, int minute, int second, int TwoOrFourYear, int HourShow,
                                           int format, int spanMode, int Advacehour, int Advaceminute);

    public native static int SetTest(int pno, int value);

    public native static int AdjustTime(int PNO);

    public native static int SetPower(int PNO, int power);

    public native static int SetHardPara(int PNO, int Sign, int Mirror, int ScanStyle, int LineOrder, int cls, int RGChange, int zhangKong);

    public native static int GetHardPara(int PNO, String FilePath);

    public native static int SearchController(String filePath, String IPAddress);

    public native static int ComSearchController(int PNO, int ComNo, int BaudRate, String filePath);

    public native static int SetRemoteNetwork(int pno, String macAddress, String ip, String gateway, String subnetmask);

    public native static int SetPowerTimer(int pno, int bTimer, int startHour1, int startMinute1, int endHour1, int endMinute1, int startHour2,
                                           int startMinute2, int endHour2, int endMinute2, int startHour3, int startMinute3, int endHour3, int endMinute3);

    public native static int SetBrightnessTimer(int pno, int bTimer, int startHour1, int startMinute1, int endHour1, int endMinute1, int brightness1,
                                                int startHour2, int startMinute2, int endHour2, int endMinute2, int brightness2, int startHour3, int startMinute3, int endHour3, int endMinute3,
                                                int brightness3);

    public native static int SendScreenPara(int pno, int DBColor, int width, int height);

    public native static int SetTimingLimit(int pno, int FSecond, int FMinute, int FHour, int FDay, int FMonth, int FWeek, int FYear);

    public native static int CancelTimingLimit(int pno);

    public native static int GenerateFile(int PNO, String buffer);

    public static void SendMulTiText(String text) {
        SetTransMode(config.getTransMode(), config.getConType());
        SetSerialPortPara(config.getSerialPortPara_pno(), config.getSerialPortPara_comno(), config.getSerialPortPara_baud());
        StartSend();
        AddControl(config.getControl_pno(), config.getControl_DBColor());
        AddProgram(config.getProgram_pno(), config.getProgram_jno(), config.getProgram_playTime());
        AddFileArea(1, 1, 1, config.getLnTxt_left(), config.getLnTxt_top(), config.getLnTxt_width(), config.getLnTxt_height());
        AddFileString(config.getLnTxt_pno(), config.getLnTxt_jno(), config.getLnTxt_qno(), 1, text, config.getLnTxt_fontname(),
                config.getLnTxt_fontsize(), config.getLnTxt_fontcolor(), config.isLnTxt_bold(), config.isLnTxt_italic(), config.isLnTxt_underline(), 1,
                config.getLnTxt_width(), config.getLnTxt_height(), 5, 255, config.getLnTxt_Playspeed(), 3, 1);
        SendControl(config.getSendControl_pno(), config.getSendControl_SendType(), config.getSendControl_hwd());
    }

}