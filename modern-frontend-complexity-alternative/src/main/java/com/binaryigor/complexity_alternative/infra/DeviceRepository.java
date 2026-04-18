package com.binaryigor.complexity_alternative.infra;

import com.binaryigor.complexity_alternative.domain.Device;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class DeviceRepository {

    private final Map<UUID, Device> db = new ConcurrentHashMap<>();

    public void save(Device device) {
        db.put(device.id(), device);
    }

    public List<Device> all() {
        return db.values().stream().toList();
    }

    public List<Device> devices(String search) {
        if (search == null || search.isBlank()) {
            return db.values().stream().toList();
        }
        var loweredSearch = search.toLowerCase();
        return db.values().stream()
                .filter(d -> d.name().toLowerCase().contains(loweredSearch))
                .toList();
    }

    public List<Device> allDevices() {
        return devices(null);
    }

    public Optional<Device> ofId(UUID id) {
        return Optional.ofNullable(db.get(id));
    }
}
