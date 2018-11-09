package com.jll.canteen.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jll.canteen.config.Config;
import com.jll.canteen.model.OrderModel;
import com.jll.canteen.model.ResultModel;
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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
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

    public static final Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

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
    public static final Map<String, List<OrderModel>> COOKING_A_MAP = Maps.newConcurrentMap();
    // B锅正在炒的队列
    public static final Map<String, List<OrderModel>> COOKING_B_MAP = Maps.newConcurrentMap();
    // 还未炒的队列
    public static final List<OrderModel> WAITING_LIST = Collections.synchronizedList(Lists.newArrayList());
    // 等待取餐
    public static final List<OrderModel> READY_LIST = Collections.synchronizedList(Lists.newArrayList());
    // 锁
    private Lock lock = new ReentrantLock();
    // 缓存菜品序列，保证同一菜品的优先性
    private Map<String, Integer> ORDER_MAP = Maps.newConcurrentMap();


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
                if (pairs.length != 2) {
                    continue;
                }
                String orderNo = pairs[0];
                String name = pairs[1];
                OrderModel orderModel = new OrderModel(orderNo, name);
                Map<String, List<OrderModel>> dishCountMap = WAITING_LIST.stream().collect(Collectors.groupingBy(OrderModel::getDishName));
                int dishOrder;
                if (ORDER_MAP.containsKey(name) && ORDER_MAP.get(name) < 4) {
                    List<OrderModel> modelList = dishCountMap.get(name);
                    dishOrder = modelList.get(modelList.size() - 1).getDishOrder();
                } else {
                    order++;
                    dishOrder = order;
                }
                if (ORDER_MAP.containsKey(name) && ORDER_MAP.get(name) == 4) {
                    ORDER_MAP.clear();
                }
                ORDER_MAP.put(name, ORDER_MAP.computeIfAbsent(name, s -> 0) + 1);
                orderModel.setDishOrder(dishOrder);
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
                    iterator.remove();
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
//                    WAITING_LIST.remove(target);
                    target.setCallNo(callNo);
                    String dishName = target.getDishName();
                    List<OrderModel> orderModelList;
                    // 如果有空锅，直接放入空锅排队开始炒菜
                    if (CollectionUtils.isEmpty(COOKING_A_MAP)) {
                        iterator.remove();
                        orderModelList = Lists.newArrayList();
                        // 修改为正在炒状态
                        target.setState(2);
                        orderModelList.add(target);
                        COOKING_A_MAP.put(dishName, orderModelList);
                    } else if (COOKING_A_MAP.containsKey(dishName) && COOKING_A_MAP.get(dishName).size() < 4
                            && COOKING_A_MAP.get(dishName).get(0).getDishOrder() == target.getDishOrder()) {
                        iterator.remove();
                        orderModelList = COOKING_A_MAP.get(dishName);
                        target.setState(2);
                        orderModelList.add(target);
                        COOKING_A_MAP.put(dishName, orderModelList);
                    } else if (CollectionUtils.isEmpty(COOKING_B_MAP)) {
                        iterator.remove();
                        orderModelList = Lists.newArrayList();
                        target.setState(2);
                        orderModelList.add(target);
                        COOKING_B_MAP.put(dishName, orderModelList);
                    } else if (COOKING_B_MAP.containsKey(dishName) && COOKING_B_MAP.get(dishName).size() < 4
                            && COOKING_B_MAP.get(dishName).get(0).getDishOrder() == target.getDishOrder()) {
                        iterator.remove();
                        orderModelList = COOKING_B_MAP.get(dishName);
                        target.setState(2);
                        orderModelList.add(target);
                        COOKING_B_MAP.put(dishName, orderModelList);
                    } else {
                        // 修改为已绑定状态
                        target.setState(1);
                    }
                    return;
                }
            }

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
            // 正在炒
            if (!CollectionUtils.isEmpty(COOKING_A_MAP)) {
                ResultModel.CookPot cookPot = new ResultModel.CookPot();
                COOKING_A_MAP.entrySet().forEach(entry -> {
                    cookPot.setName(COOKING_A_MAP.keySet().iterator().next());
                    cookPot.setPot(POT_A_NAME);
                    List<OrderModel> orderModels = entry.getValue();
                    orderModels.sort((o1, o2) -> o2.getTime().compareTo(o1.getTime()));
                    cookPot.getWaitNo().addAll(orderModels);
                });
                resultModel.getCooking().add(cookPot);
            }
            if (!CollectionUtils.isEmpty(COOKING_B_MAP)) {
                ResultModel.CookPot cookPot = new ResultModel.CookPot();
                COOKING_B_MAP.entrySet().forEach(entry -> {
                    cookPot.setName(COOKING_B_MAP.keySet().iterator().next());
                    cookPot.setPot(POT_B_NAME);
                    List<OrderModel> orderModels = entry.getValue();
                    orderModels.sort((o1, o2) -> o2.getTime().compareTo(o1.getTime()));
                    cookPot.getWaitNo().addAll(orderModels);
                });
                resultModel.getCooking().add(cookPot);
            }
            List<ResultModel.WaitQueue> waitQueueList = Lists.newArrayList();
            WAITING_LIST.stream().sorted(Comparator.comparingInt(OrderModel::getDishOrder)).collect(Collectors.groupingBy(OrderModel::getDishOrder)).forEach((s, orderModels) -> {
                OrderModel om = orderModels.get(0);
                ResultModel.WaitQueue waitQueue = new ResultModel.WaitQueue();
                waitQueue.setName(om.getDishName());
                waitQueue.setSize(orderModels.size());
                waitQueue.setSortNo(s);
                waitQueue.setWaitNo(orderModels);
                waitQueueList.add(waitQueue);
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
        return "/img/bg.jpg";
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
            ORDER_MAP.clear();
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
                        boolean swapped = this.swapPot();
                        if (!CollectionUtils.isEmpty(WAITING_LIST)) {
                            // 确定下一锅要炒的菜
                            Optional<Map.Entry<Integer, List<OrderModel>>> optional = WAITING_LIST.stream().filter(o -> o.getState() == 1).collect(Collectors.groupingBy(OrderModel::getDishOrder)).entrySet()
                                    .stream().min((o1, o2) -> o1.getKey() < o2.getKey() ? -1 : 1);
                            if (optional.isPresent()) {
                                List<OrderModel> orderModelList = optional.get().getValue();
                                String name = orderModelList.iterator().next().getDishName();
                                List<OrderModel> nextPotOrders = Lists.newArrayList(orderModelList.subList(0, Math.min(orderModelList.size(), 4)));
                                List<OrderModel> val = swapped ? COOKING_B_MAP.put(name, nextPotOrders) : COOKING_A_MAP.put(name, nextPotOrders);
                                nextPotOrders.forEach(WAITING_LIST::remove);
                            }
                        }
                    } else {
                        COOKING_A_MAP.get(dishName).remove(target);
                        int dishOrder = target.getDishOrder();
                        Optional<Map.Entry<Integer, List<OrderModel>>> optional = WAITING_LIST.stream().filter(o -> o.getState() == 1 && dishName.equals(o.getDishName()) && o.getDishOrder() == dishOrder)
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
                            boolean swapped = this.swapPot();
                            if (!CollectionUtils.isEmpty(WAITING_LIST)) {
                                // 确定下一锅要炒的菜
                                Optional<Map.Entry<Integer, List<OrderModel>>> optional = WAITING_LIST.stream().filter(o -> o.getState() == 1).collect(Collectors.groupingBy(OrderModel::getDishOrder)).entrySet()
                                        .stream().min((o1, o2) -> o1.getKey() < o2.getKey() ? -1 : 1);
                                if (optional.isPresent()) {
                                    List<OrderModel> orderModelList = optional.get().getValue();
                                    String name = orderModelList.iterator().next().getDishName();
                                    List<OrderModel> nextPotOrders = Lists.newArrayList(orderModelList.subList(0, Math.min(orderModelList.size(), 4)));
                                    List<OrderModel> val = swapped ? COOKING_A_MAP.put(name, nextPotOrders) : COOKING_B_MAP.put(name, nextPotOrders);
                                    nextPotOrders.forEach(WAITING_LIST::remove);
                                }
                            }
                        } else {
                            COOKING_B_MAP.get(dishName).remove(target);
                            int dishOrder = target.getDishOrder();
                            Optional<Map.Entry<Integer, List<OrderModel>>> optional = WAITING_LIST.stream().filter(o -> o.getState() == 1 && dishName.equals(o.getDishName()) && o.getDishOrder() == dishOrder)
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
            if (null != target) {
                WAITING_LIST.remove(target);
                target.setState(3);
                target.setTime(target.getTime());
                READY_LIST.add(target);
                if (null != target.getCallNo()) {
                    System.out.println(target.getCallNo());
                    USBControl.callNo(target.getCallNo());
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 交换锅的顺序
     */
    private boolean swapPot() {
        if (COOKING_A_MAP.size() == 0) {
            Map<String, List<OrderModel>> tmpMap = Maps.newHashMap(COOKING_B_MAP);
            COOKING_B_MAP.clear();
            COOKING_B_MAP.putAll(Maps.newHashMap(COOKING_A_MAP));
            COOKING_A_MAP.clear();
            COOKING_A_MAP.putAll(tmpMap);
            return true;
        }
        return false;
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
                        System.out.println(orderModel.getCallNo());
                        USBControl.callNo(orderModel.getCallNo());
                    }
                });
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
            if (!ORDER_MAP.containsKey(name)) {
                ORDER_MAP.clear();
            }
        } else {
            ORDER_MAP.clear();
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
        try {
            BACKUP_MAP.put("a", QueueService.COOKING_A_MAP);
            BACKUP_MAP.put("b", QueueService.COOKING_B_MAP);
            BACKUP_MAP.put("w", QueueService.WAITING_LIST);
            BACKUP_MAP.put("r", QueueService.READY_LIST);
            BACKUP_MAP.put("order", order);
            System.out.print("保存备份:" + path);
            outputStream.writeObject(BACKUP_MAP);
            outputStream.close();
            System.out.println("成功！");
        } catch (Throwable ignored) {

        }
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
        executor.execute(() -> {
            try {
                backup();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static String getBasePath() {
        String path;
        File dir;
        if (isWindows) {
            path = "C:\\intel\\";
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
