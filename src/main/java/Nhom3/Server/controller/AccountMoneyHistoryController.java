package Nhom3.Server.controller;

import Nhom3.Server.model.AccountMoneyHistoryModel;
import Nhom3.Server.model.FetchCoinsAPIModel;
import Nhom3.Server.model.ResponseAPIModel;
import Nhom3.Server.model.client_response.AccountMoneyHistoryResponseModel;
import Nhom3.Server.model.client_response.CoinResponseModel;
import Nhom3.Server.service.AccountMoneyHistoryService;
import Nhom3.Server.service.AccountService;
import Nhom3.Server.service.CoinAPIService;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;


@RestController
@RequestMapping("/accountMoneyHistory")
public class AccountMoneyHistoryController extends CoreController {
    @Autowired
    AccountMoneyHistoryService accountMoneyHistoryService;
    @GetMapping("/getList")
    public ResponseAPIModel getList(HttpServletRequest request) {
        try{
            AccountService.AccountAuth accountAuth = getAccountAuthFromRequest(request);
            if(accountAuth==null)throw new Exception();

            ArrayList<AccountMoneyHistoryModel> items = accountMoneyHistoryService.getByIdAccount(accountAuth.id);
            items.sort(new Comparator<AccountMoneyHistoryModel>() {
                @Override
                public int compare(AccountMoneyHistoryModel a, AccountMoneyHistoryModel b) {
                    return (int)(b.time-a.time);
                }
            });
            AccountMoneyHistoryResponseModel.Lists resOj = new AccountMoneyHistoryResponseModel.Lists(items);

            return new ResponseAPIModel(0,ResponseAPIModel.Status.Success,resOj);
        }catch (Exception e){
            System.out.println(e);
            return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Lỗi hệ thống");
        }
    }

}
