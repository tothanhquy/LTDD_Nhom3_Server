package Nhom3.Server.controller;

import Nhom3.Server.model.*;
import Nhom3.Server.model.client_response.AccountMoneyHistoryResponseModel;
import Nhom3.Server.model.client_response.TradingCommandResponseModel;
import Nhom3.Server.service.AccountMoneyHistoryService;
import Nhom3.Server.service.AccountService;
import Nhom3.Server.service.General;
import Nhom3.Server.service.TradingCommandService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;


@RestController
@RequestMapping("/tradingCommand")
public class TradingCommandController extends CoreController {
    @Autowired
    TradingCommandService tradingCommandService;
    @Autowired
    AccountService accountService;
    @Autowired
    AccountMoneyHistoryService accountMoneyHistoryService;

    private static final float BASE_COMMISSION = 0.0001F;//0.01%
    private static final ArrayList<Integer> LeveragesValid = new ArrayList<>(Arrays.asList(1,5,8,10,13,20));
    @PostMapping("/open")
    public ResponseAPIModel open(HttpServletRequest request, @RequestParam String coinId, @RequestParam float moneyNumber, @RequestParam int leverage, @RequestParam boolean enableTpSl, @RequestParam float takeProfit, @RequestParam float stopLoss) {
        try{
            AccountService.AccountAuth accountAuth = getAccountAuthFromRequest(request);
            if(accountAuth==null)throw new Exception();
            AccountModel queryAccount = accountService.getById(accountAuth.id);
            if(queryAccount==null)throw new Exception();

            //check valid coin
            FetchCoinsAPIModel.CoinNow coin = CoinsValueNow.getCoin(coinId);
            if(coin==null){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Hiện không hỗ trợ đồng tiền này.");
            }

            //check valid leverage
            if(!LeveragesValid.contains(leverage))throw new Exception();

            //check valid moneyNumber
            if(queryAccount.getMoneyNow()<moneyNumber){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Tài khoản hiện không đủ tiền để giao dịch.");
            }

            if(enableTpSl&&stopLoss>=moneyNumber){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Cắt lỗ phải nhỏ hơn số tiền giao dịch.");
            }
            if(enableTpSl&&takeProfit<=moneyNumber){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Chốt lời phải lớn hơn số tiền giao dịch.");
            }

            float openPrice = coin.priceUsd;
            float coinNumber = moneyNumber/openPrice;

            ResponseServiceModel resAction = tradingCommandService.create(queryAccount, coinNumber, moneyNumber, leverage, openPrice, enableTpSl, takeProfit, stopLoss, coinId);
            if(resAction.status==ResponseServiceModel.Status.Fail){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,resAction.error);
            }

            TradingCommandResponseModel.Created resOj = new TradingCommandResponseModel.Created(resAction.data.toString());
            return new ResponseAPIModel(0,ResponseAPIModel.Status.Success,resOj);
        }catch (Exception e){
            System.out.println(e);
            return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Lỗi hệ thống");
        }
    }

    @PostMapping("/edit")
    public ResponseAPIModel edit(HttpServletRequest request, @RequestParam String id, @RequestParam boolean enableTpSl, @RequestParam float takeProfit, @RequestParam float stopLoss) {
        try{
            AccountService.AccountAuth accountAuth = getAccountAuthFromRequest(request);
            if(accountAuth==null)throw new Exception();
            AccountModel queryAccount = accountService.getById(accountAuth.id);
            if(queryAccount==null)throw new Exception();
            TradingCommandModel tradingCommand = tradingCommandService.getById(id);
            if(tradingCommand==null)throw new Exception();

            //check permission
            if(!tradingCommand.author.getId().equals(accountAuth.id)){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Bạn không phải chủ của lệnh này.");
            }
            //check permission
            if(!tradingCommand.isOpen){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Bạn không thể sửa lệnh đã đóng.");
            }

            //check valid coin
            FetchCoinsAPIModel.CoinNow coin = CoinsValueNow.getCoin(tradingCommand.coinId);
            if(coin==null){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Hiện không hỗ trợ đồng tiền này.");
            }

            if(enableTpSl){
                //check valid value
                float moneyNow = (coin.priceUsd-tradingCommand.openPrice)*tradingCommand.leverage*tradingCommand.coinNumber+tradingCommand.moneyNumber;
                if(takeProfit<=moneyNow){
                    return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Chốt lời không thể thấp hơn giá trị hiện tại.");
                }
                if(stopLoss>=moneyNow){
                    return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Cắt lỗ không thể cao hơn giá trị hiện tại.");
                }
                tradingCommand.enableTpSl = true;
                tradingCommand.stopLoss = stopLoss;
                tradingCommand.takeProfit = takeProfit;
            }else{
                tradingCommand.enableTpSl = false;
            }

            ResponseServiceModel resAction = tradingCommandService.update(tradingCommand);
            if(resAction.status==ResponseServiceModel.Status.Fail){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,resAction.error);
            }

            return new ResponseAPIModel(ResponseAPIModel.Status.Success,"");
        }catch (Exception e){
            System.out.println(e);
            return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Lỗi hệ thống");
        }
    }

    @PostMapping("/close")
    public ResponseAPIModel close(HttpServletRequest request, @RequestParam String id) {
        try{
            AccountService.AccountAuth accountAuth = getAccountAuthFromRequest(request);
            if(accountAuth==null)throw new Exception();
            AccountModel queryAccount = accountService.getById(accountAuth.id);
            if(queryAccount==null)throw new Exception();
            TradingCommandModel tradingCommand = tradingCommandService.getById(id);
            if(tradingCommand==null)throw new Exception();

            //check permission
            if(!tradingCommand.author.getId().equals(accountAuth.id)){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Bạn không phải chủ của lệnh này.");
            }
            //check permission
            if(!tradingCommand.isOpen){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Bạn không thể đóng lệnh đã đóng trước đó.");
            }

            //check valid coin
            FetchCoinsAPIModel.CoinNow coin = CoinsValueNow.getCoin(tradingCommand.coinId);
            if(coin==null){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Hiện không hỗ trợ đồng tiền này.");
            }

            float moneyNow = (coin.priceUsd-tradingCommand.openPrice)*tradingCommand.leverage*tradingCommand.coinNumber+tradingCommand.moneyNumber;

            tradingCommand.isOpen=false;
            tradingCommand.closePrice = coin.priceUsd;
            tradingCommand.closeTime = System.currentTimeMillis();
            tradingCommand.finalProfit = moneyNow-tradingCommand.moneyNumber;
            tradingCommand.commission = tradingCommand.finalProfit<0?0L:tradingCommand.finalProfit*tradingCommand.leverage*BASE_COMMISSION;

            ResponseServiceModel resAction = tradingCommandService.update(tradingCommand);
            if(resAction.status==ResponseServiceModel.Status.Fail){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,resAction.error);
            }

            queryAccount.setMoneyNow(queryAccount.getMoneyNow()+tradingCommand.finalProfit-tradingCommand.commission);
            resAction = accountService.update(queryAccount);
            if(resAction.status==ResponseServiceModel.Status.Fail){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,resAction.error);
            }

            String moneyHistoryName = AccountMoneyHistoryService.NAME_TEMPLATE;
            moneyHistoryName = moneyHistoryName.replace("{{CoinSymbol}}",coin.symbol.toUpperCase());
            moneyHistoryName = moneyHistoryName.replace("{{MoneyNumber}}", General.addDotIntoNumber(((long)tradingCommand.moneyNumber)+""));
            moneyHistoryName = moneyHistoryName.replace("{{Leverage}}",""+tradingCommand.leverage);
            moneyHistoryName = moneyHistoryName.replace("{{OpenDateTime}}",""+General.addDotIntoNumber(tradingCommand.openTime));
            moneyHistoryName+= ", Đóng lệnh thủ công.";
            resAction = accountMoneyHistoryService.create(queryAccount,moneyHistoryName,tradingCommand.finalProfit-tradingCommand.commission);
            if(resAction.status==ResponseServiceModel.Status.Fail){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,resAction.error);
            }

            return new ResponseAPIModel(ResponseAPIModel.Status.Success,"");
        }catch (Exception e){
            System.out.println(e);
            return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Lỗi hệ thống");
        }
    }

    //tool / not http
    public ResponseAPIModel closeAuto(AccountModel account, String id) {
        try{
            TradingCommandModel tradingCommand = tradingCommandService.getById(id);
            if(tradingCommand==null)throw new Exception();

            //check permission
            if(!tradingCommand.author.getId().equals(account.getId())){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Bạn không phải chủ của lệnh này.");
            }
            //check permission
            if(!tradingCommand.isOpen){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Bạn không thể đóng lệnh đã đóng trước đó.");
            }

            //check valid coin
            FetchCoinsAPIModel.CoinNow coin = CoinsValueNow.getCoin(tradingCommand.coinId);
            if(coin==null){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Hiện không hỗ trợ đồng tiền này.");
            }

            float moneyNow = (coin.priceUsd-tradingCommand.openPrice)*tradingCommand.leverage*tradingCommand.coinNumber+tradingCommand.moneyNumber;

            tradingCommand.isOpen=false;
            tradingCommand.closePrice = coin.priceUsd;
            tradingCommand.closeTime = System.currentTimeMillis();
            tradingCommand.finalProfit = moneyNow-tradingCommand.moneyNumber;
            tradingCommand.commission = tradingCommand.finalProfit<0?0L:tradingCommand.finalProfit*tradingCommand.leverage*BASE_COMMISSION;

            ResponseServiceModel resAction = tradingCommandService.update(tradingCommand);
            if(resAction.status==ResponseServiceModel.Status.Fail){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,resAction.error);
            }

            account.setMoneyNow(account.getMoneyNow()+tradingCommand.finalProfit-tradingCommand.commission);
            resAction = accountService.update(account);
            if(resAction.status==ResponseServiceModel.Status.Fail){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,resAction.error);
            }

            String moneyHistoryName = AccountMoneyHistoryService.NAME_TEMPLATE;
            moneyHistoryName = moneyHistoryName.replace("{{CoinSymbol}}",coin.symbol.toUpperCase());
            moneyHistoryName = moneyHistoryName.replace("{{MoneyNumber}}", General.addDotIntoNumber(((long)tradingCommand.moneyNumber)+""));
            moneyHistoryName = moneyHistoryName.replace("{{Leverage}}",""+tradingCommand.leverage);
            moneyHistoryName = moneyHistoryName.replace("{{OpenDateTime}}",""+General.addDotIntoNumber(tradingCommand.openTime));
            moneyHistoryName+= ", Đóng lệnh tự động.";
            resAction = accountMoneyHistoryService.create(account,moneyHistoryName,tradingCommand.finalProfit-tradingCommand.commission);
            if(resAction.status==ResponseServiceModel.Status.Fail){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,resAction.error);
            }

            return new ResponseAPIModel(ResponseAPIModel.Status.Success,"");
        }catch (Exception e){
            System.out.println(e);
            return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Lỗi hệ thống");
        }
    }


}
