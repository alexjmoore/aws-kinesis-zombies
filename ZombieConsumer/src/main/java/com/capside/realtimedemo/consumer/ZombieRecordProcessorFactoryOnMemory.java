package com.capside.realtimedemo.consumer;

import com.microsoft.azure.eventprocessorhost.IEventProcessor;
import com.microsoft.azure.eventprocessorhost.IEventProcessorFactory;
import com.microsoft.azure.eventprocessorhost.PartitionContext;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 *
 * @author ciberado
 * @author alexjmoore
 */
@Component
@Slf4j
public class ZombieRecordProcessorFactoryOnMemory implements IEventProcessorFactory {
    
    private final Set<ZombieLecture> lectures = new HashSet<>();
    
    @Override
    public IEventProcessor createEventProcessor(PartitionContext context) throws Exception {
        return new ZombieRecordProcessorOnMemory(lectures);
    }

    public Set<ZombieLecture> getLectures() {
        return lectures;
    }
    
    
}
