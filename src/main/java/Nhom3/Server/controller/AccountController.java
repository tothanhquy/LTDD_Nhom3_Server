package Nhom3.Server.controller;

import Nhom3.Server.model.AccountModel;
import Nhom3.Server.model.ResponseAPIModel;
import Nhom3.Server.model.ResponseServiceModel;
import Nhom3.Server.model.client_request.AccountRequestModel;
import Nhom3.Server.service.AccountService;
import Nhom3.Server.service.General;
import Nhom3.Server.service.JWTService;
import Nhom3.Server.service.SMSAPIService;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account")
public class AccountController extends CoreController{

    @Autowired
    AccountService accountService;

    @PostMapping("/registerStep1")
    public ResponseAPIModel registerStep1(@RequestBody AccountRequestModel.RegisterStep1 account) {
        try {
            AccountModel queryAccount = accountService.getByNumberPhone(account.numberPhone);
            if(queryAccount!=null){
                if(queryAccount.isVerifyNumberPhone()){
                    return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Số điện thoại đã được đăng ký từ trước.");
                }else{
                    //resend
                    SMSAPIService.sendSMS(account.numberPhone);
                }
            }else{
                //insert
//                System.out.println(new Gson().toJson(account));
                ResponseServiceModel resAction = accountService.register(account.numberPhone,account.password,account.name);
                if(resAction.status==ResponseServiceModel.Status.Fail){
                    return new ResponseAPIModel(ResponseAPIModel.Status.Fail,resAction.error);
                }else{
                    //send
                    SMSAPIService.sendSMS(account.numberPhone);
                }
            }
            return new ResponseAPIModel(ResponseAPIModel.Status.Success,"");
        } catch (Exception e) {
            System.out.println(e.toString());
            return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Lỗi hệ thống");
        }
    }
    @PostMapping("/registerStep2")
    public ResponseAPIModel registerStep2(@RequestBody AccountRequestModel.RegisterStep2 account) {
        try {
            AccountModel queryAccount = accountService.getByNumberPhone(account.numberPhone);
            if(queryAccount==null) {
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Số điện thoại chưa được đăng.");
            }else if(queryAccount.isVerifyNumberPhone()){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Tài khoản đã được xác minh trước đó.");
            }else{
                if(!SMSAPIService.verifyCode(account.numberPhone,account.code)){
                    return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"OTP sai hoặc đã hết hạn.");
                }else{
                    ResponseServiceModel resAction = accountService.setVerifyNumberPhone(queryAccount.getId());
                    if(resAction.status==ResponseServiceModel.Status.Fail){
                        return new ResponseAPIModel(ResponseAPIModel.Status.Fail,resAction.error);
                    }else{
                        return new ResponseAPIModel(ResponseAPIModel.Status.Success,"");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Lỗi hệ thống");
        }
    }
    @PostMapping("/login")
    public ResponseAPIModel login(@RequestBody AccountRequestModel.Login account) {
        try {
            AccountModel queryAccount = accountService.getByNumberPhone(account.numberPhone);
            if(queryAccount==null||!queryAccount.isVerifyNumberPhone()) {
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Tài khoản không tồn tại.");
            }else{
                if(!General.verifyPassword(account.password,queryAccount.getPassword())){
                    return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Sai số điện thoại hoặc mật khẩu.");
                }else{
                    //ok
                    AccountService.AccessToken accessToken = AccountService.AccessToken.general(queryAccount.getId());
                    queryAccount.setAccessToken(accessToken.toString());

                    ResponseServiceModel resAction = accountService.update(queryAccount);
                    if(resAction.status==ResponseServiceModel.Status.Fail){
                        return new ResponseAPIModel(ResponseAPIModel.Status.Fail,resAction.error);
                    }

                    AccountService.JWTContent jwtContent = new AccountService.JWTContent(queryAccount.getId(),accessToken.toString());
                    String jwtResponse = JWTService.register(jwtContent);

                    return new <String>ResponseAPIModel(0,ResponseAPIModel.Status.Success,jwtResponse);
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Lỗi hệ thống");
        }
    }

    @PostMapping("/changeName")
    public ResponseAPIModel changeName(HttpServletRequest request, @RequestBody AccountRequestModel.ChangeName body) {
        try {
            AccountService.AccountAuth accountAuth = getAccountAuthFromRequest(request);
            if(accountAuth==null)throw new Exception();

            AccountModel editAccount = accountService.getById(accountAuth.id);
            if(editAccount==null)throw new Exception();

            editAccount.setName(body.newName);
            ResponseServiceModel resAction = accountService.update(editAccount);
            if(resAction.status==ResponseServiceModel.Status.Fail){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,resAction.error);
            }

            return new ResponseAPIModel(ResponseAPIModel.Status.Success,"");

        } catch (Exception e) {
            System.out.println(e.toString());
            return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Lỗi hệ thống");
        }
    }

}
