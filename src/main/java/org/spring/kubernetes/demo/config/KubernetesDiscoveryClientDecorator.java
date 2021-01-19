package org.spring.kubernetes.demo.config;

/**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 *
 * @author Haojun Ren
 * @version 1.0
 */

import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.kubernetes.commons.discovery.KubernetesDiscoveryProperties;
import org.springframework.cloud.kubernetes.fabric8.discovery.KubernetesClientServicesFunction;
import org.springframework.cloud.kubernetes.fabric8.discovery.KubernetesDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.List;

public class KubernetesDiscoveryClientDecorator extends KubernetesDiscoveryClient {
    // private static final Logger LOG = LoggerFactory.getLogger(DiscoveryClientDecorator.class);

    private KubernetesDiscoveryProperties properties;
    private KubernetesClient client;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    public KubernetesDiscoveryClientDecorator(KubernetesClient client, KubernetesDiscoveryProperties kubernetesDiscoveryProperties, KubernetesClientServicesFunction kubernetesClientServicesFunction) {
        super(client, kubernetesDiscoveryProperties, kubernetesClientServicesFunction);
        this.properties = kubernetesDiscoveryProperties;
        this.client = client;
    }

    @Override
    public List<Endpoints> getEndPointsList(String serviceId) {
        return this.properties.isAllNamespaces()
                ? this.client.endpoints().inAnyNamespace().withField("metadata.name", serviceId)
                        .list().getItems()
                : this.client.endpoints().withField("metadata.name", serviceId)
                        .list().getItems();
    }

    @Override
    public List<String> getServices() {
        List<String> services = getRealServices();
        return services;
    }

    public List<String> getRealServices() {
        return super.getServices();
    }

    public ConfigurableEnvironment getEnvironment() {
        return applicationContext.getEnvironment();
    }
}
