package com.jll.canteen.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jll.canteen.config.Config;
import com.jll.canteen.model.OrderModel;
import com.jll.canteen.model.ResultModel;
import com.jll.canteen.util.LedUtil;
import com.jll.canteen.util.USBControl;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * ┏━━━━━━━━━Orz━━━━━━━━━┓
 *
 * @author wanghui
 * @date 2018/4/3
 * @e-mail 450193027@qq.com
 * @description doc：
 * ┗━━━━━━━━━Orz━━━━━━━━━┛
 */
@Service
public class QueueService {

    @Autowired
    private Config config;

    public static boolean isWindows = false;

    static {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            isWindows = true;
        }
    }

    private static Map<String, Object> BACKUP_MAP = Maps.newConcurrentMap();
    private static final String POT_A_NAME = "A";
    private static final String POT_B_NAME = "B";
    // 菜品序号
    public static int order = 0;
    // A锅正在炒的队列
    public static final Map<String, List<OrderModel>> COOKING_A_MAP = Maps.newHashMap();
    // B锅正在炒的队列
    public static final Map<String, List<OrderModel>> COOKING_B_MAP = Maps.newHashMap();
    // 还未炒的队列
    public static final List<OrderModel> WAITING_LIST = Lists.newArrayList();
    //    // 剩菜
//    public static final Map<String, Integer> LEFT_DISH_MAP = Maps.newHashMap();
    // 等待取餐
    public static final List<OrderModel> READY_LIST = Lists.newArrayList();
    // 锁
    private Lock lock = new ReentrantLock();

    @PostConstruct
    public void init() throws IOException, ClassNotFoundException {
        recovery();
        File ipFile = new File(getBasePath() + "ip.txt");
        if (!ipFile.exists()) {
            ipFile.createNewFile();
        }
        File menuFile = new File(getBasePath() + "menu.txt");
        if (!menuFile.exists()) {
            menuFile.createNewFile();
        }
//        File iconFile = new File(QueueService.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "static/images/logo.ico");
        File copyFile = new File("c:\\intel\\logo.ico");
        if (!copyFile.exists()) {
            copyFile.createNewFile();
        }
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("/static/images/logo.ico");
        if (resources.length > 0) {
            InputStream inputStream = resources[0].getInputStream();
            OutputStream outputStream = new FileOutputStream(copyFile);
            IOUtils.copy(inputStream, outputStream);
            inputStream.close();
            outputStream.flush();
            outputStream.close();
        }
//        Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler http://localhost:1101/");
        try {
            Runtime.getRuntime().exec("\"C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe\"  --kiosk http://localhost:1101/");
        } catch (Exception ignored) {
            try {
                Runtime.getRuntime().exec("\"C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe\"  --kiosk http://localhost:1101/");
            } catch (Exception ignored1) {

            }
        }
    }

    /**
     * 点餐排队
     *
     * @param data
     */
    public void addOrder(String data) {
        if (StringUtils.isBlank(data) || !data.contains(",")) {
            return;
        }
        lock.lock();
        try {
            String[] datas = data.split(config.getNumberNameSplit());
            for (String pair : datas) {
                String[] pairs = pair.split(config.getNamesSplit());
                String orderNo = pairs[0];
                String name = pairs[1];
                OrderModel orderModel = new OrderModel(orderNo, name);
//                // 有剩菜，直接取餐
//                if (LEFT_DISH_MAP.containsKey(name)) {
//                    READY_LIST.add(orderModel);
//                    int leftNo = LEFT_DISH_MAP.get(name) - 1;
//                    if (leftNo > 0) {
//                        LEFT_DISH_MAP.put(name, leftNo);
//                    } else {
//                        LEFT_DISH_MAP.remove(name);
//                    }
//                    // TODO: 2018/4/9 直接取菜 不做通知
//                    LedUtil.addMessage(orderNo);
////                    USBControl.callNo(Integer.valueOf(orderNo));
//                    return;
//                }
                Map<String, Integer> dishOrderMap = WAITING_LIST.stream().collect(Collectors.toMap(OrderModel::getDishName, OrderModel::getDishOrder, (order1, order2) -> order1 > order2 ? order2 : order1));
                if (dishOrderMap.containsKey(name)) {
                    orderModel.setDishOrder(dishOrderMap.get(name));
                } else {
                    orderModel.setDishOrder(order);
                    order++;
                }
//                if (CollectionUtils.isEmpty(COOKING_A_MAP)) {
//                    List<OrderModel> orderModelList = Lists.newArrayList();
//                    orderModelList.add(orderModel);
//                    COOKING_A_MAP.put(name, orderModelList);
//                    continue;
//                } else if (COOKING_A_MAP.containsKey(name) && COOKING_A_MAP.get(name).size() < 4) {
//                    List<OrderModel> orderModelList = COOKING_A_MAP.get(name);
//                    orderModelList.add(orderModel);
//                    COOKING_A_MAP.put(name, orderModelList);
//                    continue;
//                } else if (CollectionUtils.isEmpty(COOKING_B_MAP)) {
//                    List<OrderModel> orderModelList = Lists.newArrayList();
//                    orderModelList.add(orderModel);
//                    COOKING_B_MAP.put(name, orderModelList);
//                    continue;
//                } else if (COOKING_B_MAP.containsKey(name) && COOKING_B_MAP.get(name).size() < 4) {
//                    List<OrderModel> orderModelList = COOKING_B_MAP.get(name);
//                    orderModelList.add(orderModel);
//                    COOKING_B_MAP.put(name, orderModelList);
//                    continue;
//                }
                WAITING_LIST.add(orderModel);
            }
        } finally {
            lock.unlock();
            doBackup();
        }
    }

    /**
     * 取餐后移除
     *
     * @param orderModel
     */
    public void remove(OrderModel orderModel) {
//        Integer callNo = orderModel.getCallNo();
        String id = orderModel.getId();
        if (StringUtils.isEmpty(id)) {
            return;
        }
        lock.lock();
        try {
            Iterator iterator = READY_LIST.iterator();
            while (iterator.hasNext()) {
                OrderModel target = (OrderModel) iterator.next();
                if (target.getId().equals(id)) {
                    READY_LIST.remove(target);
                    break;
                }
            }
        } finally {
            lock.unlock();
            doBackup();
        }
    }

    /**
     * 绑定叫号器
     *
     * @param orderModel
     */
    public void bind(OrderModel orderModel) {
        Integer callNo = orderModel.getCallNo();
        String id = orderModel.getId();
        if (null == callNo || StringUtils.isEmpty(id)) {
            return;
        }
        lock.lock();
        try {
            Iterator iterator = WAITING_LIST.iterator();
            while (iterator.hasNext()) {
                OrderModel target = (OrderModel) iterator.next();
                if (target.getId().equals(id)) {
                    WAITING_LIST.remove(target);
                    target.setCallNo(callNo);
                    String dishName = target.getDishName();
                    List<OrderModel> orderModelList;
                    // 如果有空锅，直接放入空锅排队开始炒菜
                    if (CollectionUtils.isEmpty(COOKING_A_MAP)) {
                        WAITING_LIST.remove(target);
                        orderModelList = Lists.newArrayList();
                        // 修改为正在炒状态
                        target.setState(2);
                        orderModelList.add(target);
                        COOKING_A_MAP.put(dishName, orderModelList);
                    } else if (COOKING_A_MAP.containsKey(dishName) && COOKING_A_MAP.get(dishName).size() < 4) {
                        WAITING_LIST.remove(target);
                        orderModelList = COOKING_A_MAP.get(dishName);
                        target.setState(2);
                        orderModelList.add(target);
                        COOKING_A_MAP.put(dishName, orderModelList);
                    } else if (CollectionUtils.isEmpty(COOKING_B_MAP)) {
                        WAITING_LIST.remove(target);
                        orderModelList = Lists.newArrayList();
                        target.setState(2);
                        orderModelList.add(target);
                        COOKING_B_MAP.put(dishName, orderModelList);
                    } else if (COOKING_B_MAP.containsKey(dishName) && COOKING_B_MAP.get(dishName).size() < 4) {
                        WAITING_LIST.remove(target);
                        orderModelList = COOKING_B_MAP.get(dishName);
                        target.setState(2);
                        orderModelList.add(target);
                        COOKING_B_MAP.put(dishName, orderModelList);
                    } else {
                        // 修改为已绑定状态
                        target.setState(1);
                        WAITING_LIST.add(target);
                    }
                    return;
                }
            }

//            COOKING_A_MAP.entrySet().forEach(entry -> {
//                String dishName = entry.getKey();
//                List<OrderModel> orderModelList = entry.getValue();
//                orderModelList = orderModelList.stream().map(om -> {
//                    if (om.getOrderNo().equals(orderNo)) {
//                        om.setCallNo(callNo);
//                    }
//                    return om;
//                }).collect(Collectors.toList());
//                COOKING_A_MAP.put(dishName, orderModelList);
//            });
//
//            COOKING_B_MAP.entrySet().forEach(entry -> {
//                String dishName = entry.getKey();
//                List<OrderModel> orderModelList = entry.getValue();
//                orderModelList = orderModelList.stream().map(om -> {
//                    if (om.getOrderNo().equals(orderNo)) {
//                        om.setCallNo(callNo);
//                    }
//                    return om;
//                }).collect(Collectors.toList());
//                COOKING_B_MAP.put(dishName, orderModelList);
//            });
        } finally {
            lock.unlock();
            doBackup();
        }
    }

    /**
     * 修改炒锅状态
     *
     * @param pot
     */
    public void changeStatus(String pot) {
        if (StringUtils.isEmpty(pot)) {
            return;
        }
        lock.lock();
        try {
            switch (pot) {
                case POT_A_NAME:
                    this.changePot(COOKING_A_MAP);
                    break;
                case POT_B_NAME:
                    this.changePot(COOKING_B_MAP);
                    break;
                default:
            }
        } finally {
            lock.unlock();
            doBackup();
        }
    }


    /**
     * 获取排队情况
     *
     * @return
     */
    public ResultModel getQueue() {
        lock.lock();
        ResultModel resultModel = new ResultModel();
        try {
//            // 剩菜
//            LEFT_DISH_MAP.entrySet().forEach(entry -> {
//                ResultModel.LeftDish leftDish = new ResultModel.LeftDish();
//                leftDish.setCount(entry.getValue());
//                leftDish.setName(entry.getKey());
//                resultModel.getLeftDish().add(leftDish);
//            });
            // 正在炒
            if (!CollectionUtils.isEmpty(COOKING_A_MAP)) {
                ResultModel.CookPot cookPot = new ResultModel.CookPot();
                COOKING_A_MAP.entrySet().forEach(entry -> {
                    cookPot.setName(COOKING_A_MAP.keySet().iterator().next());
                    cookPot.setPot(POT_A_NAME);
                    cookPot.getWaitNo().addAll(entry.getValue());
                });
                resultModel.getCooking().add(cookPot);
            }
            if (!CollectionUtils.isEmpty(COOKING_B_MAP)) {
                ResultModel.CookPot cookPot = new ResultModel.CookPot();
                COOKING_B_MAP.entrySet().forEach(entry -> {
                    cookPot.setName(COOKING_B_MAP.keySet().iterator().next());
                    cookPot.setPot(POT_B_NAME);
                    cookPot.getWaitNo().addAll(entry.getValue());
                });
                resultModel.getCooking().add(cookPot);
            }
            List<ResultModel.WaitQueue> waitQueueList = Lists.newArrayList();
            // 排队中
            WAITING_LIST.stream().collect(Collectors.groupingBy(OrderModel::getDishName)).forEach((s, orderModels) -> {
                double size = (double) orderModels.size();
                int page = (int) Math.ceil(size / 4);
                for (int i = 0; i < page; i++) {
                    ResultModel.WaitQueue waitQueue = new ResultModel.WaitQueue();
                    waitQueue.setName(s);
                    waitQueue.setSize((int) size);
                    waitQueue.setSortNo(orderModels.get(0).getDishOrder());
                    waitQueue.setWaitNo(orderModels.subList(4 * i, Math.min(4 * (i + 1), (int) size)));
                    waitQueueList.add(waitQueue);
                }
            });
            waitQueueList.sort((o1, o2) -> o1.getSortNo() < o2.getSortNo() ? -1 : 1);
            resultModel.getWaiting().addAll(waitQueueList);
            resultModel.getReady().addAll(READY_LIST);
        } finally {
            lock.unlock();
        }
        return resultModel;
    }

    /**
     * 获取监控地址
     *
     * @return
     */
    public List<String> getIpList() {
        String path = getBasePath() + "ip.txt";
        File ipFile = new File(path);
        if (!ipFile.exists()) {
            return config.getIps();
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(ipFile), "GBK"));
            List<String> ipList = Lists.newArrayList();
            String tempString = null;
            int line = 1;
            while ((tempString = reader.readLine()) != null) {
                // 显示行号
                ipList.add(tempString);
                if (line > 4) {
                    break;
                }
                line++;
            }
            reader.close();
            return ipList;
        } catch (IOException e) {
            return config.getIps();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }

    }

    /**
     * 获取菜单
     *
     * @return
     */
    public List<Map<String, String>> getMenu() {
//        if (true) {
//            Map<String, String> item = Maps.newHashMap();
//            List<Map<String, String>> menus = Lists.newArrayList();
//            item.put("name", "鱼香茄子");
//            item.put("price", "15");
//            for (int i = 0; i < 8; i++) {
//                menus.add(item);
//            }
//            return menus;
//        }
        String path = getBasePath() + "menu.txt";
        File menuFile = new File(path);
        if (!menuFile.exists()) {
            return Lists.newArrayList();
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(menuFile), "GBK"));
            List<String> menuList = Lists.newArrayList();
            String tempString = null;
            int line = 1;
            while ((tempString = reader.readLine()) != null) {
                menuList.add(tempString);
                if (line > 8) {
                    break;
                }
                line++;
            }
            reader.close();
            if (CollectionUtils.isEmpty(menuList)) {
                return Lists.newArrayList();
            }
            return menuList.stream()
                    .filter(s -> s.contains(",") && s.contains(";"))
                    .map(s -> s.split(";")).flatMap(Arrays::stream)
                    .map(s -> s.split(",")).map(s -> {
                        Map<String, String> itemMap = Maps.newHashMap();
                        itemMap.put("name", s[0]);
                        itemMap.put("price", s[1] + "");
                        return itemMap;
                    }).collect(Collectors.toList());
        } catch (IOException e) {
            return Lists.newArrayList();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
    }


    /**
     * 获取背景图片地址
     *
     * @return
     */
    public String getImgUrl() {
//        String imgPath = QueueService.getBasePath() + "img" + (QueueService.isWindows ? "\\\\" : "/");
//        File imgDir = new File(imgPath);
//        if (imgDir.exists() && imgDir.listFiles().length != 0) {
        return "/img/bg.jpg";
//        } else {
//            return "/images/bg.jpg";
//        }
    }

    /**
     * 清除所有数据
     */
    public void clear() {
        lock.lock();
        try {
            order = 0;
            COOKING_A_MAP.clear();
            COOKING_B_MAP.clear();
            WAITING_LIST.clear();
//            LEFT_DISH_MAP.clear();
            READY_LIST.clear();
            BACKUP_MAP = Maps.newConcurrentMap();
            doBackup();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 手动修改排队状态，可能是因为有剩菜
     *
     * @param orderModel
     */
    public void forceChange(OrderModel orderModel) {
        int state = orderModel.getState();
        if (1 != state || StringUtils.isEmpty(orderModel.getId())) {
            return;
        }
        OrderModel target = null;
        String id = orderModel.getId();
        lock.lock();
        try {
            Map<String, List<OrderModel>> waitMap = WAITING_LIST.stream().collect(Collectors.groupingBy(OrderModel::getId));
            if (waitMap.containsKey(id)) {
                target = waitMap.get(id).get(0);
                WAITING_LIST.remove(target);
            } else {
                Map<String, List<OrderModel>> cookAMap = COOKING_A_MAP.values().stream().flatMap(List::stream).collect(Collectors.groupingBy(OrderModel::getId));
                if (cookAMap.containsKey(id)) {
                    target = cookAMap.get(id).get(0);
                    String dishName = target.getDishName();
                    List<OrderModel> dishes = COOKING_A_MAP.get(dishName);
                    if (dishes.size() == 1) {
                        COOKING_A_MAP.remove(dishName);
                        if (!CollectionUtils.isEmpty(WAITING_LIST)) {
                            // 确定下一锅要炒的菜
                            Optional<Map.Entry<Integer, List<OrderModel>>> optional = WAITING_LIST.stream().filter(o -> o.getState() == 1).collect(Collectors.groupingBy(OrderModel::getDishOrder)).entrySet()
                                    .stream().min((o1, o2) -> o1.getKey() < o2.getKey() ? -1 : 1);
                            if (optional.isPresent()) {
                                List<OrderModel> orderModelList = optional.get().getValue();
                                String name = orderModelList.iterator().next().getDishName();
                                List<OrderModel> nextPotOrders = Lists.newArrayList(orderModelList.subList(0, Math.min(orderModelList.size(), 4)));
                                COOKING_A_MAP.put(name, nextPotOrders);
                                nextPotOrders.forEach(WAITING_LIST::remove);
                            }
                        }
                    } else {
                        COOKING_A_MAP.get(dishName).remove(target);
                        Optional<Map.Entry<Integer, List<OrderModel>>> optional = WAITING_LIST.stream().filter(o -> o.getState() == 1 && dishName.equals(o.getDishName()))
                                .collect(Collectors.groupingBy(OrderModel::getDishOrder)).entrySet()
                                .stream().min((o1, o2) -> o1.getKey() < o2.getKey() ? -1 : 1);
                        if (optional.isPresent()) {
                            List<OrderModel> sameDishes = optional.get().getValue();
                            List<OrderModel> nextPotOrders = Lists.newArrayList(sameDishes.subList(0, Math.min(sameDishes.size(), 4 - dishes.size())));
                            dishes.addAll(nextPotOrders);
                            nextPotOrders.forEach(WAITING_LIST::remove);
                        }
                    }
                } else {
                    Map<String, List<OrderModel>> cookBMap = COOKING_B_MAP.values().stream().flatMap(List::stream).collect(Collectors.groupingBy(OrderModel::getId));
                    if (cookBMap.containsKey(id)) {
                        target = cookBMap.get(id).get(0);
                        String dishName = target.getDishName();
                        List<OrderModel> dishes = COOKING_B_MAP.get(dishName);
                        if (dishes.size() == 1) {
                            COOKING_B_MAP.remove(dishName);
                            if (!CollectionUtils.isEmpty(WAITING_LIST)) {
                                // 确定下一锅要炒的菜
                                Optional<Map.Entry<Integer, List<OrderModel>>> optional = WAITING_LIST.stream().filter(o -> o.getState() == 1).collect(Collectors.groupingBy(OrderModel::getDishOrder)).entrySet()
                                        .stream().min((o1, o2) -> o1.getKey() < o2.getKey() ? -1 : 1);
                                if (optional.isPresent()) {
                                    List<OrderModel> orderModelList = optional.get().getValue();
                                    String name = orderModelList.iterator().next().getDishName();
                                    List<OrderModel> nextPotOrders = Lists.newArrayList(orderModelList.subList(0, Math.min(orderModelList.size(), 4)));
                                    COOKING_B_MAP.put(name, nextPotOrders);
                                    nextPotOrders.forEach(WAITING_LIST::remove);
                                }
                            }
                        } else {
                            COOKING_B_MAP.get(dishName).remove(target);
                            Optional<Map.Entry<Integer, List<OrderModel>>> optional = WAITING_LIST.stream().filter(o -> o.getState() == 1 && dishName.equals(o.getDishName()))
                                    .collect(Collectors.groupingBy(OrderModel::getDishOrder)).entrySet()
                                    .stream().min((o1, o2) -> o1.getKey() < o2.getKey() ? -1 : 1);
                            if (optional.isPresent()) {
                                List<OrderModel> sameDishes = optional.get().getValue();
                                List<OrderModel> nextPotOrders = Lists.newArrayList(sameDishes.subList(0, Math.min(sameDishes.size(), 4 - dishes.size())));
                                dishes.addAll(nextPotOrders);
                                nextPotOrders.forEach(WAITING_LIST::remove);
                            }
                        }
                    }
                }
            }
//            for (OrderModel model : WAITING_LIST) {
//                if (Objects.equals(model.getId(), orderModel.getId())) {
//                    target = model;
//                    break;
//                }
//            }
            if (null != target) {
                WAITING_LIST.remove(target);
                target.setState(3);
                target.setTime(target.getTime());
                READY_LIST.add(target);
                if (null != target.getCallNo()) {
                    LedUtil.addMessage(target.getCallNo() + "");
                    System.out.println(target.getCallNo());
                    USBControl.callNo(target.getCallNo());
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 改变正在炒的菜品
     *
     * @param map
     */
    private void changePot(final Map<String, List<OrderModel>> map) {
        map.entrySet().forEach(entry ->
        {
            List<OrderModel> readyList = entry.getValue();
            if (!CollectionUtils.isEmpty(readyList)) {
                readyList.forEach(orderModel -> {
                    orderModel.setState(3);
                    orderModel.setTime(orderModel.getTime());
                    READY_LIST.add(orderModel);
                    if (null != orderModel.getCallNo()) {
                        LedUtil.addMessage(orderModel.getCallNo() + "");
                        System.out.println(orderModel.getCallNo());
                        USBControl.callNo(orderModel.getCallNo());
                    }
                });
//                if (readyList.size() < 4) {
//                    LEFT_DISH_MAP.put(entry.getKey(), 4 - readyList.size());
//                }
            }
        });
        map.clear();
        if (CollectionUtils.isEmpty(WAITING_LIST)) {
            return;
        }
        // 确定下一锅要炒的菜
        Optional<Map.Entry<Integer, List<OrderModel>>> optional = WAITING_LIST.stream().filter(o -> o.getState() == 1).collect(Collectors.groupingBy(OrderModel::getDishOrder)).entrySet()
                .stream().min((o1, o2) -> o1.getKey() < o2.getKey() ? -1 : 1);
        if (optional.isPresent()) {
            List<OrderModel> orderModelList = optional.get().getValue();
            String name = orderModelList.iterator().next().getDishName();
            List<OrderModel> nextPotOrders = Lists.newArrayList(orderModelList.subList(0, Math.min(orderModelList.size(), 4)));
            map.put(name, nextPotOrders);
            nextPotOrders.forEach(WAITING_LIST::remove);
        }
    }

    /**
     * 备份
     *
     * @throws IOException
     */
    public void backup() throws IOException {
        String path = getBasePath() + "backup.txt";
        File backupFile = new File(path);
        System.out.println(path);
        if (!backupFile.exists()) {
            backupFile.createNewFile();
        }
        ObjectOutputStream outputStream = new ObjectOutputStream(
                new FileOutputStream(backupFile));

        BACKUP_MAP.put("a", QueueService.COOKING_A_MAP);
        BACKUP_MAP.put("b", QueueService.COOKING_B_MAP);
        BACKUP_MAP.put("w", QueueService.WAITING_LIST);
//        BACKUP_MAP.put("l", QueueService.LEFT_DISH_MAP);
        BACKUP_MAP.put("r", QueueService.READY_LIST);
        BACKUP_MAP.put("order", order);
        System.out.print("保存备份:" + path);
        outputStream.writeObject(BACKUP_MAP);
        outputStream.close();
        System.out.println("成功！");
    }

    /**
     * 反序列化
     */
    @SuppressWarnings("unchecked")
    public void recovery() throws IOException, ClassNotFoundException {
        String path = getBasePath() + "backup.txt";
        File backupFile = new File(path);
        if (!backupFile.exists()) {
            return;
        }
        ObjectInputStream inputStream;
        try {
            inputStream = new ObjectInputStream(
                    new FileInputStream(backupFile));
        } catch (Exception e) {
            return;
        }
        System.out.print("加载备份:" + path);
        BACKUP_MAP = (Map<String, Object>) inputStream.readObject();

        if (BACKUP_MAP.containsKey("a")) {
            QueueService.COOKING_A_MAP.clear();
            QueueService.COOKING_A_MAP.putAll((Map<String, List<OrderModel>>) BACKUP_MAP.get("a"));
        }
        if (BACKUP_MAP.containsKey("b")) {
            QueueService.COOKING_B_MAP.clear();
            QueueService.COOKING_B_MAP.putAll((Map<String, List<OrderModel>>) BACKUP_MAP.get("b"));
        }
        if (BACKUP_MAP.containsKey("w")) {
            QueueService.WAITING_LIST.clear();
            QueueService.WAITING_LIST.addAll((Collection<? extends OrderModel>) BACKUP_MAP.get("w"));
        }
//        if (BACKUP_MAP.containsKey("l")) {
//            QueueService.LEFT_DISH_MAP.clear();
//            QueueService.LEFT_DISH_MAP.putAll((Map<? extends String, ? extends Integer>) BACKUP_MAP.get("l"));
//        }

        if (BACKUP_MAP.containsKey("r")) {
            QueueService.READY_LIST.clear();
            QueueService.READY_LIST.addAll((Collection<? extends OrderModel>) BACKUP_MAP.get("r"));
        }
        if (BACKUP_MAP.containsKey("order")) {
            order = (int) BACKUP_MAP.get("order");
        }
        inputStream.close();
        System.out.println("成功！");
    }

    public void doBackup() {
        new Thread() {
            public void run() {
                try {
                    backup();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public static String getBasePath() {
        String path = QueueService.class.getProtectionDomain().getCodeSource().getLocation().getPath();
//        if (path.contains("file:")) {
//            path = path.substring(5);
//        }
//        path = path.contains("c.jar") ? path.substring(0, path.indexOf("c.jar")) : path;
        File dir;
        if (isWindows) {
//            path = "C:\\Documents and Settings\\Administrator\\桌面\\canteen\\";
            path = "C:\\intel\\";
//            path = path.replaceAll("/", "\\\\");
        } else {
            path = "/tmp/canteen/";
        }
        dir = new File(path);
        if (!dir.exists()) {
            dir.mkdir();
        }
        return path;
    }

    public void closeChrome() throws IOException {
        Runtime.getRuntime().exec("taskkill /im chrome.exe");
    }
}
