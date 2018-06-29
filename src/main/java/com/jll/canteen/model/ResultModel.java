package com.jll.canteen.model;

import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.List;

/**
 * ┏━━━━━━━━━Orz━━━━━━━━━┓
 *
 * @author wanghui
 * @date 2018/4/3
 * @e-mail 450193027@qq.com
 * @description doc：
 * ┗━━━━━━━━━Orz━━━━━━━━━┛
 */
public class ResultModel implements Serializable {

    private static final long serialVersionUID = 2790640458438566118L;

    public ResultModel() {
        this.leftDish = Lists.newArrayList();
        this.cooking = Lists.newArrayList();
        this.waiting = Lists.newArrayList();
        this.ready = Lists.newArrayList();
    }

    private List<LeftDish> leftDish;
    private List<CookPot> cooking;
    private List<WaitQueue> waiting;
    private List<OrderModel> ready;

    public List<LeftDish> getLeftDish() {
        return leftDish;
    }

    public void setLeftDish(List<LeftDish> leftDish) {
        this.leftDish = leftDish;
    }

    public List<CookPot> getCooking() {
        return cooking;
    }

    public void setCooking(List<CookPot> cooking) {
        this.cooking = cooking;
    }

    public List<WaitQueue> getWaiting() {
        return waiting;
    }

    public void setWaiting(List<WaitQueue> waiting) {
        this.waiting = waiting;
    }

    public List<OrderModel> getReady() {
        return ready;
    }

    public void setReady(List<OrderModel> ready) {
        this.ready = ready;
    }

    public static class LeftDish implements Serializable {

        private static final long serialVersionUID = 1019701665376440095L;

        private String name;
        private int count;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    public static class CookPot implements Serializable {

        private static final long serialVersionUID = 7492787740517694454L;

        public CookPot() {
            waitNo = Lists.newArrayList();
        }

        private String pot;
        private String name;
        private List<OrderModel> waitNo;

        public String getPot() {
            return pot;
        }

        public void setPot(String pot) {
            this.pot = pot;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<OrderModel> getWaitNo() {
            return waitNo;
        }

        public void setWaitNo(List<OrderModel> waitNo) {
            this.waitNo = waitNo;
        }
    }

    public static class WaitQueue implements Serializable {

        private static final long serialVersionUID = -4601023908724846947L;

        public WaitQueue() {
            waitNo = Lists.newArrayList();
        }

        private int size;
        private int sortNo;
        private String name;
        private List<OrderModel> waitNo;

        public int getSortNo() {
            return sortNo;
        }

        public void setSortNo(int sortNo) {
            this.sortNo = sortNo;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<OrderModel> getWaitNo() {
            return waitNo;
        }

        public void setWaitNo(List<OrderModel> waitNo) {
            this.waitNo = waitNo;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }
    }
}
