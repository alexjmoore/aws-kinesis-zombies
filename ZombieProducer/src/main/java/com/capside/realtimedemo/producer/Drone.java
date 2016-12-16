/*
    Derived from: https://github.com/capside/aws-kinesis-zombies
 */
package com.capside.realtimedemo.producer;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

import com.microsoft.azure.eventhubs.*;
import com.microsoft.azure.servicebus.*;
import com.capside.realtimedemo.geo.CoordinateLatLon;
import com.capside.realtimedemo.geo.CoordinateUTM;
import com.capside.realtimedemo.geo.Datum;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import static java.lang.String.format;

/**
 * Aditional documentation: https://github.com/Azure/azure-event-hubs-java
 * 
 * @author ciberado
 * @author alexjmoore
 */
@Component
@Slf4j
public class Drone implements CommandLineRunner {

    public static final int NUMBER_OF_ZOMBIES = 1000;
    public static final double ZOMBIE_SPEED = 1;
    public static final double RADIOUS = 3 * 1000;
    public static final int MAX_OUTSTANDING = 2000;

    private final ObjectMapper mapper;

    private final int id;
    private final String namespaceName;
    private final String eventHubName;
    private final String sasKey;
    private final String sasKeyName;
    private final double latitude;
    private final double longitude;
    private final List<Zombie> zombies = new ArrayList<>();
    
    /**
     * The sequence number of the next record.
     */
    private final AtomicLong currentRecordNumber;
    /**
     * How many records have been confirmed to be successfully sent.
     */
    private final AtomicLong recordsCompleted;

    private EventHubClient ehClient;

    @Autowired
    public Drone(@Value("${drone}") int droneId,
            @Value("${ns}") String ns,
            @Value("${hub}") String hub,
            @Value("${keyname}") String keyname,
            @Value("${key}") String key,
            @Value("${latitude}") double latitude,
            @Value("${longitude}") double longitude) {
        // --drone=7777 --latitude=41.3902 --longitude=2.15400
        // --drone=5555 --latitude=40.415363 --longitude=-3.707398
        this.mapper = new ObjectMapper();
        this.id = droneId;
        this.namespaceName = ns;
        this.eventHubName = hub;
        this.sasKeyName = keyname;
        this.sasKey = key;
        this.latitude = latitude;
        this.longitude = longitude;
        this.currentRecordNumber = new AtomicLong(0);
        this.recordsCompleted = new AtomicLong(0);
    }

    @SneakyThrows
    @Override
    public void run(String... args) {
        initZombies(latitude, longitude, RADIOUS);
        
        ConnectionStringBuilder connStr = new ConnectionStringBuilder(namespaceName, eventHubName, sasKeyName, sasKey);
        log.info(format("HERE: %s", namespaceName));
        log.info(format("Connecting to Azure EventHub: %s", connStr));
        ehClient = createEhClient(connStr);
        while (true) {
            long t0 = System.currentTimeMillis();
            for (Zombie zombie : zombies) {
                zombie.move();
            }
            //while (producer.getOutstandingRecordsCount() > MAX_OUTSTANDING) {
            //   log.warn(format("Kinesis KPL is under pressure (count=%s). Waiting 1 second.", 
            //            producer.getOutstandingRecordsCount()));
            //    Thread.sleep(1000);
            //}
            sendZombiesToEventHub();
            long tf = System.currentTimeMillis();
            log.info("Main loop time: " + (tf-t0));
            if (tf - t0 < 1000) {
                Thread.sleep(1000 - (tf -t0));
            }
        }
    }

    /**
     * For reference:
     * https://github.com/Azure/azure-event-hubs-java
     * @return EventHubClient instance used to send records.
     */
    public EventHubClient createEhClient(ConnectionStringBuilder connStr) throws ServiceBusException, java.io.IOException {
        
        EventHubClient client = EventHubClient.createFromConnectionStringSync(connStr.toString());

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                log.info("Flushing remaining records.");
                //TODO - possibly track all outstanding threads and ensure they completed?
                log.info("All records flushed.");
            }
        }) {
        });

        return client;
    }


    private void initZombies(double latitude, double longitude, double radious) {
        for (int i=0; i < NUMBER_OF_ZOMBIES; i++) {
            CoordinateUTM utm = Datum.WGS84.latLonToUTM(latitude, longitude, -1);
            double angle = 2 * PI * Math.random();
            double distance = radious * Math.random();
            double dx = distance * cos(angle);
            double dy = distance * sin(angle);
            utm.translate(dx, dy);
            Zombie zombie = new Zombie(id + "-" + i, utm, ZOMBIE_SPEED * 0.5 * (1+Math.random()));
            zombies.add(zombie);
        }
    }
    
    public void sendZombiesToEventHub() {
        for (Zombie zombie : zombies) {
            putNewRecord(zombie);
        }
    }


    @SneakyThrows
    public void putNewRecord(Zombie zombie) {        
        CoordinateUTM utm = zombie.getCurrentPosition();
        CoordinateLatLon latLon = Datum.WGS84.utmToLatLon(utm);
        ZombieLecture lect = new ZombieLecture(id, zombie.getId(), new Date(), latLon.getLat(), latLon.getLon());
        utm.setAccuracy(RADIOUS);
        String partitionKey = utm.getShortForm();
        String json = mapper.writeValueAsString(lect);
        ByteBuffer data = ByteBuffer.wrap(json.getBytes("UTF-8"));

        EventData sendEvent = new EventData(data);
        ehClient.send(sendEvent, partitionKey).thenApplyAsync( result -> { 
            //log.info(format("RECORDS: %s;", recordsCompleted.get()));
            recordsCompleted.getAndIncrement();
            if (recordsCompleted.get() % NUMBER_OF_ZOMBIES == 0) {
                log.info(format("Records completed: %s;",
                            recordsCompleted.get()));
            }
            return null; 
        }).exceptionally( failedresult -> {
            //if (t instanceof UserRecordFailedException ){
            //    Attempt last = Iterables.getLast(
            //            ((UserRecordFailedException) t).getResult().getAttempts());
            //    log.error(format(
            //            "Record failed to put - %s : %s",
            //            last.getErrorCode(), last.getErrorMessage()));
            //}
            log.error("Exception during put", failedresult);
            return null; 
        });         
    }
}