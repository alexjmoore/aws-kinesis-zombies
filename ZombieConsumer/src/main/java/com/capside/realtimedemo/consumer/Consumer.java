package com.capside.realtimedemo.consumer;

import com.microsoft.azure.eventprocessorhost.*;
import com.microsoft.azure.servicebus.ConnectionStringBuilder;
import java.lang.management.ManagementFactory;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/*
    Derived from: https://github.com/capside/aws-kinesis-zombies
 */
@Component
@Slf4j
public class Consumer {

    private final String namespaceName;
    private final String eventHubName;
    private final String sasKey;
    private final String sasKeyName;
    private final String storageAccount;
    private final String storageAccountKey;
    private final String storageContainer;

    private final IEventProcessorFactory zombieEventFactory;

    @Autowired
    public Consumer(@Value("${ns}") String ns,
            @Value("${hub}") String hub,
            @Value("${keyname}") String keyname,
            @Value("${key}") String key,
            @Value("${storageaccount}") String storageAccount,
            @Value("${storagekey}") String storageAccountKey,
            @Value("${storagecontainer}") String storageContainer,
            IEventProcessorFactory zombieEventFactory) {
        this.namespaceName = ns;
        this.eventHubName = hub;
        this.sasKeyName = keyname;
        this.sasKey = key;
        this.storageAccount = storageAccount;
        this.storageAccountKey = storageAccountKey;
        this.storageContainer = storageContainer;
        this.zombieEventFactory = zombieEventFactory;
        this.initEventHub();
    }
    
    // Must read: https://github.com/Azure/azure-event-hubs-java/tree/e6413d059471d75224a62df89251c5dfd331f5e2/azure-eventhubs-eph
    private void initEventHub() {
        String pid = ManagementFactory.getRuntimeMXBean().getName();
        pid = pid.indexOf('@') == -1 ? pid : pid.substring(0, pid.indexOf('@'));
        log.info("Creating Azure EventHub consumer with pid {}.", pid);
        ConnectionStringBuilder eventHubConnectionString = new ConnectionStringBuilder(namespaceName, eventHubName, sasKeyName, sasKey);

        String storageConnectionString =
            "DefaultEndpointsProtocol=http;" +
            "AccountName=" + storageAccount +
            ";AccountKey=" + storageAccountKey;
        
        EventProcessorHost host = new EventProcessorHost(EventProcessorHost.createHostName("zombieProcessor"),
            eventHubName, "$Default", eventHubConnectionString.toString(), storageConnectionString, storageContainer);


        //EventProcessorOptions options = EventProcessorOptions.getDefaultOptions();
        //options.setExceptionNotification(new ErrorNotificationHandler());
        try {
            host.registerEventProcessorFactory(zombieEventFactory).get();
        } catch (Exception e) {
            System.out.print("Failure while registering: ");
            if (e instanceof ExecutionException) {
                Throwable inner = e.getCause();
                log.error("Exectution Exception: {}",inner.toString());
            } else {
                log.error("Other error: {}", e.toString());
            }
        }

    }

}
