package Nhom3.Server.service;

import Nhom3.Server.controller.TradingCommandController;
import Nhom3.Server.model.CoinsValueNow;
import Nhom3.Server.model.FetchCoinsAPIModel;
import Nhom3.Server.model.SocketCoinsModel;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class LoopService implements Runnable{
    @Autowired
    CoinAPIService coinAPIService;
    @Autowired
    TradingCommandController tradingCommandController;

    private static int DELAY_TIME_PER_LOOP = 5;//5 sec

    public LoopService(){
//        coinAPIService = new CoinAPIService();
//        tradingCommandController = new TradingCommandController();
    }

    public void start(){
        while(true){
            try {
                    FetchCoinsAPIModel.CoinsNow coinsValue = coinAPIService.getCoinsNow();
                    handleCoinsValue(coinsValue);
                    Thread.sleep(DELAY_TIME_PER_LOOP * 1000);
            } catch (Exception ie) {
                System.out.println(ie.toString());
            }
        }
    }
    @Override
    public void run(){
        while(true){
            try {
                FetchCoinsAPIModel.CoinsNow coinsValue = coinAPIService.getCoinsNow();
                handleCoinsValue(coinsValue);
                Thread.sleep(DELAY_TIME_PER_LOOP * 1000);
            } catch (Exception ie) {
                System.out.println(ie.toString());
            }
        }
    }
    public void handleCoinsValue(FetchCoinsAPIModel.CoinsNow coinsValue){
        CoinsValueNow.set(coinsValue);
        //check auto close trading command
        new Thread(new TradingCommandAutoCheck(tradingCommandController)).start();
        //send to socket
        SocketCoinsModel.Coins coins = new SocketCoinsModel.Coins();
        coins.timestamp = coinsValue.timestamp;
        coins.data = new ArrayList(coinsValue.data.stream().map(e->new SocketCoinsModel.Coin(e.id,e.rank,e.symbol,e.name,e.volumeUsd24Hr,e.priceUsd,e.changePercent24Hr,e.vwap24Hr)).toList());
        String coinsJson = new Gson().toJson(coins);

        SocketService.sendToAll(SocketService.EventNames.Send.CoinsPriceNow,coinsJson);
    }

    public class TradingCommandAutoCheck implements Runnable{
        TradingCommandController tradingCommandController;
        public TradingCommandAutoCheck(TradingCommandController tradingCommandController){
            this.tradingCommandController=tradingCommandController;
        }
        @Override
        public void run() {
            tradingCommandController.checkAutoClose();
        }
    }

}
