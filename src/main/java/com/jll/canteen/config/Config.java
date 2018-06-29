package com.jll.canteen.config;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@ConfigurationProperties
@PropertySource("classpath:config.yml")
public class Config {


    private int autoLineNumber;

    // 1、网络通讯 2、232通讯 3、485通讯
    private int TransMode;
    // 控制器型号(1、N1,N2,L1 /2、N3 /3、T1,T2,T3,T4,Q0,Q1)
    private int ConType;
    // 设置串口参数
    // 屏号
    private int SerialPortPara_pno;
    // 端口号
    private int SerialPortPara_comno;
    // 波特率
    private int SerialPortPara_baud;

    // 添加显示屏
    // 屏幕号
    private int Control_pno;
    // 单双色(单色为1 ，双色为2,三基色3)
    private int Control_DBColor;

    // 添加节目
    // 屏幕号
    private int Program_pno;
    // 节目号
    private int Program_jno;
    // 播放时间
    private int Program_playTime;

    // 单行文本配置
    // 屏号 (>=1)
    private int LnTxt_pno;
    // 节目号 (>=1)
    private int LnTxt_jno;
    // 区域号 (>=1)
    private int LnTxt_qno;
    // 区域左上角顶点x坐标：8的倍数，单位：象素
    private int LnTxt_left;
    // 区域左上角顶点y坐标
    private int LnTxt_top;
    // 区域宽度
    private int LnTxt_width;
    // 区域高度
    private int LnTxt_height;
    // 字体名
    private String LnTxt_fontname;
    // 字体号
    private int LnTxt_fontsize;
    // 字体颜色 颜色的RGB值，如红色为 ：255
    private int LnTxt_fontcolor;
    // 是否粗体
    private boolean LnTxt_bold;
    // 是否斜体
    private boolean LnTxt_italic;
    // 是否下划线
    private boolean LnTxt_underline;
    // 显示特技（支持左移、右移、上移、下移）
    private int LnTxt_PlayStyle;
    // 显示速度
    private int LnTxt_Playspeed;
    // 保留参数（暂未使用）
    private int LnTxt_times;

    // 发送控制参数
    // 屏号
    private int SendControl_pno;
    // 发送模式1为普通 2为SD卡发送
    private int SendControl_SendType;
    // 窗口句柄
    private int SendControl_hwd;

    private int vendorId;

    private int productId;

    private String numberNameSplit;

    private String namesSplit;

    private String imgPath;

    private String ips;

    public int getAutoLineNumber() {
        return autoLineNumber;
    }

    public void setAutoLineNumber(int autoLineNumber) {
        this.autoLineNumber = autoLineNumber;
    }

    public int getTransMode() {
        return TransMode;
    }

    public void setTransMode(int transMode) {
        TransMode = transMode;
    }

    public int getConType() {
        return ConType;
    }

    public void setConType(int conType) {
        ConType = conType;
    }

    public int getSerialPortPara_pno() {
        return SerialPortPara_pno;
    }

    public void setSerialPortPara_pno(int serialPortPara_pno) {
        SerialPortPara_pno = serialPortPara_pno;
    }

    public int getSerialPortPara_comno() {
        return SerialPortPara_comno;
    }

    public void setSerialPortPara_comno(int serialPortPara_comno) {
        SerialPortPara_comno = serialPortPara_comno;
    }

    public int getSerialPortPara_baud() {
        return SerialPortPara_baud;
    }

    public void setSerialPortPara_baud(int serialPortPara_baud) {
        SerialPortPara_baud = serialPortPara_baud;
    }

    public int getControl_pno() {
        return Control_pno;
    }

    public void setControl_pno(int control_pno) {
        Control_pno = control_pno;
    }

    public int getControl_DBColor() {
        return Control_DBColor;
    }

    public void setControl_DBColor(int control_DBColor) {
        Control_DBColor = control_DBColor;
    }

    public int getProgram_pno() {
        return Program_pno;
    }

    public void setProgram_pno(int program_pno) {
        Program_pno = program_pno;
    }

    public int getProgram_jno() {
        return Program_jno;
    }

    public void setProgram_jno(int program_jno) {
        Program_jno = program_jno;
    }

    public int getProgram_playTime() {
        return Program_playTime;
    }

    public void setProgram_playTime(int program_playTime) {
        Program_playTime = program_playTime;
    }

    public int getLnTxt_pno() {
        return LnTxt_pno;
    }

    public void setLnTxt_pno(int lnTxt_pno) {
        LnTxt_pno = lnTxt_pno;
    }

    public int getLnTxt_jno() {
        return LnTxt_jno;
    }

    public void setLnTxt_jno(int lnTxt_jno) {
        LnTxt_jno = lnTxt_jno;
    }

    public int getLnTxt_qno() {
        return LnTxt_qno;
    }

    public void setLnTxt_qno(int lnTxt_qno) {
        LnTxt_qno = lnTxt_qno;
    }

    public int getLnTxt_left() {
        return LnTxt_left;
    }

    public void setLnTxt_left(int lnTxt_left) {
        LnTxt_left = lnTxt_left;
    }

    public int getLnTxt_top() {
        return LnTxt_top;
    }

    public void setLnTxt_top(int lnTxt_top) {
        LnTxt_top = lnTxt_top;
    }

    public int getLnTxt_width() {
        return LnTxt_width;
    }

    public void setLnTxt_width(int lnTxt_width) {
        LnTxt_width = lnTxt_width;
    }

    public int getLnTxt_height() {
        return LnTxt_height;
    }

    public void setLnTxt_height(int lnTxt_height) {
        LnTxt_height = lnTxt_height;
    }

    public String getLnTxt_fontname() {
        return LnTxt_fontname;
    }

    public void setLnTxt_fontname(String lnTxt_fontname) {
        LnTxt_fontname = lnTxt_fontname;
    }

    public int getLnTxt_fontsize() {
        return LnTxt_fontsize;
    }

    public void setLnTxt_fontsize(int lnTxt_fontsize) {
        LnTxt_fontsize = lnTxt_fontsize;
    }

    public int getLnTxt_fontcolor() {
        return LnTxt_fontcolor;
    }

    public void setLnTxt_fontcolor(int lnTxt_fontcolor) {
        LnTxt_fontcolor = lnTxt_fontcolor;
    }

    public boolean isLnTxt_bold() {
        return LnTxt_bold;
    }

    public void setLnTxt_bold(boolean lnTxt_bold) {
        LnTxt_bold = lnTxt_bold;
    }

    public boolean isLnTxt_italic() {
        return LnTxt_italic;
    }

    public void setLnTxt_italic(boolean lnTxt_italic) {
        LnTxt_italic = lnTxt_italic;
    }

    public boolean isLnTxt_underline() {
        return LnTxt_underline;
    }

    public void setLnTxt_underline(boolean lnTxt_underline) {
        LnTxt_underline = lnTxt_underline;
    }

    public int getLnTxt_PlayStyle() {
        return LnTxt_PlayStyle;
    }

    public void setLnTxt_PlayStyle(int lnTxt_PlayStyle) {
        LnTxt_PlayStyle = lnTxt_PlayStyle;
    }

    public int getLnTxt_Playspeed() {
        return LnTxt_Playspeed;
    }

    public void setLnTxt_Playspeed(int lnTxt_Playspeed) {
        LnTxt_Playspeed = lnTxt_Playspeed;
    }

    public int getLnTxt_times() {
        return LnTxt_times;
    }

    public void setLnTxt_times(int lnTxt_times) {
        LnTxt_times = lnTxt_times;
    }

    public int getSendControl_pno() {
        return SendControl_pno;
    }

    public void setSendControl_pno(int sendControl_pno) {
        SendControl_pno = sendControl_pno;
    }

    public int getSendControl_SendType() {
        return SendControl_SendType;
    }

    public void setSendControl_SendType(int sendControl_SendType) {
        SendControl_SendType = sendControl_SendType;
    }

    public int getSendControl_hwd() {
        return SendControl_hwd;
    }

    public void setSendControl_hwd(int sendControl_hwd) {
        SendControl_hwd = sendControl_hwd;
    }

    public int getVendorId() {
        return vendorId;
    }

    public void setVendorId(int vendorId) {
        this.vendorId = vendorId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getNumberNameSplit() {
        return numberNameSplit;
    }

    public void setNumberNameSplit(String numberNameSplit) {
        this.numberNameSplit = numberNameSplit;
    }

    public String getNamesSplit() {
        return namesSplit;
    }

    public void setNamesSplit(String namesSplit) {
        this.namesSplit = namesSplit;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public List<String> getIps() {
        return StringUtils.isEmpty(ips) ? Lists.newArrayList() : Arrays.asList(ips.split(","));
    }

    public void setIps(String ips) {
        this.ips = ips;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
