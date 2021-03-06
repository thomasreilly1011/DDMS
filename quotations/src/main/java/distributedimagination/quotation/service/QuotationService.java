package distributedimagination.quotation.service;

import com.google.gson.*;
import distributedimagination.quotation.entity.OrderQuery;
import distributedimagination.quotation.entity.ParcelQuery;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

@Service
public class QuotationService {


    public Map<String, String> getQuotes() {
        final String uri = "http://discovery:8761/postal-services/urls";
        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> map;
        map = restTemplate.getForObject(uri, Map.class);

        return map;
    }


    public String GenerateQuote(JsonObject jsonObject) {

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

        ArrayList<Map<String, String>> quotes = getQuotationsList(orderQuery);

        Gson gson = new Gson();
        String jsArray = gson.toJson(quotes);

        return jsArray;
    }

    /*loops through all postal services, and sends a post request to each using gson to convert a OrderQuery object to
    JSON. The response is then parsed to a JSON Object and returns the name of the postal service and the cost field*/
    public ArrayList<Map<String, String>> getQuotationsList(OrderQuery orderQuery) {
        Map<String, String> map = getQuotes();
        ArrayList<Map<String, String>> quotations = new ArrayList<>();
        Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            String name = entry.getKey();
            String appURL = entry.getValue() + "quote";
            Gson gson = new Gson();
            String json = gson.toJson(orderQuery);
            HttpEntity<String> request = new HttpEntity<String>(json, headers);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(appURL, request, String.class);
            JsonObject jo = (JsonObject) JsonParser.parseString(responseEntity.getBody());
            HashMap<String, String> obj = new HashMap<>();
            obj.put("serviceID", jo.get("serviceId").toString().replace("\"", ""));
            obj.put("providerName", name);
            obj.put("price", jo.get("cost").toString());
            obj.put("expectedDate", jo.get("dateExpected").toString().replace("\"", ""));
            quotations.add(obj);
        }
        return quotations;
    }

}