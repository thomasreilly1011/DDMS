package distributedimagination.tracking.controller;

import distributedimagination.tracking.service.TrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
public class TrackingController {

    private final TrackingService trackingService;

    @Autowired
    public TrackingController(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @RequestMapping(value = "/service-instances/tracking/list")
    public Map<String, String> returnMap() {
        return trackingService.getLocations();
    }

    @RequestMapping(value = "/service-instances/tracking")
    public ArrayList<String> getTrackingList() {
        return trackingService.getTrackingList();
    }
}