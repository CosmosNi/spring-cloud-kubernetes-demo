package org.spring.kubernetes.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * @author: nijiahui
 * @Date: 2021/1/19 10:21
 * @Description:
 * @Version： 1.0
 */
@RestController
public class DiscoveryController {
    @Autowired
    private DiscoveryClient discoveryClient;

    /**
     * 获取k8s该NameSpace下的所有服务
     *
     * @return
     */
    @GetMapping("/service")
    public List<String> getServiceList() {
        return discoveryClient.getServices();
    }


    /**
     * 获取k8s该NameSpace下的服务的实例
     *
     * @param name
     * @return
     */
    @GetMapping("/instance")
    public Object getInstance(@RequestParam("name") String name) {
        return discoveryClient.getInstances(name);
    }

}
