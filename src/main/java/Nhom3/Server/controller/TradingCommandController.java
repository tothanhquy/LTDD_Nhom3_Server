package Nhom3.Server.controller;

import Nhom3.Server.model.*;
import Nhom3.Server.model.client_response.AccountMoneyHistoryResponseModel;
import Nhom3.Server.model.client_response.TradingCommandResponseModel;
import Nhom3.Server.service.*;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


@RestController
@RequestMapping("/tradingCommand")
public class TradingCommandController extends CoreController {
    @Autowired
    TradingCommandService tradingCommandService;
    @Autowired
    AccountService accountService;
    @Autowired
    AccountMoneyHistoryService accountMoneyHistoryService;
    @Autowired
    AccountController accountController;

    private static final float BASE_COMMISSION = 0.0001F;//0.01%
    private static final ArrayList<Integer> LeveragesValid = new ArrayList<>(Arrays.asList(1,5,8,10,13,20));

    @PostMapping("/verifyPin")
    public ResponseAPIModel verifyPin(HttpServletRequest request, @RequestParam String pin) {
        try{
            AccountService.AccountAuth accountAuth = getAccountAuthFromRequest(request);
            if(accountAuth==null)throw new Exception();
            AccountModel queryAccount = accountService.getById(accountAuth.id);
            if(queryAccount==null)throw new Exception();

            return accountController.verifyTradingAuthStep1(queryAccount,pin);
        }catch (Exception e){
            System.out.println(e);
            return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Lỗi hệ thống");
        }
    }

    @PostMapping("/open")
    public ResponseAPIModel open(HttpServletRequest request, @RequestParam String optCode, @RequestParam String coinId, @RequestParam float moneyNumber, @RequestParam int leverage, @RequestParam boolean enableTpSl, @RequestParam float takeProfit, @RequestParam float stopLoss) {
        try{
            AccountService.AccountAuth accountAuth = getAccountAuthFromRequest(request);
            if(accountAuth==null)throw new Exception();
            AccountModel queryAccount = accountService.getById(accountAuth.id);
            if(queryAccount==null)throw new Exception();

            //check otp
            ResponseAPIModel otpResAction = accountController.verifyTradingAuthStep2(queryAccount, optCode);
            if(otpResAction.status==ResponseAPIModel.Status.Fail){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,otpResAction.error);
            }

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

            float openPrice = coin.priceUsd;
            float coinNumber = moneyNumber*leverage/openPrice;

            float commission = leverage==1?0L:openPrice*coinNumber*BASE_COMMISSION;

            if(enableTpSl&&stopLoss>=moneyNumber-commission){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Cắt lỗ phải nhỏ hơn giá trị hiện tại(đã bao gồm hoa hồng).");
            }
            if(enableTpSl&&takeProfit<=moneyNumber-commission){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Chốt lời phải lớn hơn giá trị hiện tại(đã bao gồm hoa hồng).");
            }

            ResponseServiceModel resAction = tradingCommandService.create(queryAccount, coinNumber, moneyNumber, leverage, openPrice, enableTpSl, takeProfit, stopLoss, coinId);
            if(resAction.status==ResponseServiceModel.Status.Fail){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,resAction.error);
            }
            String createdId = resAction.data.toString();

            queryAccount.setMoneyNow(queryAccount.getMoneyNow()-moneyNumber);
            queryAccount.setInvestedMoney(queryAccount.getInvestedMoney()+moneyNumber);
            queryAccount.compareInvestedMoney(moneyNumber);
            queryAccount.setOpenTradingCommandNumber(queryAccount.getOpenTradingCommandNumber()+1);
            resAction = accountService.update(queryAccount);
            if(resAction.status==ResponseServiceModel.Status.Fail){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,resAction.error);
            }

            //update top chart
            TopChartUserNow.update(new TopChartUserNow.User(queryAccount.getId(),queryAccount.getMoneyNow()));

            TradingCommandResponseModel.Created resOj = new TradingCommandResponseModel.Created(createdId);
            return new ResponseAPIModel(0,ResponseAPIModel.Status.Success,resOj);
        }catch (Exception e){
            System.out.println(e);
            return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Lỗi hệ thống");
        }
    }

    @PostMapping("/edit")
    public ResponseAPIModel edit(HttpServletRequest request, @RequestParam String optCode, @RequestParam String id, @RequestParam boolean enableTpSl, @RequestParam float takeProfit, @RequestParam float stopLoss) {
        try{
            AccountService.AccountAuth accountAuth = getAccountAuthFromRequest(request);
            if(accountAuth==null)throw new Exception();
            AccountModel queryAccount = accountService.getById(accountAuth.id);
            if(queryAccount==null)throw new Exception();

            //check otp
            ResponseAPIModel otpResAction = accountController.verifyTradingAuthStep2(queryAccount, optCode);
            if(otpResAction.status==ResponseAPIModel.Status.Fail){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,otpResAction.error);
            }

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
                float profitNow = (coin.priceUsd-tradingCommand.openPrice)*tradingCommand.coinNumber;

                float commission = tradingCommand.leverage==1?0L:coin.priceUsd*tradingCommand.coinNumber*BASE_COMMISSION;

                if(takeProfit<=tradingCommand.moneyNumber+profitNow-commission){
                    return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Chốt lời không thể thấp hơn giá trị hiện tại(đã bao gồm hoa hồng).");
                }
                if(stopLoss>=tradingCommand.moneyNumber+profitNow-commission){
                    return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Cắt lỗ không thể cao hơn giá trị hiện tại(đã bao gồm hoa hồng).");
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
    public ResponseAPIModel close(HttpServletRequest request, @RequestParam String optCode, @RequestParam String id) {
        try{
            AccountService.AccountAuth accountAuth = getAccountAuthFromRequest(request);
            if(accountAuth==null)throw new Exception();
            AccountModel queryAccount = accountService.getById(accountAuth.id);
            if(queryAccount==null)throw new Exception();

            //check otp
            ResponseAPIModel otpResAction = accountController.verifyTradingAuthStep2(queryAccount, optCode);
            if(otpResAction.status==ResponseAPIModel.Status.Fail){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,otpResAction.error);
            }

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

            float profitNow = (coin.priceUsd-tradingCommand.openPrice)*tradingCommand.coinNumber;

            tradingCommand.isOpen=false;
            tradingCommand.closePrice = coin.priceUsd;
            tradingCommand.closeTime = System.currentTimeMillis();
            tradingCommand.finalProfit = profitNow;
            if(tradingCommand.leverage==1){
                tradingCommand.commission=0F;
            }else{
                tradingCommand.commission = coin.priceUsd*tradingCommand.coinNumber*BASE_COMMISSION;
            }

            ResponseServiceModel resAction = tradingCommandService.update(tradingCommand);
            if(resAction.status==ResponseServiceModel.Status.Fail){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,resAction.error);
            }

            queryAccount.setMoneyNow(queryAccount.getMoneyNow()+profitNow-tradingCommand.commission+tradingCommand.moneyNumber);
            queryAccount.setInvestedMoney(queryAccount.getInvestedMoney()-tradingCommand.moneyNumber);
            queryAccount.compareProfit(profitNow);
            queryAccount.setOpenTradingCommandNumber(queryAccount.getOpenTradingCommandNumber()-1);
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
            resAction = accountMoneyHistoryService.create(queryAccount,moneyHistoryName,tradingCommand.finalProfit);
            if(resAction.status==ResponseServiceModel.Status.Fail){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,resAction.error);
            }

            //update top chart
            TopChartUserNow.update(new TopChartUserNow.User(queryAccount.getId(),queryAccount.getMoneyNow()));

            return new ResponseAPIModel(ResponseAPIModel.Status.Success,"");
        }catch (Exception e){
            System.out.println(e);
            return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Lỗi hệ thống");
        }
    }

    //tool / not http
    public ResponseAPIModel closeAuto(TradingCommandModel tradingCommand) {
        try{
            //check permission
            if(!tradingCommand.isOpen){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Bạn không thể đóng lệnh đã đóng trước đó.");
            }

            //check valid coin
            FetchCoinsAPIModel.CoinNow coin = CoinsValueNow.getCoin(tradingCommand.coinId);
            if(coin==null){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Hiện không hỗ trợ đồng tiền này.");
            }

            float profitNow = (coin.priceUsd-tradingCommand.openPrice)*tradingCommand.coinNumber;

            tradingCommand.isOpen=false;
            tradingCommand.closePrice = coin.priceUsd;
            tradingCommand.closeTime = System.currentTimeMillis();
            tradingCommand.finalProfit = profitNow;
            if(tradingCommand.leverage==1){
                tradingCommand.commission=0F;
            }else{
                tradingCommand.commission = coin.priceUsd*tradingCommand.coinNumber*BASE_COMMISSION;
            }

            ResponseServiceModel resAction = tradingCommandService.update(tradingCommand);
            if(resAction.status==ResponseServiceModel.Status.Fail){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,resAction.error);
            }

            AccountModel account = tradingCommand.author;
            account.setMoneyNow(account.getMoneyNow()+profitNow-tradingCommand.commission+tradingCommand.moneyNumber);
            account.setInvestedMoney(account.getInvestedMoney()-tradingCommand.moneyNumber);
            account.compareProfit(profitNow);
            account.setOpenTradingCommandNumber(account.getOpenTradingCommandNumber()-1);
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
            resAction = accountMoneyHistoryService.create(account,moneyHistoryName,tradingCommand.finalProfit);
            if(resAction.status==ResponseServiceModel.Status.Fail){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,resAction.error);
            }
            //send socket
            String resJson = new Gson().toJson(new TradingCommandResponseModel.AutoClose(tradingCommand.id));

            SocketService.sendToRoom(SocketService.EventNames.Send.AutoCloseTradingCommand,SocketService.RoomNamesPrefix.PersonalRoom+account.getId(),resJson);

            //update top chart
            TopChartUserNow.update(new TopChartUserNow.User(account.getId(),account.getMoneyNow()));

            return new ResponseAPIModel(ResponseAPIModel.Status.Success,"");
        }catch (Exception e){
            System.out.println(e);
            return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Lỗi hệ thống");
        }
    }

    //tool / not http
    public void checkAutoClose(){
        int numberPerThread = 5;
        ArrayList<TradingCommandModel> items = tradingCommandService.serverGetItems(true);
        for(int i=0;i<items.size()/numberPerThread;i++){
            new Thread(new CheckAutoCloseSub(items.subList(i*numberPerThread,i*numberPerThread+numberPerThread))).start();
        }
        if(items.size()%numberPerThread!=0){
            int startInd = items.size()-(items.size()%numberPerThread);
            new Thread(new CheckAutoCloseSub(items.subList(startInd,startInd+(items.size()%numberPerThread)))).start();
        }
    }
    //tool / not http
    public class CheckAutoCloseSub implements Runnable{
        List<TradingCommandModel> items;
        public CheckAutoCloseSub(List<TradingCommandModel> items){
            this.items = items;
        }
        @Override
        public void run() {
            TradingCommandModel item;
            float tradingFee;
            float profitNow;
            for (int i = 0; i < items.size(); i++) {
                item = items.get(i);

                FetchCoinsAPIModel.CoinNow coin = CoinsValueNow.getCoin(item.coinId);
                if(coin==null)continue;

                tradingFee = item.leverage==1?0L:coin.priceUsd*item.coinNumber*BASE_COMMISSION;
                profitNow = (coin.priceUsd-item.openPrice)*item.coinNumber;

                if(
                        (item.enableTpSl&&(
                                item.moneyNumber+profitNow-tradingFee>item.takeProfit
                                ||item.moneyNumber+profitNow-tradingFee<item.stopLoss
                        ))||(
                                item.moneyNumber+profitNow-tradingFee<=0L
                                )){
                    ResponseAPIModel res = closeAuto(item);
                }
            }
        }
    }

    @GetMapping("/details")
    public ResponseAPIModel details(HttpServletRequest request, @RequestParam String id) {
        try{
            AccountService.AccountAuth accountAuth = getAccountAuthFromRequest(request);
            if(accountAuth==null)throw new Exception();
            TradingCommandModel tradingCommand = tradingCommandService.getById(id);
            if(tradingCommand==null)throw new Exception();
//
            //check permission
            if(!tradingCommand.author.getId().equals(accountAuth.id)&&tradingCommand.isOpen){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Bạn không phải chủ của lệnh này.");
            }

            if(tradingCommand.isOpen){
                TradingCommandResponseModel.OpenTradingCommand resOj = new TradingCommandResponseModel.OpenTradingCommand(tradingCommand);
                return new ResponseAPIModel(0,ResponseAPIModel.Status.Success,resOj);
            }else{
                TradingCommandResponseModel.CloseTradingCommand resOj = new TradingCommandResponseModel.CloseTradingCommand(tradingCommand);
                return new ResponseAPIModel(0,ResponseAPIModel.Status.Success,resOj);
            }
        }catch (Exception e){
            System.out.println(e);
            return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Lỗi hệ thống");
        }
    }
    @GetMapping("/getList")
    public ResponseAPIModel getList(HttpServletRequest request, @RequestParam String userId, @RequestParam boolean isOpen) {
        try{
            AccountService.AccountAuth accountAuth = getAccountAuthFromRequest(request);
            if(accountAuth==null)throw new Exception();

            //check permission
            if(!userId.equals(accountAuth.id)&&isOpen){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Bạn không phải chủ của các lệnh này.");
            }

            ArrayList<TradingCommandModel> tradingCommands = tradingCommandService.getItems(userId, isOpen);

            tradingCommands.sort(new Comparator<TradingCommandModel>() {
                @Override
                public int compare(TradingCommandModel a, TradingCommandModel b) {
                    if(isOpen){ return (int)(b.openTime-a.openTime);} else{ return (int)(b.closeTime-a.closeTime);}
                }
            });
            if(isOpen){
                TradingCommandResponseModel.OpenTradingCommandList resOj = new TradingCommandResponseModel.OpenTradingCommandList(tradingCommands);
                return new ResponseAPIModel(0,ResponseAPIModel.Status.Success,resOj);
            }else{
                TradingCommandResponseModel.CloseTradingCommandList resOj = new TradingCommandResponseModel.CloseTradingCommandList(tradingCommands);
                return new ResponseAPIModel(0,ResponseAPIModel.Status.Success,resOj);
            }
        }catch (Exception e){
            System.out.println(e);
            return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Lỗi hệ thống");
        }
    }

}
