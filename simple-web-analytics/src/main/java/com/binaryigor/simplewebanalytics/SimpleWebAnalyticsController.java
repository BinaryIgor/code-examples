package com.binaryigor.simplewebanalytics;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/analytics")
public class SimpleWebAnalyticsController {

    private static final Logger logger = LoggerFactory.getLogger(SimpleWebAnalyticsApp.class);
    private final UserAuth userAuth;
    private final Clock clock;

    public SimpleWebAnalyticsController(UserAuth userAuth, Clock clock) {
        this.userAuth = userAuth;
        this.clock = clock;
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/events")
    void addEvent(@RequestHeader(name = "device-id") UUID deviceId,
                  // use this header, if you host it behind reverse proxy of some sorts
                  @RequestHeader(required = false, name = "real-ip") String realIp,
                  @RequestBody AnalyticsEventRequest eventRequest,
                  HttpServletRequest httpRequest) {
        try {
            var clientIp = Optional.ofNullable(realIp)
                .orElseGet(httpRequest::getRemoteAddr);

            var event = eventRequest.toEvent(clock.instant(), clientIp, deviceId, userAuth.currentUserId().orElse(null));

            logger.info("Should save an event: {}", event);
        } catch (Exception e) {
            // this endpoint must be public, and we don't want to show failures to unknown clients
            logger.error("Problem while handling analytics event: ", e);
        }
    }
}
