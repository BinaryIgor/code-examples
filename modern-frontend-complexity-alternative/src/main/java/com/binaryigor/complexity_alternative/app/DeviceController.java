package com.binaryigor.complexity_alternative.app;

import com.binaryigor.complexity_alternative.domain.AvailableAttribute;
import com.binaryigor.complexity_alternative.domain.Device;
import com.binaryigor.complexity_alternative.domain.DeviceNotFoundException;
import com.binaryigor.complexity_alternative.domain.DevicesException;
import com.binaryigor.complexity_alternative.infra.DeviceOfferRepository;
import com.binaryigor.complexity_alternative.infra.DeviceRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class DeviceController {

    private final DeviceRepository deviceRepository;
    private final DeviceOfferRepository deviceOfferRepository;
    private final TemplatesResolver templatesResolver;
    private final Translations translations;

    public DeviceController(DeviceRepository deviceRepository, DeviceOfferRepository deviceOfferRepository,
                            TemplatesResolver templatesResolver, Translations translations) {
        this.deviceRepository = deviceRepository;
        this.deviceOfferRepository = deviceOfferRepository;
        this.templatesResolver = templatesResolver;
        this.translations = translations;
    }

    @GetMapping("/devices")
    public String devices(Model model, Locale locale,
                          @RequestParam(required = false) String search) {
        translations.enrich(model, locale, Map.of("devices-page.title", "title"),
                "devices-page.title",
                "devices-page.search-input-placeholder",
                "devices-page.search-indicator",
                "devices-page.trigger-error-button");

        enrichWithDevicesSearchResultsTranslations(model, locale);

        var devices = deviceRepository.devices(search);

        return templatesResolver.resolve("devices-page",
                devicesModel(model, devices));
    }

    private void enrichWithDevicesSearchResultsTranslations(Model model, Locale locale) {
        translations.enrich(model, locale,
                "devices-search-results.no-results",
                "devices-search-results.details-option",
                "devices-search-results.buy-option");
    }

    private Model devicesModel(Model model, List<Device> devices) {
        return model.addAttribute("devices", devices)
                .addAttribute("no-results", devices.isEmpty());
    }

    @GetMapping("/search-devices")
    public String searchDevices(Model model, Locale locale, @RequestParam(required = false) String search) {
        var devices = deviceRepository.devices(search);
        enrichWithDevicesSearchResultsTranslations(model, locale);
        // simulate slower search for UI experiments
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return templatesResolver.resolve("devices-search-results", devicesModel(model, devices));
    }

    @GetMapping("/devices/{id}/offers")
    public String deviceOffers(@PathVariable UUID id,
                               @RequestParam MultiValueMap<String, String> params,
                               Model model) {
        var availableAttributes = deviceOfferRepository.availableAttributes(id).stream()
                .map(AvailableAttribute::key)
                .collect(Collectors.toSet());

        var searchRequestAttributes = params.entrySet().stream()
                .filter(e -> availableAttributes.contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        var sortingColumn = Optional.ofNullable(params.getFirst("sortingColumn"))
                .map(v -> DeviceOfferRepository.SortingColumn.valueOf(v.toUpperCase()))
                .orElse(DeviceOfferRepository.SortingColumn.PRICE);

        var ascendingSortingDirection = Optional.ofNullable(params.getFirst("sortingDirection"))
                .map(d -> d.equalsIgnoreCase("ascending"))
                .orElse(true);

        var deviceOffers = deviceOfferRepository.offers(new DeviceOfferRepository.SearchRequest(id, searchRequestAttributes,
                sortingColumn, ascendingSortingDirection));
        return templatesResolver.resolve("device-offers",
                model.addAttribute("offers", deviceOffers)
                        .addAttribute("hasOffers", !deviceOffers.isEmpty()));
    }

    @PostMapping("/devices/trigger-error")
    void triggerError() {
        throw new DevicesException("Triggered devices error");
    }

    @GetMapping("/devices/{id}")
    public String device(@PathVariable UUID id, Model model, Locale locale) {
        var device = deviceRepository.ofId(id)
                .orElseThrow(() -> new DeviceNotFoundException(id));

        translations.enrich(model, locale, Map.of("device-page.title", "title"),
                "device-page.title", "device-page.page-description");

        return templatesResolver.resolve("device-page",
                model.addAttribute("id", device.id())
                        .addAttribute("name", device.name()));
    }

    @GetMapping("/buy-device/{id}")
    public String buyDevice(@PathVariable UUID id, Locale locale, Model model) {
        translations.enrich(model, locale, Map.of("buy-device-page.title", "title"),
                "buy-device-page.title",
                "buy-device-page.no-offers",
                "buy-device-page.attributes-column",
                "buy-device-page.price-column",
                "buy-device-page.merchant-column",
                "buy-device-page.expires-at-column",
                "buy-device-page.actions-column",
                "buy-device-page.actions.buy");

        var device = deviceRepository.ofId(id)
                .orElseThrow(() -> new DeviceNotFoundException(id));
        var availableAttributes = deviceOfferRepository.availableAttributes(id).stream()
                .map(a -> toTranslatedAvailableAttribute(a, locale))
                .toList();
        var deviceOffers = deviceOfferRepository.offers(new DeviceOfferRepository.SearchRequest(id));

        var attributeNames = availableAttributes.stream()
                .map(TranslatedAvailableAttribute::name)
                .toList();

        return templatesResolver.resolve("buy-device-page",
                model.addAttribute("id", device.id())
                        .addAttribute("name", device.name())
                        .addAttribute("availableAttributes", availableAttributes)
                        .addAttribute("offers", deviceOffers)
                        .addAttribute("hasOffers", !deviceOffers.isEmpty())
                        .addAttribute("attributeNames", attributeNames));
    }

    private TranslatedAvailableAttribute toTranslatedAvailableAttribute(AvailableAttribute attribute, Locale locale) {
        var name = translations.translate("device-attributes." + attribute.key(), attribute.key(), locale);
        return TranslatedAvailableAttribute.translated(attribute, name);
    }

    @GetMapping("/buy-device/{deviceId}/offers/{offerId}")
    public String buyDeviceOffer(@PathVariable UUID deviceId, @PathVariable UUID offerId,
                                 Locale locale, Model model) {
        // TODO: better exception & check deviceId with offerId alignment
        var deviceOffer = deviceOfferRepository.ofId(offerId).orElseThrow();
        var device = deviceRepository.ofId(deviceOffer.deviceId()).orElseThrow();

        translations.enrich(model, locale,
                Map.of("buy-device-offer-page.title", "title"),
                Map.of("buy-device-offer-page.merchant-offer",
                        Map.of("__merchant__", deviceOffer.merchantId().toString())),
                "buy-device-offer-page.title",
                "buy-device-offer-page.merchant-offer",
                "buy-device-offer-page.user-offer",
                "buy-device-offer-page.user-offer-validation-error",
                "buy-device-offer-page.send-button");

        return templatesResolver.resolve("buy-device-offer-page",
                model.addAttribute("deviceName", device.name())
                        .addAttribute("offerPrice", deviceOffer.price()));
    }
}
