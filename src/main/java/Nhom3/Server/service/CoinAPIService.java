package Nhom3.Server.service;

import Nhom3.Server.model.FetchCoinsAPIModel;
import com.google.gson.Gson;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CoinAPIService {
    private final String apiUrl = "https://api.coincap.io/v2/assets";

    private final RestTemplate restTemplate;

    public CoinAPIService() {
        this.restTemplate = new RestTemplate();
    }

    public FetchCoinsAPIModel.MainResponse fetchDataFromApi() {
        // Make a GET request to the API
        String str = restTemplate.getForObject(apiUrl, String.class);
        FetchCoinsAPIModel.MainResponseCrude crude = new Gson().fromJson(str,FetchCoinsAPIModel.MainResponseCrude.class);
        FetchCoinsAPIModel parent = new FetchCoinsAPIModel();
        FetchCoinsAPIModel.MainResponse res = parent.new MainResponse(crude);
        return res;
    }
}
