package com.capside.realtimedemo.consumer;

import com.microsoft.azure.eventprocessorhost.*;
import com.microsoft.azure.eventhubs.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.UnsupportedEncodingException;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessagingException;

/**
 *
 * @author ciberado
 * @author alexjmoore
 */
@Slf4j
abstract class ZombieRecordProcessor implements IEventProcessor {

    private final ObjectMapper mapper;
    private int processedEvents;

    public ZombieRecordProcessor() {
        this.processedEvents = 0;
        this.mapper = new ObjectMapper();
    }

    @Override
    public void onOpen(PartitionContext context) {
        log.info("Processing from the partition {}.", context.getPartitionId());
    }

    @Override
    @SneakyThrows
    public void onError(PartitionContext context, Throwable error) {
        log.error("Partition " + context.getPartitionId() + " got error " + error.toString());
    }

    @Override
    @SneakyThrows
    public void onEvents(PartitionContext context, Iterable<EventData> messages) throws Exception {
        //log.info("Retrieving records from Azure Event Hub.");
        for (EventData r : messages) {
            try {
                String json = new String(r.getBody(), "UTF-8");
                ZombieLecture lecture = mapper.readValue(json, ZombieLecture.class);
                this.processZombieLecture(lecture);
                log.debug(processedEvents++ + ": " + json);
                if (processedEvents % 1000 == 999) {
                    // Uncomment next line to keep track of the processed lectures. 
                    log.info("CHECKPOINT:" + processedEvents++ + ": " + json);
                    context.checkpoint(r);
                }
            } catch (Exception ex) {
                log.warn(ex.getMessage());
            }
        }
    }

    abstract void processZombieLecture(ZombieLecture lecture);

    @Override
    @SneakyThrows
    public void onClose(PartitionContext context, CloseReason reason) {
        log.info("Finished work: {}.", reason.toString());
    }

}
