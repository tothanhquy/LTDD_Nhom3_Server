package Nhom3.Server.controller;

import Nhom3.Server.service.AccountService;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;

public class CoreController {
    public AccountService.AccountAuth getAccountAuthFromRequest(HttpServletRequest request){
        try{
            AccountService.AccountAuth accountAuth = new Gson().fromJson(request.getAttribute("accountAuth").toString(),AccountService.AccountAuth.class);
            return accountAuth;
        }catch(Exception e){
            return null;
        }
    }
}
