package distributedimagination.delivery.service;

import com.google.gson.*;
import distributedimagination.delivery.entity.OrderQuery;
import distributedimagination.delivery.entity.ParcelQuery;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class DeliveryService {

    public Map<String, String> getDelivery() {
        final String uri = "http://discovery:8761/postal-services/id";
        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> map = restTemplate.getForObject(uri, Map.class);
        return map;
    }

    public String GenerateDelivery(String serviceID, JsonObject jsonObject) {
        JsonArray parcelArray = jsonObject.getAsJsonArray("parcels");
        ArrayList<ParcelQuery> parcelQueryList = new ArrayList<ParcelQuery>();

        //iterates through each parcel and parses each element
        for (JsonElement jsonElement : parcelArray) {
            JsonObject parcel = jsonElement.getAsJsonObject();
            ParcelQuery parcelQuery = new ParcelQuery(parcel.get("weightKg").getAsDouble(),
                    parcel.get("lengthCm").getAsDouble(), parcel.get("widthCm").getAsDouble(),
                    parcel.get("heightCm").getAsDouble());
            parcelQueryList.add(parcelQuery);
        }

        OrderQuery orderQuery = new OrderQuery(jsonObject.get("sourceLon").getAsDouble(),
                jsonObject.get("sourceLat").getAsDouble(), jsonObject.get("destinationLon").getAsDouble(),
                jsonObject.get("destinationLat").getAsDouble(), parcelQueryList);

        Map<String, String> deliveries = getDeliveryList(serviceID, orderQuery);

        Gson gson = new Gson();
        String jsObj = gson.toJson(deliveries);

        return jsObj;

    }

    public Map<String, String> getDeliveryList(String serviceID, OrderQuery orderQuery) {
        Map<String, String> map = getDelivery();
        HashMap<String, String> deliveries = new HashMap<>();
        Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            RestTemplate restTemplate = new RestTemplate();
            if (entry.getKey().equals(serviceID)) {

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

                String appURL = entry.getValue() + "orders";
                Gson gson = new Gson();
                String json = gson.toJson(orderQuery);
                HttpEntity<String> request = new HttpEntity<String>(json, headers);
                ResponseEntity<String> responseEntity = restTemplate.postForEntity(appURL, request, String.class);
                JsonObject jo = (JsonObject) JsonParser.parseString(responseEntity.getBody());
                String orderDate = jo.get("dateOrdered").toString().replace("\"", "");
                String expectedDate = jo.get("dateExpected").toString().replace("\"", "");
                String trackingID = jo.get("trackingId").toString().replace("\"", "");

                deliveries.put("orderDate", orderDate);
                deliveries.put("expectedDate", expectedDate);
                deliveries.put("trackingID", trackingID);

            }
        }
        return deliveries;
    }
}



