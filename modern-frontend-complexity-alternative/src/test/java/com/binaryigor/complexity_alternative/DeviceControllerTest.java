package com.binaryigor.complexity_alternative;

import com.binaryigor.complexity_alternative.infra.DeviceRepository;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

public class DeviceControllerTest extends BaseIntegrationTest {

    @Autowired
    private DeviceRepository deviceRepository;

    @Test
    void rendersFullDevicesPage() {
        var allDevices = deviceRepository.allDevices();

        var response = testRestClient.get()
                .uri("/devices")
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        var document = Jsoup.parse(response.getBody());
        assertThat(document.select("html"))
                .isNotEmpty();

        var devicesElement = document.select("#devices");
        allDevices.forEach(device -> {
            assertThat(devicesElement.text())
                    .contains(device.id().toString())
                    .contains(device.name());

            var devicePageAttribute = "[hx-get=/devices/%s]".formatted(device.id());
            var buyDevicePageAttribute = "[hx-get=/buy-device/%s]".formatted(device.id());
            assertThat(devicesElement.select(devicePageAttribute))
                    .isNotEmpty();
            assertThat(devicesElement.select(buyDevicePageAttribute))
                    .isNotEmpty();
        });
    }
}
