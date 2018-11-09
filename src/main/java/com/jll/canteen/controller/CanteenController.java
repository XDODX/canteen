package com.jll.canteen.controller;

import com.jll.canteen.model.OrderModel;
import com.jll.canteen.service.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * ┏━━━━━━━━━Orz━━━━━━━━━┓
 *
 * @author wanghui
 * @date 2018/2/28
 * @e-mail 450193027@qq.com
 * @description doc：
 * ┗━━━━━━━━━Orz━━━━━━━━━┛
 */
@RestController
public class CanteenController {

    public static final Logger LOGGER = LoggerFactory.getLogger(CanteenController.class);

    @Autowired
    private QueueService queueService;

    @GetMapping("hurry")
    public Object hurry(String data) {
        LOGGER.info("点菜:{}", data);
        queueService.addOrder(data);
        return "0";
    }

    @GetMapping("remove")
    public Object remove(OrderModel orderModel) {
        LOGGER.info("移除:{}", orderModel.getCallNo());
        queueService.remove(orderModel);
        return "0";
    }

    @GetMapping("bind")
    public Object bind(OrderModel orderModel) {
        LOGGER.info("绑定:{}", orderModel.getCallNo());
        queueService.bind(orderModel);
        return "0";
    }

    @GetMapping("changeStatus")
    public Object changeStatus(String pot) {
        queueService.changeStatus(pot);
        return "0";
    }

    @GetMapping("queue")
    public Object queue() {
        return queueService.getQueue();
    }

    @GetMapping("monitor")
    public Object getIpList() {
        return queueService.getIpList();
    }

    @GetMapping("background")
    public Object getImgUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + queueService.getImgUrl();
    }

    @GetMapping("clear")
    public Object clear() {
        queueService.clear();
        return "0";
    }

    @GetMapping("menu")
    public Object getMenu() {
        return queueService.getMenu();
    }

    @GetMapping("change")
    public Object forceChange(OrderModel orderModel) {
        LOGGER.info("修改状态：{}", orderModel.getId());
        queueService.forceChange(orderModel);
        return "0";
    }

    @PostMapping("close")
    public Object closeChrome() throws IOException {
        queueService.closeChrome();
        return "0";
    }
}
