package Nhom3.Server.model.client_response;

import Nhom3.Server.model.AccountModel;
import Nhom3.Server.model.AccountMoneyHistoryModel;
import Nhom3.Server.model.TradingCommandModel;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.ArrayList;
import java.util.Collection;

public class TradingCommandResponseModel {
    public static class Created{
        public String newId;

        public Created(String newId) {
            this.newId = newId;
        }
    }
    public static class AutoClose{
        public String id;

        public AutoClose(String id) {
            this.id = id;
        }
    }
    public static class OpenTradingCommand{
        public String id;
        public String buyOrSell;
        public float coinNumber = 0;
        public float moneyNumber = 0;
        public int leverage = 1;
        public float openPrice=0;
        public long openTime;
        public boolean enableTpSl = false;
        public float takeProfit = 0;
        public float stopLoss = 0;
        public String coinId;
        public float finalProfit=0;
        public boolean isOpen = true;
        public float commission = 0;

        public OpenTradingCommand(TradingCommandModel tradingCommandModel) {
            this.id = tradingCommandModel.id;
            this.buyOrSell = tradingCommandModel.buyOrSell;
            this.coinNumber = tradingCommandModel.coinNumber;
            this.moneyNumber = tradingCommandModel.moneyNumber;
            this.leverage = tradingCommandModel.leverage;
            this.openPrice = tradingCommandModel.openPrice;
            this.openTime = tradingCommandModel.openTime;
            this.enableTpSl = tradingCommandModel.enableTpSl;
            this.takeProfit = tradingCommandModel.takeProfit;
            this.stopLoss = tradingCommandModel.stopLoss;
            this.coinId = tradingCommandModel.coinId;
            this.finalProfit = tradingCommandModel.finalProfit;
            this.isOpen = tradingCommandModel.isOpen;
            this.commission = tradingCommandModel.commission;
        }
    }
    public static class CloseTradingCommand{
        public String id;
        public String buyOrSell;
        public float coinNumber = 0;
        public float moneyNumber = 0;
        public int leverage = 1;
        public float openPrice=0;
        public long openTime;
        public float closePrice=0;
        public long closeTime;
        public boolean enableTpSl = false;
        public float takeProfit = 0;
        public float stopLoss = 0;
        public String coinId;
        public float finalProfit=0;
        public boolean isOpen = true;
        public float commission = 0;

        public CloseTradingCommand(TradingCommandModel tradingCommandModel) {
            this.id = tradingCommandModel.id;
            this.buyOrSell = tradingCommandModel.buyOrSell;
            this.coinNumber = tradingCommandModel.coinNumber;
            this.moneyNumber = tradingCommandModel.moneyNumber;
            this.leverage = tradingCommandModel.leverage;
            this.openPrice = tradingCommandModel.openPrice;
            this.openTime = tradingCommandModel.openTime;
            this.closePrice = tradingCommandModel.closePrice;
            this.closeTime = tradingCommandModel.closeTime;
            this.enableTpSl = tradingCommandModel.enableTpSl;
            this.takeProfit = tradingCommandModel.takeProfit;
            this.stopLoss = tradingCommandModel.stopLoss;
            this.coinId = tradingCommandModel.coinId;
            this.finalProfit = tradingCommandModel.finalProfit;
            this.isOpen = tradingCommandModel.isOpen;
            this.commission = tradingCommandModel.commission;
        }
    }

    public static class OpenTradingCommandItem{
        public String id;
        public String buyOrSell;
        public float coinNumber = 0;
        public float moneyNumber = 0;
        public int leverage = 1;
        public float openPrice=0;
        public long openTime;
        public boolean enableTpSl = false;
        public String coinId;
        public float finalProfit=0;

        public OpenTradingCommandItem(TradingCommandModel tradingCommandModel) {
            this.id = tradingCommandModel.id;
            this.buyOrSell = tradingCommandModel.buyOrSell;
            this.coinNumber = tradingCommandModel.coinNumber;
            this.moneyNumber = tradingCommandModel.moneyNumber;
            this.leverage = tradingCommandModel.leverage;
            this.openPrice = tradingCommandModel.openPrice;
            this.openTime = tradingCommandModel.openTime;
            this.enableTpSl = tradingCommandModel.enableTpSl;
            this.coinId = tradingCommandModel.coinId;
            this.finalProfit = tradingCommandModel.finalProfit;
        }
    }
    public static class OpenTradingCommandList{
        public ArrayList<OpenTradingCommandItem> items;
        public OpenTradingCommandList(ArrayList<TradingCommandModel> tradingCommandModels){
            items = new ArrayList<>(tradingCommandModels.stream().map(e->new OpenTradingCommandItem(e)).toList());
        }
    }
    public static class CloseTradingCommandItem{
        public String id;
        public String buyOrSell;
        public float moneyNumber = 0;
        public int leverage = 1;
        public float openPrice=0;
        public long openTime;
        public long closeTime;
        public String coinId;
        public float finalProfit=0;

        public CloseTradingCommandItem(TradingCommandModel tradingCommandModel) {
            this.id = tradingCommandModel.id;
            this.buyOrSell = tradingCommandModel.buyOrSell;
            this.moneyNumber = tradingCommandModel.moneyNumber;
            this.leverage = tradingCommandModel.leverage;
            this.openPrice = tradingCommandModel.openPrice;
            this.openTime = tradingCommandModel.openTime;
            this.closeTime = tradingCommandModel.closeTime;
            this.coinId = tradingCommandModel.coinId;
            this.finalProfit = tradingCommandModel.finalProfit;
        }
    }
    public static class CloseTradingCommandList{
        public ArrayList<CloseTradingCommandItem> items;
        public CloseTradingCommandList(ArrayList<TradingCommandModel> tradingCommandModels){
            items = new ArrayList<>(tradingCommandModels.stream().map(e->new CloseTradingCommandItem(e)).toList());
        }
    }

}
