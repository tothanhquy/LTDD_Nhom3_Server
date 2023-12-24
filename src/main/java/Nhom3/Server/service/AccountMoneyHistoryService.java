package Nhom3.Server.service;

import Nhom3.Server.model.AccountModel;
import Nhom3.Server.model.AccountMoneyHistoryModel;
import Nhom3.Server.model.ResponseServiceModel;
import Nhom3.Server.repository.AccountMoneyHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class AccountMoneyHistoryService {
    @Autowired
    AccountMoneyHistoryRepository accountMoneyHistoryRepository;


//    public AccountMoneyHistoryService(MongoTemplate mongoTemplate) {
//        this.mongoTemplate = mongoTemplate;
//    }

    public ArrayList<AccountMoneyHistoryModel> getByIdAccount(String idAccount){
        ArrayList<AccountMoneyHistoryModel> items = new ArrayList<>(accountMoneyHistoryRepository.findAll().stream().filter((e)->{
            return e.account.getId().contains(idAccount);
        }).toList());
        return items;
    }
    public static String NAME_TEMPLATE = "{{CoinSymbol}}/USD, {{MoneyNumber}}$ x {{Leverage}}, {{OpenDateTime}}";
    public ResponseServiceModel create(AccountModel account,String name, float money){
        try {
            AccountMoneyHistoryModel newItem = new AccountMoneyHistoryModel(account, name, System.currentTimeMillis(), money);
            accountMoneyHistoryRepository.save(newItem);
            return new ResponseServiceModel<String>(ResponseServiceModel.Status.Success,"","");
        }catch(Exception e){
            return new ResponseServiceModel<String>(ResponseServiceModel.Status.Fail,"Lỗi hệ thống","");
        }
    }


}
