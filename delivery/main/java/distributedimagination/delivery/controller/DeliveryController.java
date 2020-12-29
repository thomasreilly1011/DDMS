package distributedimagination.delivery;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Applications;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.ws.rs.GET;
import java.util.ArrayList;
import java.util.List;
import java.net.URI;
import java.net.URISyntaxException;


@SpringBootApplication
@RestController
public class QuotationController {

    @Autowired
    private EurekaClient eurekaClient;

    @RequestMapping(value = "/service-instances/list")
    public ArrayList<String> getApplications() {
        ArrayList<String> serviceInstances = new ArrayList<String>();
        List<Application> applications = eurekaClient.getApplications().getRegisteredApplications();
        for (Application application : applications) {
            for (InstanceInfo instance : application.getInstances()) {
                if (instance.getAppName().contains("POSTAL-SERVICE-"))
                    serviceInstances.add(instance.getHomePageUrl());
            }
        }
        return serviceInstances;
    }

    @RequestMapping(value = "/service-instances/delivery")
    public ArrayList<String> getDeliveryList() throws URISyntaxException {
        ArrayList<String> delivery = new ArrayList<String>();
        delivery.add("Test Delivery");
        ArrayList<String> serviceInstances = getApplications();
        Iterator<String> iterator = serviceInstances.iterator();
        while (iterator.hasNext()) {
            String appURL = iterator.next();
            final String baseUrl = appURL + "/deliveryDate";
            URI uri = new URI(baseUrl);
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<String> requestEntity = new HttpEntity<>(null);
            ResponseEntity<String> result = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, String.class);
            delivery.add(result.toString());
        }
        return delivery;
    }
}
