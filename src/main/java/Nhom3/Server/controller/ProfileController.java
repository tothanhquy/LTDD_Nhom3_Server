package Nhom3.Server.controller;

import Nhom3.Server.model.*;
import Nhom3.Server.model.client_response.AccountMoneyHistoryResponseModel;
import Nhom3.Server.model.client_response.ProfileResponseModel;
import Nhom3.Server.service.AccountMoneyHistoryService;
import Nhom3.Server.service.AccountService;
import Nhom3.Server.service.TradingCommandService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@RestController
@RequestMapping("/profile")
public class ProfileController extends CoreController {
    @Autowired
    AccountService accountService;
    @Autowired
    TradingCommandService tradingCommandService;
    @GetMapping("/details")
    public ResponseAPIModel details(HttpServletRequest request, @RequestParam String userId) {
        try{
            AccountService.AccountAuth accountAuth = getAccountAuthFromRequest(request);
            if(accountAuth==null)throw new Exception();

            AccountModel queryAccount = accountService.getById(userId);
            if(queryAccount==null){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Tài khoản không tồn tại.");
            }

            ProfileResponseModel.Profile resOj = new ProfileResponseModel.Profile();

            resOj.id = queryAccount.getId();
            resOj.name = queryAccount.getName();
            resOj.avatar = queryAccount.getAvatar();
            resOj.moneyNow=queryAccount.getMoneyNow();
            resOj.moneyInvested=queryAccount.getInvestedMoney();
            resOj.moneyProfitNow=getProfitNowOfUser(userId);
            resOj.tradingCommandNumber=queryAccount.getInvestedMoneyTimeNumber();
            resOj.tradingCommandProfitNumber=queryAccount.getInvestedMoneyProfitTimeNumber();
            resOj.topNumber=TopChartUserNow.getIndex(userId)+1;
            resOj.totalNumber=TopChartUserNow.count();
            resOj.tradingCommandMoneyMaximum=queryAccount.getInvestedMoneyMaximum();
            resOj.tradingCommandMoneyAvg=queryAccount.getInvestedMoneySum()/queryAccount.getInvestedMoneyTimeNumber();
            resOj.tradingCommandProfitMaximum=queryAccount.getProfitMoneyMaximum();
            resOj.tradingCommandLossMaximum=queryAccount.getLossMoneyMaximum();

            ArrayList<TradingCommandModel> openCommands = tradingCommandService.getItems(accountAuth.id,true);
            for (int i = 0; i <openCommands.size(); i++) {
                TradingCommandModel item = openCommands.get(i);
                resOj.openCommandItems.add(new ProfileResponseModel.OpenCommandItem(item.id, item.coinId, item.openPrice, item.coinNumber));
            }

            return new ResponseAPIModel(0,ResponseAPIModel.Status.Success,resOj);
        }catch (Exception e){
            System.out.println(e);
            return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Lỗi hệ thống");
        }
    }

    @GetMapping("/topChartUser")
    public ResponseAPIModel topChartUser(HttpServletRequest request, @RequestParam int start ,@RequestParam int end) {
        try{
            AccountService.AccountAuth accountAuth = getAccountAuthFromRequest(request);
            if(accountAuth==null)throw new Exception();

            ArrayList<TopChartUserNow.User> topUsers = TopChartUserNow.getTopUsers(start-1,end-1);

            ProfileResponseModel.TopUsers resOj = new ProfileResponseModel.TopUsers();

            if(topUsers.size()!=0){
                List<AccountModel> accountModels = accountService.getByIds(topUsers.stream().map(e->e.id).toList());

                for (int i = 0; i < accountModels.size(); i++) {
                    AccountModel item = accountModels.get(i);
                    int topNumber = -1;
                    for (int j = 0; j < topUsers.size(); j++) {
                        if(item.getId().equals(topUsers.get(j).id)){
                            topNumber = j + start;
                            break;
                        }
                    }
                    resOj.items.add(new ProfileResponseModel.TopUser(item.getId(),item.getName(),item.getAvatar(),item.getMoneyNow(),item.getInvestedMoneyTimeNumber(),item.getInvestedMoneyProfitTimeNumber(),topNumber));
                }

            }


            return new ResponseAPIModel(0,ResponseAPIModel.Status.Success,resOj);
        }catch (Exception e){
            System.out.println(e);
            return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Lỗi hệ thống");
        }
    }

    @GetMapping("/miniDetails")
    public ResponseAPIModel miniDetails(HttpServletRequest request) {
        try{
            AccountService.AccountAuth accountAuth = getAccountAuthFromRequest(request);
            if(accountAuth==null)throw new Exception();

            AccountModel queryAccount = accountService.getById(accountAuth.id);
            if(queryAccount==null){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Tài khoản không tồn tại.");
            }

            ProfileResponseModel.MiniProfile resOj = new ProfileResponseModel.MiniProfile();

            resOj.moneyNow=queryAccount.getMoneyNow();
            resOj.moneyInvested=queryAccount.getInvestedMoney();
            resOj.moneyProfitNow=getProfitNowOfUser(accountAuth.id);
            resOj.openTradingCommandNumber= queryAccount.getOpenTradingCommandNumber();

            ArrayList<TradingCommandModel> openCommands = tradingCommandService.getItems(accountAuth.id,true);
            for (int i = 0; i <openCommands.size(); i++) {
                TradingCommandModel item = openCommands.get(i);
                resOj.openCommandItems.add(new ProfileResponseModel.OpenCommandItem(item.id, item.coinId, item.openPrice, item.coinNumber));
            }

            return new ResponseAPIModel(0,ResponseAPIModel.Status.Success,resOj);
        }catch (Exception e){
            System.out.println(e);
            return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Lỗi hệ thống");
        }
    }

    //not http
    public float getProfitNowOfUser(String userId){
        ArrayList<TradingCommandModel> tradingCommands = tradingCommandService.getItems(userId, true);
        float sum = 0F;
        for (int i = 0; i < tradingCommands.size(); i++) {
            FetchCoinsAPIModel.CoinNow coin = CoinsValueNow.getCoin(tradingCommands.get(i).coinId);
            if(coin==null)continue;
            sum+=(float)(coin.priceUsd-tradingCommands.get(i).openPrice)*tradingCommands.get(i).coinNumber;
        }
        return sum;
    }
}
