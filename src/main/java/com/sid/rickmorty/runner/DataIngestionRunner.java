package com.sid.rickmorty.runner;

import com.sid.rickmorty.service.LocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Simple scheduled ingester. Automatically refreshes so the demo always has data.
 */
@Component
@ConditionalOnProperty(prefix = "rickmorty.ingestion", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DataIngestionRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataIngestionRunner.class);
    private final LocationService locationService;

    public DataIngestionRunner(LocationService locationService) {
        this.locationService = locationService;
    }

    @Override
    public void run(String... args) {
        log.info("Seeding location cache");
        locationService.refreshFromSource();
    }

    @Scheduled(fixedDelayString = "${rickmorty.ingestion.refresh-interval:PT30M}", initialDelayString = "${rickmorty.ingestion.initial-delay:PT30S}")
    public void refresh() {
        log.info("Scheduled refresh triggered");
        locationService.refreshFromSource();
    }
}
