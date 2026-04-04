package com.binaryigor.complexity_alternative.infra;

import com.binaryigor.complexity_alternative.domain.AvailableAttribute;
import com.binaryigor.complexity_alternative.domain.DeviceOffer;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class DeviceOfferRepository {

    private final Map<UUID, List<DeviceOffer>> deviceIdToOfferDb = new ConcurrentHashMap<>();
    private final Map<UUID, DeviceOffer> offerIdToOfferDb = new ConcurrentHashMap<>();

    public void save(DeviceOffer offer) {
        deviceIdToOfferDb.computeIfAbsent(offer.deviceId(), $ -> new ArrayList<>()).add(offer);
        offerIdToOfferDb.put(offer.id(), offer);
    }

    public List<AvailableAttribute> availableAttributes(UUID deviceId) {
        var attributes = new HashMap<String, Collection<String>>();
        deviceIdToOfferDb.getOrDefault(deviceId, List.of())
                .forEach(o -> {
                    o.attributes().forEach((attrK, attrV) -> {
                        attributes.computeIfAbsent(attrK, $ -> new HashSet<>()).add(attrV);
                    });
                });
        return attributes.entrySet().stream()
                .map(e -> new AvailableAttribute(e.getKey(), e.getValue()))
                .toList();
    }

    public List<DeviceOffer> offers(SearchRequest request) {
        return deviceIdToOfferDb.getOrDefault(request.deviceId, List.of()).stream()
                .filter(d -> {
                    for (var e : request.attributes().entrySet()) {
                        var key = e.getKey();
                        var values = e.getValue();
                        var deviceValue = d.attributes().get(key);
                        if (deviceValue == null || !values.contains(deviceValue)) {
                            return false;
                        }
                    }
                    return true;
                })
                .sorted((a, b) -> {
                    Comparator<DeviceOffer> comparator = switch (request.sortingColumn) {
                        case PRICE -> Comparator.comparing(o -> o.price().value());
                        case MERCHANT -> Comparator.comparing(DeviceOffer::merchantId);
                        case EXPIRES_AT -> Comparator.comparing(DeviceOffer::expiresAt);
                    };
                    return request.sortingAscending ? comparator.compare(a, b) : -comparator.compare(a, b);
                })
                .toList();
    }

    public Optional<DeviceOffer> ofId(UUID id) {
        return Optional.ofNullable(offerIdToOfferDb.get(id));
    }

    public record SearchRequest(UUID deviceId,
                                Map<String, List<String>> attributes,
                                SortingColumn sortingColumn,
                                boolean sortingAscending) {

        public SearchRequest(UUID deviceId) {
            this(deviceId, null, null, true);
        }

        public SearchRequest {
            if (attributes == null) {
                attributes = Map.of();
            }
            if (sortingColumn == null) {
                sortingColumn = SortingColumn.PRICE;
            }
        }
    }

    public enum SortingColumn {
        PRICE, MERCHANT, EXPIRES_AT
    }

}
