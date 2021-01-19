### first question

import eureka：

```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

instance-A setting：

```
eureka.instance.metadataMap.group=example-service-group
eureka.instance.metadataMap.version=1.0
eureka.instance.metadataMap.region=dev
eureka.instance.metadataMap.env=env1
eureka.instance.metadataMap.zone=zone1
```

instance-B setting：

```
eureka.instance.metadataMap.group=example-service-group
eureka.instance.metadataMap.version=1.1
eureka.instance.metadataMap.region=prod
eureka.instance.metadataMap.env=env2
eureka.instance.metadataMap.zone=zone2
```

![image-20210119110459413](C:\Users\cosmos\AppData\Roaming\Typora\typora-user-images\image-20210119110459413.png)





When I use the above configuration，calling method getInstance，i can get the metadata for each instance(instance-A /instance-B)

or  how I can read the label data of K8S pod?

```java
@RestController
public class DiscoveryController {
    @Autowired
    private DiscoveryClient discoveryClient;
    
    @GetMapping("/service")
    public List<String> getServiceList() {
        return discoveryClient.getServices();
    }

    @GetMapping("/instance")
    public Object getInstance(@RequestParam("name") String name) {
        return discoveryClient.getInstances(name);
    }

}
```



The deployment scripts of k8s:

After the test, I can't get the instance label（version: v1）

```
apiVersion: v1
kind: Service
metadata:
  name: kubernetes-demo
  labels:
    app: kubernetes-demo
    service: kubernetes-demo
spec:
  ports:
  - port: 8080
    targetPort: 8080
    nodePort: 31001
    name: http
  type: NodePort
  selector:
    app: kubernetes-demo
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: kubernetes-demo-v1
  labels:
    app: kubernetes-demo
spec:
  replicas: 1
  selector:
    matchLabels:
      app: kubernetes-demo
  template:
    metadata:
      labels:
        app: kubernetes-demo
        version: v1
    spec:
      containers:
      - name: kubernetes-demo
        image: docker.cmss.io:5000/cnvas/spring-cloud-kubernetes-demo:1.0-SNAPSHOT
        imagePullPolicy: Always
        ports:
        - containerPort: 8080
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: kubernetes-demo-v2
  labels:
    app: kubernetes-demo
spec:
  replicas: 1
  selector:
    matchLabels:
      app: kubernetes-demo
  template:
    metadata:
      labels:
        app: kubernetes-demo
        version: v2
    spec:
      containers:
      - name: kubernetes-demo
        image: docker.cmss.io:5000/cnvas/spring-cloud-kubernetes-demo:1.0-SNAPSHOT
        imagePullPolicy: Always
        ports:
        - containerPort: 8080
```



so my question is,in SpringCloud kubernetes,what configuration can I use to get this result.

```
maven dependency:
<dependency>
   <groupId>org.springframework.cloud</groupId>
   <artifactId>spring-cloud-starter-kubernetes-fabric8-all</artifactId>
</dependency>
```



### second question:

When I configuration service-labels,I found that all services and instances could not be found.I wish there was a switch to turn it off.

```properties
spring.cloud.kubernetes.discovery.service-labels.group=example-service-group
spring.cloud.kubernetes.discovery.service-labels.version=1.1
```



I tried to rewrite KubernetesDiscoveryClient,The method  (discoveryClient.getInstances(name) )  works,but  for ( discoveryClient.getServices() ),It didn't work.

```java
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

```



