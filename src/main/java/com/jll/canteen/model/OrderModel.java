package com.jll.canteen.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * ┏━━━━━━━━━Orz━━━━━━━━━┓
 *
 * @author wanghui
 * @date 2018/4/3
 * @e-mail 450193027@qq.com
 * @description doc：
 * ┗━━━━━━━━━Orz━━━━━━━━━┛
 */
public class OrderModel implements Serializable {

    private static final long serialVersionUID = -9120563019375551741L;

    public OrderModel() {
        this.id = UUID.randomUUID().toString().replaceAll("-", "");
    }

    public OrderModel(String orderNo, Integer callNo) {
        this.id = UUID.randomUUID().toString().replaceAll("-", "");
        this.orderNo = orderNo;
        this.callNo = callNo;
    }

    public OrderModel(String orderNo, String dishName) {
        this.id = UUID.randomUUID().toString().replaceAll("-", "");
        this.orderNo = orderNo;
        this.cooking = true;
        this.dishName = dishName;
    }

    public OrderModel(String orderNo, boolean cooking, String dishName) {
        this.id = UUID.randomUUID().toString().replaceAll("-", "");
        this.orderNo = orderNo;
        this.cooking = cooking;
        this.dishName = dishName;
    }

    private String id;
    private String orderNo;
    private Integer callNo;
    private int state = 0;
    @JsonIgnore
    private boolean cooking = false;
    private String dishName;
    @JsonIgnore
    private int dishOrder;
    @JsonIgnore
    private Date createTime = new Date();
    private String time;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Integer getCallNo() {
        return callNo;
    }

    public void setCallNo(Integer callNo) {
        this.callNo = callNo;
    }

    public boolean isCooking() {
        return cooking;
    }

    public void setCooking(boolean cooking) {
        this.cooking = cooking;
    }

    public String getDishName() {
        return dishName;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
    }

    public int getDishOrder() {
        return dishOrder;
    }

    public void setDishOrder(int dishOrder) {
        this.dishOrder = dishOrder;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getTime() {
        if (StringUtils.isEmpty(time)) {
            long waitMillis = new Date().getTime() - createTime.getTime();
            long minutes = waitMillis / 1000 / 60;
            long seconds = (waitMillis / 1000) % 60;
            return String.format("%s分%s秒", minutes > 9 ? minutes : "0" + minutes, seconds > 9 ? seconds : "0" + seconds);
        } else {
            return time;
        }
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
