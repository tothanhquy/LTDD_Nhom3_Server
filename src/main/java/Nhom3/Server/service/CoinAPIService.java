package Nhom3.Server.service;

import Nhom3.Server.model.FetchCoinsAPIModel;
import com.google.gson.Gson;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;


@Service
public class CoinAPIService {
    private final String COINS_VALUE_NOW_API_URL = "https://api.coincap.io/v2/assets";
    private final String COINS_VALUE_HISTORY_API_URL = "https://api.coincap.io/v2/assets/{{CoinId}}/history?interval={{Interval}}&start={{Start}}&end={{End}}";
//    private final String COINS_VALUE_HISTORY_API_URL_EXAMPLE = "https://api.coincap.io/v2/assets/bitcoin/history?interval=d1&start=1674950400000&end=1784973000000";
//    https://assets.coincap.io/assets/icons/btc@2x.png
    private final RestTemplate restTemplate;

    public CoinAPIService() {
        this.restTemplate = new RestTemplate();
    }

    public FetchCoinsAPIModel.CoinsNow getCoinsNow() {
        // Make a GET request to the API
        String str = restTemplate.getForObject(COINS_VALUE_NOW_API_URL, String.class);
        FetchCoinsAPIModel.CoinsNowCrude crude = new Gson().fromJson(str,FetchCoinsAPIModel.CoinsNowCrude.class);
        FetchCoinsAPIModel parent = new FetchCoinsAPIModel();
        FetchCoinsAPIModel.CoinsNow res = parent.new CoinsNow(crude);
        return res;
    }

    public static ArrayList<String> InternalsValid = new ArrayList<>(Arrays.asList("m1","m5","m15","m30","h1","h2","h6","h12","d1"));
    public FetchCoinsAPIModel.CoinsM1History getCoinsM1ToH12History(String idCoin, String interval, long start, long end) {
        try{
            if(!InternalsValid.contains(interval))return null;

            long M1 = 1000*60;
            long M5 = M1*5;
            long M15 = M5*3;
            long M30 = M15*2;
            long H1 = M30*2;
            long H2 = H1*2;
            long H6 = H2*3;
            long H12 = H6*2;

            int maxDays;
            long startTime;
            long endTime;
            long duringDays;

            if(interval.equals("m1")){
                maxDays = FetchCoinsAPIModel.COIN_M1_HISTORY_MAX_DAY;
                startTime = start - (start%M1);
                endTime = end - (end%M1);
            }else if(interval.equals("m5")){
                maxDays = FetchCoinsAPIModel.COIN_M5_HISTORY_MAX_DAY;
                startTime = start - (start%M5);
                endTime = end - (end%M5);
            }else if(interval.equals("m15")){
                maxDays = FetchCoinsAPIModel.COIN_M15_HISTORY_MAX_DAY;
                startTime = start - (start%M15);
                endTime = end - (end%M15);
            }else if(interval.equals("m30")){
                maxDays = FetchCoinsAPIModel.COIN_M30_HISTORY_MAX_DAY;
                startTime = start - (start%M30);
                endTime = end - (end%M30);
            }else if(interval.equals("h1")){
                maxDays = FetchCoinsAPIModel.COIN_H1_HISTORY_MAX_DAY;
                startTime = start - (start%H1);
                endTime = end - (end%H1);
            }else if(interval.equals("h2")){
                maxDays = FetchCoinsAPIModel.COIN_H2_HISTORY_MAX_DAY;
                startTime = start - (start%H2);
                endTime = end - (end%H2);
            }else if(interval.equals("h6")){
                maxDays = FetchCoinsAPIModel.COIN_H6_HISTORY_MAX_DAY;
                startTime = start - (start%H6);
                endTime = end - (end%H6);
            }else{
                maxDays = FetchCoinsAPIModel.COIN_H12_HISTORY_MAX_DAY;
                startTime = start - (start%H12);
                endTime = end - (end%H12);
            }
            duringDays = (endTime-startTime)/(1000*60*60*24);

            if(duringDays<=maxDays){
                //not split
                String url = COINS_VALUE_HISTORY_API_URL.replace("{{CoinId}}",idCoin).replace("{{Interval}}",interval).replace("{{Start}}",startTime+"").replace("{{End}}",endTime+"");
                String str = restTemplate.getForObject(url, String.class);
                FetchCoinsAPIModel.CoinsM1HistoryCrude crude = new Gson().fromJson(str,FetchCoinsAPIModel.CoinsM1HistoryCrude.class);
                FetchCoinsAPIModel.CoinsM1History res = new FetchCoinsAPIModel.CoinsM1History(crude);
                return res;
            }else{
                //split day
                FetchCoinsAPIModel.CoinsM1History res = new FetchCoinsAPIModel.CoinsM1History();
                for (int i = 0; i < duringDays/maxDays; i++) {
                    long urlStart = startTime+i;
                    long urlEnd = urlStart+ (long) maxDays *1000*60*60*24;
                    String url = COINS_VALUE_HISTORY_API_URL.replace("{{CoinId}}",idCoin).replace("{{Interval}}",interval).replace("{{Start}}",urlStart+"").replace("{{End}}",urlEnd+"");
                    String str = restTemplate.getForObject(url, String.class);
                    FetchCoinsAPIModel.CoinsM1HistoryCrude crude = new Gson().fromJson(str,FetchCoinsAPIModel.CoinsM1HistoryCrude.class);
                    FetchCoinsAPIModel.CoinsM1History temp = new FetchCoinsAPIModel.CoinsM1History(crude);
                    if(!res.data.isEmpty()){
                        if(res.data.get(res.data.size()-1).time==temp.data.get(0).time){
                            res.data.remove(res.data.size()-1);
                        }
                    }
                    res.data.addAll(temp.data);
                }
                return res;
            }
        }catch(Exception e){
            return null;
        }

    }
    public FetchCoinsAPIModel.CoinsD1History getCoinsD1History(String idCoin, long start, long end) {
        try{
            String url = COINS_VALUE_HISTORY_API_URL.replace("{{CoinId}}",idCoin).replace("{{Interval}}","d1").replace("{{Start}}",start+"").replace("{{End}}",end+"");
            String str = restTemplate.getForObject(url, String.class);
            FetchCoinsAPIModel.CoinsD1HistoryCrude crude = new Gson().fromJson(str,FetchCoinsAPIModel.CoinsD1HistoryCrude.class);
            FetchCoinsAPIModel.CoinsD1History res = new FetchCoinsAPIModel.CoinsD1History(crude);
            return res;
        }catch(Exception e){
            return null;
        }

    }



}
