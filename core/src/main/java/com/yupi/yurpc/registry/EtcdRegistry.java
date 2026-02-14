package com.yupi.yurpc.registry;

import cn.hutool.json.JSONUtil;
import com.yupi.yurpc.conifg.RegistryConfig;
import com.yupi.yurpc.model.ServiceMetaInfo;
import io.etcd.jetcd.*;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class EtcdRegistry implements Registry{

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
    }

    /**
     * 服务发现（服务消费端）
     *
     * @param serviceKey 服务键
     * @return 服务元信息
     */
    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) throws Exception {
        // 根据前缀搜索 eg: /rpc/com.yupi.yurpc.demo.DemoService:1.0/
        // 最后加一个“/”起到分割作用
        String searchPrefix = ETCD_ROOT_PATH + serviceKey + "/";
        try {
            GetOption getOption = GetOption.builder().isPrefix(true).build();
            ByteSequence prefix = ByteSequence.from(searchPrefix, StandardCharsets.UTF_8);
            List<KeyValue> kvs = kvClient.get(prefix, getOption).get().getKvs();
            return kvs.stream().map(kv -> {
                String value = kv.getValue().toString(StandardCharsets.UTF_8);
                return JSONUtil.toBean(value, ServiceMetaInfo.class);
            }).collect(Collectors.toList());
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
        kvClient.delete(ByteSequence.from(ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey(), StandardCharsets.UTF_8)).get();
    }

    /**
     * 注册中心销毁
     */
    @Override
    public void destroy() {
        System.out.println("当前节点下线");
        if (client != null) {
            client.close();
        }
        if (kvClient != null) {
            kvClient.close();
        }
    }
}
