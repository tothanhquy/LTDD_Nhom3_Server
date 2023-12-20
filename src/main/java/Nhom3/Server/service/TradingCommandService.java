package Nhom3.Server.service;

import Nhom3.Server.model.AccountModel;
import Nhom3.Server.model.AccountMoneyHistoryModel;
import Nhom3.Server.model.ResponseServiceModel;
import Nhom3.Server.model.TradingCommandModel;
import Nhom3.Server.repository.AccountMoneyHistoryRepository;
import Nhom3.Server.repository.TradingCommandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

@Service
public class TradingCommandService {
    @Autowired
    TradingCommandRepository tradingCommandRepository;

    public ArrayList<TradingCommandModel> getItems(String idAccount, boolean isOpen){
        ArrayList<TradingCommandModel> items = new ArrayList<>(tradingCommandRepository.findAll().stream().filter((e)->{
            return e.author.getId().contains(idAccount) && e.isOpen==isOpen;
        }).toList());
        return items;
    }
    public ArrayList<TradingCommandModel> serverGetItems(boolean isOpen){
        ArrayList<TradingCommandModel> items = new ArrayList<>(tradingCommandRepository.findAll().stream().filter((e)->{
            return e.isOpen==isOpen;
        }).toList());
        return items;
    }


    public ResponseServiceModel create(AccountModel author, float coinNumber, float moneyNumber, int leverage, float openPrice, boolean enableTpSl, float takeProfit, float stopLoss, String coinId){
        try {
            TradingCommandModel newItem = new TradingCommandModel(author, coinNumber, moneyNumber, leverage, openPrice, System.currentTimeMillis() ,enableTpSl, takeProfit, stopLoss, coinId);
            String newId = tradingCommandRepository.save(newItem).id;
            return new ResponseServiceModel<String>(ResponseServiceModel.Status.Success,"",newId);
        }catch(Exception e){
            return new ResponseServiceModel<String>(ResponseServiceModel.Status.Fail,"Lỗi hệ thống","");
        }
    }

    public TradingCommandModel getById(String id){
        Optional<TradingCommandModel> optional = tradingCommandRepository.findById(id);
        if (optional.isPresent()) {
            TradingCommandModel account = optional.get();
            return account;
        }else{
            return null;
        }
    }
    public ResponseServiceModel update(TradingCommandModel accountModel){
        try {
            tradingCommandRepository.save(accountModel);
            return new ResponseServiceModel<String>(ResponseServiceModel.Status.Success,"","");
        }catch(Exception e){
            return new ResponseServiceModel<String>(ResponseServiceModel.Status.Fail,"Lỗi hệ thống","");
        }
    }

}
