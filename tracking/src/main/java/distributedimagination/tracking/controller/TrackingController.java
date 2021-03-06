package distributedimagination.tracking.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import distributedimagination.tracking.service.TrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@CrossOrigin(origins = "*")
public class TrackingController {

    private final TrackingService trackingService;

    @Autowired
    public TrackingController(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @RequestMapping(value = "/delivery-providers")
    public Map<String, String> returnMap() {
        return trackingService.getPostalIDs();
    }

    @RequestMapping(value = "/request-tracking/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> TrackingList(@PathVariable("id")  String id) {
        return trackingService.getTracking(id);
    }
}