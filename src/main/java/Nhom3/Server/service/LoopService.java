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

    private static int DELAY_TIME_PER_LOOP = 1;//1 sec

    public LoopService(){
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
        FetchCoinsAPIModel.CoinsNow oldCoins = CoinsValueNow.get()==null?null: new FetchCoinsAPIModel(). new CoinsNow(CoinsValueNow.get().data,CoinsValueNow.get().timestamp);

        CoinsValueNow.set(coinsValue);
        //check auto close trading command
        new Thread(new TradingCommandAutoCheck(tradingCommandController)).start();

        //check
        if(checkDifferent(oldCoins,coinsValue)){
            //send to socket
            SocketCoinsModel.Coins coins = new SocketCoinsModel.Coins();
            coins.timestamp = coinsValue.timestamp;
            coins.data = new ArrayList(coinsValue.data.stream().map(e->new SocketCoinsModel.Coin(e.id,e.rank,e.symbol,e.name,e.volumeUsd24Hr,e.priceUsd,e.changePercent24Hr,e.vwap24Hr)).toList());
            String coinsJson = new Gson().toJson(coins);

            SocketService.sendToAll(SocketService.EventNames.Send.CoinsPriceNow,coinsJson);
        }
    }

    public boolean checkDifferent(FetchCoinsAPIModel.CoinsNow oldCoins, FetchCoinsAPIModel.CoinsNow newCoins) {
        if(oldCoins==null)return true;
        if(oldCoins.data.size()!=newCoins.data.size())return true;

        for (int i = 0; i < oldCoins.data.size(); i++) {
            for (int j = 0; j < newCoins.data.size(); j++) {
                if(oldCoins.data.get(i).id.equals(newCoins.data.get(j).id)){
                    if(oldCoins.data.get(i).priceUsd!=newCoins.data.get(j).priceUsd){
                        return true;
                    }
                }
            }
        }
        return false;
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
