package com.yupi.yurpc.registry;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import com.yupi.yurpc.conifg.RegistryConfig;
import com.yupi.yurpc.model.ServiceMetaInfo;
import io.etcd.jetcd.*;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchEvent;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EtcdRegistry implements Registry{
    /**
     * 本机注册的节点 key 集合（用于维护续期）,提供者使用
     */
    private final Set<String> localRegisterNodeKeySet = new HashSet<>();
    /**
     * 注册中心缓存（服务消费端）
     */
    private final RegistryServiceMultiCache registryCache = new RegistryServiceMultiCache();
    /**
     * 正在监听的 key 集合（服务消费端）
     */
    private final Set<String> watchingKeySet = new ConcurrentHashSet<>();
    private Client client;
    private KV kvClient;
    /**
     * 根节点
     */
    private static final String ETCD_ROOT_PATH = "/rpc/";
    /**
     * 初始化
     *
     * @param registryConfig
     */
    @Override
    public void init(RegistryConfig registryConfig) {
        client = Client.builder()
                .endpoints(registryConfig.getAddress())
                .connectTimeout(Duration.ofSeconds(registryConfig.getTimeout()))
                .build();
        kvClient = client.getKVClient();
        heartBeat();
    }

    /**
     * 注册服务（服务提供端）
     *
     * @param serviceMetaInfo 服务元信息
     */
    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        // 创建租约
        Lease leaseClient = client.getLeaseClient();
        long leaseId = leaseClient.grant(30).get().getID();
        // 创建键值对
        String registryKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        ByteSequence key = ByteSequence.from(registryKey, StandardCharsets.UTF_8);
        ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8);
        // 绑定租约，并发送添加键值对请求
        PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
        kvClient.put(key, value, putOption).get();

        localRegisterNodeKeySet.add(registryKey);
    }

    /**
     * 服务发现（服务消费端）
     *
     * @param serviceKey 服务键
     * @return 服务元信息
     */
    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) throws Exception {
        // 先尝试从缓存获取服务列表
        List<ServiceMetaInfo> serviceMetaInfos = registryCache.readCache(serviceKey);
        if (CollectionUtil.isNotEmpty(serviceMetaInfos)) {
            return serviceMetaInfos;
        }
        // 根据前缀搜索 eg: /rpc/com.yupi.yurpc.demo.DemoService:1.0/
        // 最后加一个“/”起到分割作用
        String searchPrefix = ETCD_ROOT_PATH + serviceKey + "/";
        try {
            GetOption getOption = GetOption.builder().isPrefix(true).build();
            ByteSequence prefix = ByteSequence.from(searchPrefix, StandardCharsets.UTF_8);
            List<KeyValue> kvs = kvClient.get(prefix, getOption).get().getKvs();
            List<ServiceMetaInfo> metaInfoList = kvs.stream().map(kv -> {
                //监听每一个实现类的key
                String key = kv.getKey().toString(StandardCharsets.UTF_8);
                watch(key);
                // 解析服务元信息
                String value = kv.getValue().toString(StandardCharsets.UTF_8);
                return JSONUtil.toBean(value, ServiceMetaInfo.class);
            }).collect(Collectors.toList());
            // 写入缓存
            registryCache.writeCache(serviceKey, metaInfoList);
            return metaInfoList;
        } catch (Exception e) {
            throw new RuntimeException("服务拉取失败");
        }
    }

    /**
     * 服务注销（服务提供端）
     *
     * @param serviceMetaInfo 服务元信息
     */
    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) throws Exception {
        String registryKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        kvClient.delete(ByteSequence.from(registryKey, StandardCharsets.UTF_8)).get();
        localRegisterNodeKeySet.remove(registryKey);
    }

    /**
     * 注册中心销毁
     */
    @Override
    public void destroy() {
        System.out.println("当前节点下线");
        for (var key : localRegisterNodeKeySet) {
            try {
                kvClient.delete(ByteSequence.from(key, StandardCharsets.UTF_8)).get();
            } catch (Exception e) {
                throw new RuntimeException(key + "下线失败", e);
            }
        }
        if (client != null) {
            client.close();
        }
        if (kvClient != null) {
            kvClient.close();
        }
    }

    /**
     * 心跳检测（服务端）
     */
    @Override
    public void heartBeat() {
        CronUtil.schedule("*/10 * * * * *", new Task() {
            @Override
            public void execute() {
                for (var key : localRegisterNodeKeySet) {
                    try {
                        List<KeyValue> kvs = kvClient.get(ByteSequence.from(key, StandardCharsets.UTF_8)).get().getKvs();
                        if (CollectionUtil.isEmpty(kvs)) {
                            continue;
                        }
                        String value = kvs.get(0).getValue().toString(StandardCharsets.UTF_8);
                        ServiceMetaInfo serviceMetaInfo = JSONUtil.toBean(value, ServiceMetaInfo.class);
                        register(serviceMetaInfo);
                    } catch (Exception e) {
                        throw new RuntimeException(key + "续签失败", e);
                    }
                }
            }
        });
        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }

    /**
     * 监听（消费端）
     *
     * @param serviceNodeKey
     */
    @Override
    public void watch(String serviceNodeKey) {
        Watch watchClient = client.getWatchClient();
        boolean isNew = watchingKeySet.add(serviceNodeKey);
        if (isNew) {
            System.out.println("开始监听节点: " + serviceNodeKey);
            watchClient.watch(ByteSequence.from(serviceNodeKey, StandardCharsets.UTF_8), response -> {
                for (WatchEvent event : response.getEvents()) {
                    String key = event.getKeyValue().getKey().toString(StandardCharsets.UTF_8);
                    String serviceKey = key.substring(4, key.lastIndexOf("/"));
                    switch (event.getEventType()) {
                        case PUT:
                        case DELETE:
                            // 服务列表更新
                            registryCache.deleteCache(serviceKey);
                            System.out.println("啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊要去了啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊");
                            break;
                        default:
                            break;
                    }
                }
            });
        }
    }
}
