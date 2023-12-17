package Nhom3.Server.controller;

import Nhom3.Server.model.AccountModel;
import Nhom3.Server.model.ResponseAPIModel;
import Nhom3.Server.model.ResponseServiceModel;
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

import java.util.ArrayList;

@RestController
@RequestMapping("/account")
public class AccountController extends CoreController{
    private final int REGISTER_VERIFY_FAIL_MAX_NUMBER = 3;
    private final int REGISTER_SEND_CODE_MAX_NUMBER = 3;
    private final int REGISTER_VERIFY_FAIL_WAIT_HOUR = 3;
    private final int VERIFY_CODE_DURATION = 10*60*1000;//10 minus
    private final int RESET_PASSWORD_VERIFY_FAIL_MAX_NUMBER = 3;
    private final int RESET_PASSWORD_SEND_CODE_MAX_NUMBER = 3;
    private final int RESET_PASSWORD_VERIFY_FAIL_WAIT_HOUR = 24;

    @Autowired
    AccountService accountService;

    @PostMapping("/registerStep1")
    public ResponseAPIModel registerStep1(@RequestParam String numberPhone,@RequestParam String password,@RequestParam String name) {
        try {
            AccountModel queryAccount = accountService.getByNumberPhone(numberPhone);
            if(queryAccount!=null){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Số điện thoại đã được đăng ký từ trước.");
            }
            //insert
            ResponseServiceModel resAction = accountService.register(numberPhone,password,name,System.currentTimeMillis());
            if(resAction.status==ResponseServiceModel.Status.Fail){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,resAction.error);
            }
            //send
            SMSAPIService.sendSMS(numberPhone);
            return new ResponseAPIModel(ResponseAPIModel.Status.Success,"");
        } catch (Exception e) {
            System.out.println(e.toString());
            return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Lỗi hệ thống");
        }
    }
    @PostMapping("/registerStep1Resend")
    public ResponseAPIModel registerStep1Resend(@RequestParam String numberPhone) {
        try {
            AccountModel queryAccount = accountService.getByNumberPhone(numberPhone);
            if(queryAccount==null){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Số điện thoại chưa được đăng ký.");
            }
            if(queryAccount.isVerifyNumberPhone()){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Số điện thoại đã được xác minh trước đó.");
            }
            if(queryAccount.getRegisterVerifyBanToTime()>System.currentTimeMillis()){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Không thể gửi. Bạn đã gửi quá nhiều lần trong thời gian ngắn.");
            }
            if(queryAccount.getRegisterLastTimeResend()+REGISTER_VERIFY_FAIL_WAIT_HOUR*60*60*1000<System.currentTimeMillis()){
                //first time after a long time
                queryAccount.setRegisterVerifyBanToTime(0L);
                queryAccount.setRegisterSendCodeNumber(0);
            }
            queryAccount.setRegisterSendCodeNumber(queryAccount.getRegisterSendCodeNumber()+1);
            if(queryAccount.getRegisterSendCodeNumber()>REGISTER_SEND_CODE_MAX_NUMBER){
                //over resend time number
                queryAccount.setRegisterVerifyBanToTime(System.currentTimeMillis()+REGISTER_VERIFY_FAIL_WAIT_HOUR*60*60*1000);
            }else{
                queryAccount.setRegisterLastTimeResend(System.currentTimeMillis());
                queryAccount.setRegisterVerifyFailNumber(0);
            }
            ResponseServiceModel resAction = accountService.update(queryAccount);
            if(resAction.status==ResponseServiceModel.Status.Fail){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,resAction.error);
            }

            if(queryAccount.getRegisterSendCodeNumber()>REGISTER_SEND_CODE_MAX_NUMBER){
                //not resend
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Bạn đã gửi quá nhiều lần trong thời gian ngắn. Bạn phải đợi "+REGISTER_VERIFY_FAIL_WAIT_HOUR+" giờ cho lần gửi tiếp theo.");
            }
            //resend
            SMSAPIService.sendSMS(numberPhone);
            return new ResponseAPIModel(ResponseAPIModel.Status.Success,"");
        } catch (Exception e) {
            System.out.println(e.toString());
            return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Lỗi hệ thống");
        }
    }
    @PostMapping("/registerStep2")
    public ResponseAPIModel registerStep2(@RequestParam String numberPhone,@RequestParam String code) {
        try {
            AccountModel queryAccount = accountService.getByNumberPhone(numberPhone);
            if(queryAccount==null) {
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Số điện thoại chưa được đăng.");
            }

            if(queryAccount.isVerifyNumberPhone()){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Tài khoản đã được xác minh trước đó.");
            }

            if(queryAccount.getRegisterLastTimeResend()+VERIFY_CODE_DURATION<System.currentTimeMillis()){
                //code out of date
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Mã đã quá hạn.");
            }


            if(queryAccount.getRegisterVerifyFailNumber()>=REGISTER_VERIFY_FAIL_MAX_NUMBER){
                //over verify time number
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Thất bại. Bạn đã dùng quá số lần xác minh.");
            }

            if(!SMSAPIService.verifyCode(numberPhone,code)){
                //verify fail
                queryAccount.setRegisterVerifyFailNumber(queryAccount.getRegisterVerifyFailNumber()+1);
            }else{
                queryAccount.setVerifyNumberPhone(true);
            }

            ResponseServiceModel resAction = accountService.update(queryAccount);
            if(resAction.status==ResponseServiceModel.Status.Fail){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,resAction.error);
            }

            return new ResponseAPIModel(ResponseAPIModel.Status.Success,"");
        } catch (Exception e) {
            System.out.println(e.toString());
            return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Lỗi hệ thống");
        }
    }
    @PostMapping("/login")
    public ResponseAPIModel login(@RequestParam String numberPhone, @RequestParam String password) {
        try {
            AccountModel queryAccount = accountService.getByNumberPhone(numberPhone);
            if(queryAccount==null||!queryAccount.isVerifyNumberPhone()) {
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Tài khoản không tồn tại.");
            }else{
                if(!General.verifyPassword(password,queryAccount.getPassword())){
                    return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Sai số điện thoại hoặc mật khẩu.");
                }else{
                    //ok
                    AccountService.AccessToken accessToken = AccountService.AccessToken.general(queryAccount.getId());

                    ArrayList<String> accountAccessTokens = queryAccount.getAccessTokens();
                    accountAccessTokens.add(accessToken.toString());
                    queryAccount.setAccessTokens(accountAccessTokens);

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
    @PostMapping("/resetPasswordStep1")
    public ResponseAPIModel resetPasswordStep1(@RequestParam String numberPhone, @RequestParam String password) {
        try {
            AccountModel queryAccount = accountService.getByNumberPhone(numberPhone);
            if(queryAccount==null){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Số điện thoại chưa được đăng ký.");
            }
            if(queryAccount.getResetPasswordVerifyBanToTime()>System.currentTimeMillis()){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Không thể gửi. Bạn đã gửi quá nhiều lần trong thời gian ngắn.");
            }
            if(queryAccount.getResetPasswordLastTimeResend()+REGISTER_VERIFY_FAIL_WAIT_HOUR*60*60*1000<System.currentTimeMillis()){
                //first time after a long time
                queryAccount.setResetPasswordVerifyBanToTime(0L);
                queryAccount.setResetPasswordSendCodeNumber(0);
            }
            queryAccount.setResetPasswordSendCodeNumber(queryAccount.getRegisterSendCodeNumber()+1);

            if(queryAccount.getResetPasswordSendCodeNumber()>RESET_PASSWORD_SEND_CODE_MAX_NUMBER){
                //over resend time number
                queryAccount.setResetPasswordVerifyBanToTime(System.currentTimeMillis()+RESET_PASSWORD_VERIFY_FAIL_WAIT_HOUR*60*60*1000);
            }else{
                //check valid password
                if(!General.checkValidPassword(password)){
                    return new ResponseAPIModel(ResponseAPIModel.Status.Fail,General.ValidPasswordConstrain);
                }
                String encodedPass = General.hashPassword(password);
                //set password resetting
                queryAccount.setPasswordResetting(encodedPass);

                queryAccount.setResetPasswordLastTimeResend(System.currentTimeMillis());
                queryAccount.setResetPasswordVerifyFailNumber(0);
            }
            ResponseServiceModel resAction = accountService.update(queryAccount);
            if(resAction.status==ResponseServiceModel.Status.Fail){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,resAction.error);
            }

            if(queryAccount.getResetPasswordSendCodeNumber()>REGISTER_SEND_CODE_MAX_NUMBER){
                //not resend
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Bạn đã gửi quá nhiều lần trong thời gian ngắn. Bạn phải đợi "+RESET_PASSWORD_VERIFY_FAIL_WAIT_HOUR+" giờ cho lần gửi tiếp theo.");
            }
            //send
            SMSAPIService.sendSMS(numberPhone);
            return new ResponseAPIModel(ResponseAPIModel.Status.Success,"");
        } catch (Exception e) {
            System.out.println(e.toString());
            return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Lỗi hệ thống");
        }
    }
    @PostMapping("/resetPasswordStep2")
    public ResponseAPIModel resetPasswordStep2(@RequestParam String numberPhone, @RequestParam String code) {
        try {
            AccountModel queryAccount = accountService.getByNumberPhone(numberPhone);
            if(queryAccount==null) {
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Số điện thoại chưa được đăng.");
            }

            if(queryAccount.getResetPasswordLastTimeResend()+VERIFY_CODE_DURATION<System.currentTimeMillis()){
                //code out of date
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Mã đã quá hạn.");
            }

            if(queryAccount.getResetPasswordVerifyFailNumber()>=RESET_PASSWORD_VERIFY_FAIL_MAX_NUMBER){
                //over verify time number
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Thất bại. Bạn đã dùng quá số lần xác minh.");
            }

            if(!SMSAPIService.verifyCode(numberPhone,code)){
                //verify fail
                queryAccount.setResetPasswordVerifyFailNumber(queryAccount.getResetPasswordVerifyFailNumber()+1);
            }else{
                queryAccount.setVerifyNumberPhone(true);
                queryAccount.setPassword(queryAccount.getPasswordResetting());
            }

            ResponseServiceModel resAction = accountService.update(queryAccount);
            if(resAction.status==ResponseServiceModel.Status.Fail){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,resAction.error);
            }

            return new ResponseAPIModel(ResponseAPIModel.Status.Success,"");
        } catch (Exception e) {
            System.out.println(e.toString());
            return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Lỗi hệ thống");
        }
    }

    @PostMapping("/changeName")
    public ResponseAPIModel changeName(HttpServletRequest request, @RequestParam String newName) {
        try {
            AccountService.AccountAuth accountAuth = getAccountAuthFromRequest(request);
            if(accountAuth==null)throw new Exception();

            AccountModel editAccount = accountService.getById(accountAuth.id);
            if(editAccount==null)throw new Exception();

            editAccount.setName(newName);
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

    @PostMapping("/logout")
    public ResponseAPIModel logout(HttpServletRequest request) {
        try {
            AccountService.AccountAuth accountAuth = getAccountAuthFromRequest(request);
            if(accountAuth==null)throw new Exception();

            AccountModel editAccount = accountService.getById(accountAuth.id);
            if(editAccount==null)throw new Exception();

            ArrayList<String> accessTokens = editAccount.getAccessTokens();
            int ind = accessTokens.indexOf(accountAuth.accessToken);
            if(ind!=-1){
                accessTokens.remove(ind);
                editAccount.setAccessTokens(accessTokens);
                ResponseServiceModel resAction = accountService.update(editAccount);
                if(resAction.status==ResponseServiceModel.Status.Fail){
                    return new ResponseAPIModel(ResponseAPIModel.Status.Fail,resAction.error);
                }
            }

            return new ResponseAPIModel(ResponseAPIModel.Status.Success,"");
        } catch (Exception e) {
            System.out.println(e.toString());
            return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Lỗi hệ thống");
        }
    }

    @PostMapping("/logoutAll")
    public ResponseAPIModel logoutAll(HttpServletRequest request) {
        try {
            AccountService.AccountAuth accountAuth = getAccountAuthFromRequest(request);
            if(accountAuth==null)throw new Exception();

            AccountModel editAccount = accountService.getById(accountAuth.id);
            if(editAccount==null)throw new Exception();

            editAccount.setAccessTokens(new ArrayList<>());
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
