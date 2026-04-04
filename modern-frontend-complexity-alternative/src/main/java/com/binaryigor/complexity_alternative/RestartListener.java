package com.binaryigor.complexity_alternative;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class RestartListener {

    @EventListener
    void onRestartEvent(ContextRefreshedEvent event) {
        System.out.println("Restart triggered!");
        System.out.println(event);
    }
}
