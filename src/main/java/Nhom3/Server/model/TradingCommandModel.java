package Nhom3.Server.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Document(collection = "tradingCommands")
public class TradingCommandModel {
    @Id
    public String id;
    @DBRef
    public AccountModel author;
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

    public TradingCommandModel(AccountModel author, String buyOrSell, float coinNumber, float moneyNumber, int leverage, float openPrice, long openTime, boolean enableTpSl, float takeProfit, float stopLoss, String coinId) {
        this.author = author;
        this.buyOrSell = buyOrSell;
        this.coinNumber = coinNumber;
        this.moneyNumber = moneyNumber;
        this.leverage = leverage;
        this.openPrice = openPrice;
        this.openTime = openTime;
        this.enableTpSl = enableTpSl;
        this.takeProfit = takeProfit;
        this.stopLoss = stopLoss;
        this.coinId = coinId;
        this.isOpen = true;
    }
}
