package com.binaryigor.complexity_alternative;

import com.binaryigor.complexity_alternative.domain.Device;
import com.binaryigor.complexity_alternative.domain.DeviceOffer;
import com.binaryigor.complexity_alternative.domain.Money;
import com.binaryigor.complexity_alternative.infra.DeviceOfferRepository;
import com.binaryigor.complexity_alternative.infra.DeviceRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

@Component
public class AppDataInitializer implements InitializingBean {

    private final DeviceRepository deviceRepository;
    private final DeviceOfferRepository deviceOfferRepository;
    private final Clock clock;

    public AppDataInitializer(DeviceRepository deviceRepository, DeviceOfferRepository deviceOfferRepository,
                              Clock clock) {
        this.deviceRepository = deviceRepository;
        this.deviceOfferRepository = deviceOfferRepository;
        this.clock = clock;
    }

    // TODO: more data
    @Override
    public void afterPropertiesSet() throws Exception {
        var merchant1Id = UUID.randomUUID();
        var merchant2Id = UUID.randomUUID();
        var merchant3Id = UUID.randomUUID();

        var now = clock.instant().truncatedTo(ChronoUnit.SECONDS);

        var iphone13 = new Device(UUID.fromString("9b0d5f33-6f9e-4aef-bb81-a57a045fb1aa"), "iPhone 13");
        deviceRepository.save(iphone13);
        deviceOfferRepository.save(new DeviceOffer(
                UUID.fromString("1b70aa73-29fd-41a1-b220-34ebba7a9c40"),
                iphone13.id(),
                Map.of("color", "black",
                        "memory", "8 GB",
                        "storage", "256 GB"),
                Money.euro("1000.00"),
                merchant1Id,
                now.plus(Duration.ofDays(7))));
        deviceOfferRepository.save(new DeviceOffer(
                UUID.fromString("4339e313-4391-48f3-bd7c-472b377aa19c"),
                iphone13.id(),
                Map.of("color", "black",
                        "memory", "12 GB",
                        "storage", "256 GB"),
                Money.euro("1500.00"),
                merchant1Id,
                now.plus(Duration.ofDays(6))));
        deviceOfferRepository.save(new DeviceOffer(
                UUID.fromString("0b14a01e-bfb4-4d91-bc57-b812114f75c8"),
                iphone13.id(),
                Map.of("color", "blue",
                        "memory", "8 GB",
                        "storage", "256 GB"),
                Money.euro("900.00"),
                merchant2Id,
                now.plus(Duration.ofDays(3))));
        deviceOfferRepository.save(new DeviceOffer(
                UUID.fromString("0b14a01e-bfb4-4d91-bc57-b812114f75c8"),
                iphone13.id(),
                Map.of("color", "midnight",
                        "memory", "8 GB",
                        "storage", "256 GB"),
                Money.euro("1000.00"),
                merchant2Id,
                now.plus(Duration.ofDays(4))));

        var iphone15 =  new Device(UUID.fromString("11bdd7e6-a5e8-4d0f-a7cd-5f8074483e37"), "iPhone 15");
        deviceRepository.save(iphone15);

        var iphone17 =  new Device(UUID.fromString("0cd8d327-604b-4ffd-9e43-08497ed99d2c"), "iPhone 17");
        deviceRepository.save(iphone17);

        var thinkPadT16 =  new Device(UUID.fromString("73aad6b7-8bf1-4346-b833-3767997205db"), "Lenovo ThinkPad T16");
        deviceRepository.save(thinkPadT16);

        var thinkPadT14 =  new Device(UUID.fromString("94a0d28e-3e7e-47ac-ac81-3cf38bff18a8"), "Lenovo ThinkPad T14");
        deviceRepository.save(thinkPadT14);
    }
}
