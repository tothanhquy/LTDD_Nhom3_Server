package Nhom3.Server.controller;

import Nhom3.Server.model.FetchCoinsAPIModel;
import Nhom3.Server.model.ResponseAPIModel;
import Nhom3.Server.model.client_response.CoinResponseModel;
import Nhom3.Server.service.CoinAPIService;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;


@RestController
@RequestMapping("/coins")
public class CoinsController {
    @Autowired
    CoinAPIService coinAPIService;
    @GetMapping("/chart/line")
    public ResponseAPIModel getLineChart(@RequestParam String coinId, @RequestParam String interval, @RequestParam String start, @RequestParam String end) {
        try{
            long startTime;
            long endTime;
            
            if(start.equals("init")){
                startTime = 0;
            }else{
                startTime = Long.parseLong(start);
            }
            if(end.equals("now")){
                endTime = System.currentTimeMillis();
            }else{
                endTime = Long.parseLong(end);
            }
            
            FetchCoinsAPIModel.CoinsM1History chartM1;
            FetchCoinsAPIModel.CoinsD1History chartD1;
            CoinResponseModel.CoinsHistoryLineChart coinsHistory = new CoinResponseModel.CoinsHistoryLineChart();
            
            if(interval.equals("d1")){
                chartD1 = coinAPIService.getCoinsD1History(coinId,startTime,endTime);
                if(chartD1==null){
                    throw new Exception();
                }
                coinsHistory.data.addAll(chartD1.data.stream().map((e)-> new CoinResponseModel.CoinHistoryLineChart(e.priceUsd,e.time)).toList());
            }else{
                chartM1 = coinAPIService.getCoinsM1ToH12History(coinId,interval,startTime,endTime);
                if(chartM1==null){
                    throw new Exception();
                }
                coinsHistory.data.addAll(chartM1.data.stream().map((e)-> new CoinResponseModel.CoinHistoryLineChart(e.priceUsd,e.time)).toList());
            }
            System.out.println(new Gson().toJson(coinsHistory));
            return new ResponseAPIModel(0,ResponseAPIModel.Status.Success,coinsHistory);
        }catch (Exception e){
            System.out.println(e);
            return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Lỗi hệ thống");
        }
    }
    @GetMapping("/chart/candle")
    public ResponseAPIModel getCandleChart(@RequestParam String coinId, @RequestParam String interval, @RequestParam String start, @RequestParam String end) {
        try{
            long startTime;
            long endTime;

            if(start.equals("init")){
                startTime = 0;
            }else{
                startTime = Long.parseLong(start);
            }
            if(end.equals("now")){
                endTime = System.currentTimeMillis();
            }else{
                endTime = Long.parseLong(end);
            }
            
            String subInterval;
            int indInterval = CoinAPIService.InternalsValid.indexOf(interval);
            if(indInterval==-1){
                throw new Exception();
            }
            if(indInterval>=3){
                subInterval = CoinAPIService.InternalsValid.get(indInterval-3);
            }else{
                subInterval = CoinAPIService.InternalsValid.get(0);
            }
            
            FetchCoinsAPIModel.CoinsM1History chartM1=coinAPIService.getCoinsM1ToH12History(coinId,subInterval,startTime,endTime);
            if(chartM1==null){
                throw new Exception();
            }
            CoinResponseModel.CoinsHistoryCandleChart coinsHistory = new CoinResponseModel.CoinsHistoryCandleChart();

            long M1 = 1000*60;
            long M5 = M1*5;
            long M15 = M5*3;
            long M30 = M15*2;
            long H1 = M30*2;
            long H2 = H1*2;
            long H6 = H2*3;
            long H12 = H6*2;
            long D1 = H12*2;
            
            long intervalTime;

            if(interval.equals("m1")){
                intervalTime = M1;
            }else if(interval.equals("m5")){
                intervalTime = M5;
            }else if(interval.equals("m15")){
                intervalTime = M15;
            }else if(interval.equals("m30")){
                intervalTime = M30;
            }else if(interval.equals("h1")){
                intervalTime = H1;
            }else if(interval.equals("h2")){
                intervalTime = H2;
            }else if(interval.equals("h6")){
                intervalTime = H6;
            }else if(interval.equals("h12")){
                intervalTime = H12;
            }else{
                intervalTime = D1;
            }

            int coinsHistoryInd = -1;
            for (int i = 0; i < chartM1.data.size(); i++) {
                FetchCoinsAPIModel.CoinM1History item = chartM1.data.get(i);
                if(coinsHistoryInd==-1||
                        coinsHistory.data.get(coinsHistoryInd).time!=(item.time-(item.time%intervalTime))){
                    //new
                    coinsHistoryInd++;
                    coinsHistory.data.add(new CoinResponseModel.CoinHistoryCandleChart(item.priceUsd,item.priceUsd,item.priceUsd,item.priceUsd,item.time-(item.time%intervalTime)));
                }else{
                    if(coinsHistory.data.get(coinsHistoryInd).low>item.priceUsd){
                        coinsHistory.data.get(coinsHistoryInd).low = item.priceUsd;
                    }
                    if(coinsHistory.data.get(coinsHistoryInd).height<item.priceUsd){
                        coinsHistory.data.get(coinsHistoryInd).height = item.priceUsd;
                    }
                    coinsHistory.data.get(coinsHistoryInd).close = item.priceUsd;
                }
            }

            System.out.println(new Gson().toJson(coinsHistory));
            return new ResponseAPIModel(0,ResponseAPIModel.Status.Success,coinsHistory);
        }catch (Exception e){
            System.out.println(e);
            return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Lỗi hệ thống");
        }
    }

}
