package Nhom3.Server.controller;

import Nhom3.Server.model.AccountModel;
import Nhom3.Server.model.ResponseAPIModel;
import Nhom3.Server.model.ResponseServiceModel;
import Nhom3.Server.model.TopChartUserNow;
import Nhom3.Server.service.*;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/account")
public class AccountController extends CoreController{
    private final int REGISTER_VERIFY_FAIL_MAX_NUMBER = 3;
    private final int REGISTER_SEND_CODE_MAX_NUMBER = 3;
    private final int REGISTER_VERIFY_FAIL_WAIT_HOUR = 3;
    private final int VERIFY_CODE_DURATION = 10*60*1000;//10 minus
    private final int RESET_PASSWORD_VERIFY_FAIL_MAX_NUMBER = 300;
    private final int RESET_PASSWORD_SEND_CODE_MAX_NUMBER = 300;
    private final int RESET_PASSWORD_VERIFY_FAIL_WAIT_HOUR = 24;
    private final int EDIT_TRACE_AUTH_VERIFY_PASSWORD_FAIL_MAX_NUMBER = 5;
    private final int EDIT_TRACE_AUTH_VERIFY_PASSWORD_FAIL_WAIT_HOUR = 2;
    private final int LOGIN_VERIFY_PASSWORD_FAIL_MAX_NUMBER = 5;
    private final int LOGIN_VERIFY_PASSWORD_FAIL_WAIT_HOUR = 2;
    private final int EDIT_TRADING_COMMAND_VERIFY_PIN_FAIL_MAX_NUMBER = 5;
    private final int EDIT_TRADING_COMMAND_VERIFY_PIN_FAIL_WAIT_HOUR = 1;
    private final int EDIT_TRADING_COMMAND_VERIFY_CODE_FAIL_MAX_NUMBER = 3;
    private final int EDIT_TRADING_COMMAND_VERIFY_CODE_FAIL_WAIT_HOUR = 1;
    private final float GIVE_MONEY_WHEN_REGISTER = 5000F;
    @Autowired
    AccountService accountService;
    @Autowired
    AccountMoneyHistoryService accountMoneyHistoryService;

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
//            SMSAPIService.sendSMS(numberPhone);
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
//            SMSAPIService.sendSMS(numberPhone);
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

//            if(!SMSAPIService.verifyCode(numberPhone,code)){
            if(!code.equals("123456")){
                //verify fail
                queryAccount.setRegisterVerifyFailNumber(queryAccount.getRegisterVerifyFailNumber()+1);
                ResponseServiceModel resAction = accountService.update(queryAccount);
                if(resAction.status==ResponseServiceModel.Status.Fail){
                    return new ResponseAPIModel(ResponseAPIModel.Status.Fail,resAction.error);
                }
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Mã không đúng hoặc đã quá hạn.");
            }else{
                queryAccount.setVerifyNumberPhone(true);
            }

            queryAccount.setMoneyNow(queryAccount.getMoneyNow()+GIVE_MONEY_WHEN_REGISTER);
            ResponseServiceModel resAction = accountService.update(queryAccount);
            if(resAction.status==ResponseServiceModel.Status.Fail){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,resAction.error);
            }
            String moneyHistoryName= "Quà tặng người mới.";
            resAction = accountMoneyHistoryService.create(queryAccount,moneyHistoryName,GIVE_MONEY_WHEN_REGISTER);
            if(resAction.status==ResponseServiceModel.Status.Fail){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,resAction.error);
            }

            //update top chart
            TopChartUserNow.add(new TopChartUserNow.User(queryAccount.getId(),queryAccount.getMoneyNow()));

            return new ResponseAPIModel(ResponseAPIModel.Status.Success,"");
        } catch (Exception e) {
            System.out.println(e.toString());
            return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Lỗi hệ thống");
        }
    }
    @PostMapping("/login")
    public ResponseAPIModel login(@RequestParam String numberPhone, @RequestParam String password) {
        try {
            List<AccountModel> queryAccounts = accountService.getAll();
            System.out.println(queryAccounts.size());
            AccountModel queryAccount = accountService.getByNumberPhone(numberPhone);
            if(queryAccount==null||!queryAccount.isVerifyNumberPhone()) {
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Sai số điện thoại hoặc mật khẩu.");
            }
            //check fail number
            if(queryAccount.getLoginVerifyPasswordFailNumber()>=LOGIN_VERIFY_PASSWORD_FAIL_MAX_NUMBER
            &&queryAccount.getLoginVerifyPasswordFailLastTime()+LOGIN_VERIFY_PASSWORD_FAIL_WAIT_HOUR*60*60*100>System.currentTimeMillis()){
                //in ban time
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Không thể thực hiện. Bạn đã sai quá nhiều lần trong thời gian ngắn.");
            }

            if(queryAccount.getLoginVerifyPasswordFailLastTime()+LOGIN_VERIFY_PASSWORD_FAIL_WAIT_HOUR*60*60*100<System.currentTimeMillis()){
                //after a long time
                queryAccount.setLoginVerifyPasswordFailNumber(0);
            }

            if(!General.verifyPassword(password,queryAccount.getPassword())){
                queryAccount.setLoginVerifyPasswordFailNumber(queryAccount.getLoginVerifyPasswordFailNumber()+1);
                queryAccount.setLoginVerifyPasswordFailLastTime(System.currentTimeMillis());

                ResponseServiceModel resAction = accountService.update(queryAccount);
                if(resAction.status==ResponseServiceModel.Status.Fail){
                    return new ResponseAPIModel(ResponseAPIModel.Status.Fail,resAction.error);
                }
                if(queryAccount.getLoginVerifyPasswordFailNumber()>=LOGIN_VERIFY_PASSWORD_FAIL_MAX_NUMBER){
                    //fail many time
                    return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Sai số điện thoại hoặc mật khẩu. Bạn đã sai quá nhiều lần. Bạn phải đợi "+LOGIN_VERIFY_PASSWORD_FAIL_WAIT_HOUR+" giờ để thực hiện thao tác tiếp theo. ");
                }
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Sai số điện thoại hoặc mật khẩu.");
            }
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
//            if(queryAccount.getResetPasswordVerifyBanToTime()>System.currentTimeMillis()){
//                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Không thể gửi. Bạn đã gửi quá nhiều lần trong thời gian ngắn.");
//            }
            if(queryAccount.getResetPasswordLastTimeResend()+RESET_PASSWORD_VERIFY_FAIL_WAIT_HOUR*60*60*1000<System.currentTimeMillis()){
                //first time after a long time
                queryAccount.setResetPasswordVerifyBanToTime(0L);
                queryAccount.setResetPasswordSendCodeNumber(0);
            }
            queryAccount.setResetPasswordSendCodeNumber(queryAccount.getResetPasswordSendCodeNumber()+1);

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

            if(queryAccount.getResetPasswordSendCodeNumber()>RESET_PASSWORD_SEND_CODE_MAX_NUMBER){
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

//            if(queryAccount.getResetPasswordVerifyFailNumber()>=RESET_PASSWORD_VERIFY_FAIL_MAX_NUMBER){
//                //over verify time number
//                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Thất bại. Bạn đã dùng quá số lần xác minh.");
//            }

            if(!SMSAPIService.verifyCode(numberPhone,code)){
//            if(!code.equals("123456")){
                //verify fail
                queryAccount.setResetPasswordVerifyFailNumber(queryAccount.getResetPasswordVerifyFailNumber()+1);
                ResponseServiceModel resAction = accountService.update(queryAccount);
                if(resAction.status==ResponseServiceModel.Status.Fail){
                    return new ResponseAPIModel(ResponseAPIModel.Status.Fail,resAction.error);
                }
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Mã không đúng hoặc đã quá hạn.");
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

    @PostMapping("/editAvatar")
    public ResponseAPIModel editAvatar(HttpServletRequest request, @RequestParam("avatar") MultipartFile avatar) {
        try {
            AccountService.AccountAuth accountAuth = getAccountAuthFromRequest(request);
            if(accountAuth==null)throw new Exception();

            AccountModel editAccount = accountService.getById(accountAuth.id);
            if(editAccount==null)throw new Exception();

            if (avatar == null || avatar.isEmpty()) {
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Bạn phải upload image.");
            }

            Path directoryPath = ResourceController.USER_AVATAR_PATH;

            if (!avatar.getContentType().startsWith("image/")) {
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Bạn phải upload image.");
            }

            if (avatar.getSize() > ResourceController.USER_AVATAR_LIMIT_MB_SIZE * 1024L * 1024) {
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Ảnh tối đa "+ResourceController.USER_AVATAR_LIMIT_MB_SIZE+" mb.");
            }

            // Save the file
            if (!avatar.getOriginalFilename().contains(".")) {
                throw new Exception();
            }
            String fileExtension = avatar.getOriginalFilename().substring(avatar.getOriginalFilename().lastIndexOf(".") + 1);
            String fileName = accountAuth.id+"."+System.currentTimeMillis()+"."+fileExtension;
            Files.write(directoryPath.resolve(fileName), avatar.getBytes());

            String oldAvatarName = editAccount.getAvatar();
            editAccount.setAvatar(fileName);

            ResponseServiceModel resAction = accountService.update(editAccount);
            if(resAction.status==ResponseServiceModel.Status.Fail){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,resAction.error);
            }

            //delete old avatar
            try {
                Files.deleteIfExists(directoryPath.resolve(oldAvatarName));
            } catch (Exception e) {}

            return new ResponseAPIModel(ResponseAPIModel.Status.Success,"");
        } catch (Exception e) {
            System.out.println(e.toString());
            return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Lỗi hệ thống");
        }
    }

    @PostMapping("/editTradeAuth")
    public ResponseAPIModel editTradeAuth(HttpServletRequest request, @RequestParam String password, @RequestParam String tradePin) {
        try {
            AccountService.AccountAuth accountAuth = getAccountAuthFromRequest(request);
            if(accountAuth==null)throw new Exception();

            AccountModel editAccount = accountService.getById(accountAuth.id);
            if(editAccount==null)throw new Exception();

            //check fail number
            if(editAccount.getEditTradeAuthVerifyPasswordFailNumber()>=EDIT_TRACE_AUTH_VERIFY_PASSWORD_FAIL_MAX_NUMBER
                    &&editAccount.getEditTradeAuthVerifyPasswordFailLastTime()+EDIT_TRACE_AUTH_VERIFY_PASSWORD_FAIL_WAIT_HOUR*60*60*1000>System.currentTimeMillis()){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Không thể thực hiện. Bạn đã sai quá nhiều lần trước đó.");
            }
            if(editAccount.getEditTradeAuthVerifyPasswordFailLastTime()+EDIT_TRACE_AUTH_VERIFY_PASSWORD_FAIL_WAIT_HOUR*60*60*1000<System.currentTimeMillis()){
                //after a long time
                editAccount.setEditTradeAuthVerifyPasswordFailNumber(0);
            }

            if(!General.verifyPassword(password,editAccount.getPassword())){
                editAccount.setEditTradeAuthVerifyPasswordFailNumber(editAccount.getEditTradeAuthVerifyPasswordFailNumber()+1);
                editAccount.setEditTradeAuthVerifyPasswordFailLastTime(System.currentTimeMillis());

                ResponseServiceModel resAction = accountService.update(editAccount);
                if(resAction.status==ResponseServiceModel.Status.Fail){
                    return new ResponseAPIModel(ResponseAPIModel.Status.Fail,resAction.error);
                }
                if(editAccount.getEditTradeAuthVerifyPasswordFailNumber()>=EDIT_TRACE_AUTH_VERIFY_PASSWORD_FAIL_MAX_NUMBER){
                    //fail many time
                    return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Xác thực thất bại. Bạn đã sai quá nhiều lần. Bạn phải đợi "+EDIT_TRACE_AUTH_VERIFY_PASSWORD_FAIL_WAIT_HOUR+" giờ để thực hiện thao tác tiếp theo. ");
                }
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Xác thực thất bại.");
            }
            //ok

            //check valid pin
            if(!General.checkValidPin(tradePin)){
                //invalid pin
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Mã pin không hợp lệ");
            }
            editAccount.setPinTrade(tradePin);

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

    @GetMapping("/getTradeAuthStatus")
    public ResponseAPIModel getTradeAuthStatus(HttpServletRequest request) {
        try {
            AccountService.AccountAuth accountAuth = getAccountAuthFromRequest(request);
            if(accountAuth==null)throw new Exception();

            AccountModel queryAccount = accountService.getById(accountAuth.id);
            if(queryAccount==null)throw new Exception();

            return new <String>ResponseAPIModel(200,ResponseAPIModel.Status.Success,queryAccount.getPinTrade().isEmpty()?"false":"true");
        } catch (Exception e) {
            System.out.println(e.toString());
            return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Lỗi hệ thống");
        }
    }

    @GetMapping("/checkAuth")
    public ResponseAPIModel checkAuth(HttpServletRequest request) {
        try {
            AccountService.AccountAuth accountAuth = getAccountAuthFromRequest(request);
            if(accountAuth==null)throw new Exception();

            return new <String>ResponseAPIModel(ResponseAPIModel.Status.Success,"");
        } catch (Exception e) {
            System.out.println(e.toString());
            return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Lỗi hệ thống");
        }
    }

    //not http
    public ResponseAPIModel verifyTradingAuthStep1(AccountModel queryAccount, String pin) {
        try {
            if(queryAccount.getEditTradingCommandVerifyPinTradeFailLastTime()+EDIT_TRADING_COMMAND_VERIFY_PIN_FAIL_WAIT_HOUR*60*60*1000>System.currentTimeMillis()&&queryAccount.getEditTradingCommandVerifyPinTradeFailNumber()>=EDIT_TRADING_COMMAND_VERIFY_PIN_FAIL_MAX_NUMBER){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Không thể thực hiện. Bạn đã sai pin quá nhiều lần trong thời gian ngắn.");
            }
            if(queryAccount.getEditTradingCommandVerifyCodeFailLastTime()+EDIT_TRADING_COMMAND_VERIFY_CODE_FAIL_WAIT_HOUR*60*60*1000>System.currentTimeMillis()&&queryAccount.getEditTradingCommandVerifyCodeFailNumber()>=EDIT_TRADING_COMMAND_VERIFY_CODE_FAIL_MAX_NUMBER){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Không thể thực hiện. Bạn đã sai quá nhiều lần trong thời gian ngắn.");
            }
            if(queryAccount.getEditTradingCommandVerifyPinTradeFailLastTime()+EDIT_TRADING_COMMAND_VERIFY_PIN_FAIL_WAIT_HOUR*60*60*1000<System.currentTimeMillis()){
                //first time after a long time
                queryAccount.setEditTradingCommandVerifyPinTradeFailNumber(0);
            }
            if(queryAccount.getPinTrade().isEmpty()){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Không thể thực hiện. Bạn chưa thiết lập mã pin giao dịch.");
            }
            if(!queryAccount.getPinTrade().equals(pin)){
                //verify fail
                queryAccount.setEditTradingCommandVerifyPinTradeFailNumber(queryAccount.getEditTradingCommandVerifyPinTradeFailNumber()+1);
                ResponseServiceModel resAction = accountService.update(queryAccount);
                if(resAction.status==ResponseServiceModel.Status.Fail){
                    return new ResponseAPIModel(ResponseAPIModel.Status.Fail,resAction.error);
                }
                if(queryAccount.getEditTradingCommandVerifyPinTradeFailNumber()>EDIT_TRADING_COMMAND_VERIFY_PIN_FAIL_MAX_NUMBER){
                    return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Bạn đã sai quá nhiều lần trong thời gian ngắn. Bạn phải đợi "+EDIT_TRADING_COMMAND_VERIFY_PIN_FAIL_WAIT_HOUR+" giờ cho lần gửi tiếp theo.");
                }
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Sai mã pin");
            }else{
                queryAccount.setEditTradingCommandVerifyPinTradeFailNumber(0);
                queryAccount.setEditTradingCommandSendCodeLastTime(System.currentTimeMillis());
                queryAccount.setEditTradingCommandVerifyCodeFailNumber(0);

                SMSAPIService.sendSMS(queryAccount.getNumberPhone());

                ResponseServiceModel resAction = accountService.update(queryAccount);
                if(resAction.status==ResponseServiceModel.Status.Fail){
                    return new ResponseAPIModel(ResponseAPIModel.Status.Fail,resAction.error);
                }
                return new ResponseAPIModel(ResponseAPIModel.Status.Success,"");
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Lỗi hệ thống");
        }
    }
    //not http
    public ResponseAPIModel verifyTradingAuthStep2(AccountModel queryAccount, String code) {
        try {
//            return new ResponseAPIModel(ResponseAPIModel.Status.Success,"");//command to test
////
            if(queryAccount.getEditTradingCommandVerifyCodeFailLastTime()+EDIT_TRADING_COMMAND_VERIFY_CODE_FAIL_WAIT_HOUR*60*60*1000>System.currentTimeMillis()&&queryAccount.getEditTradingCommandVerifyCodeFailNumber()>=EDIT_TRADING_COMMAND_VERIFY_CODE_FAIL_MAX_NUMBER){
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Không thể thực hiện. Bạn đã sai quá nhiều lần trong thời gian ngắn.");
            }
            if(queryAccount.getEditTradingCommandSendCodeLastTime()+VERIFY_CODE_DURATION<System.currentTimeMillis()){
                //code out of date
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Mã đã quá hạn.");
            }

            if(queryAccount.getEditTradingCommandVerifyCodeFailLastTime()+EDIT_TRADING_COMMAND_VERIFY_CODE_FAIL_WAIT_HOUR*60*60*1000<System.currentTimeMillis()){
                //first time after a long time
                queryAccount.setEditTradingCommandVerifyCodeFailNumber(0);
            }

            if(!SMSAPIService.verifyCode(queryAccount.getNumberPhone(),code)){
                //verify fail
                queryAccount.setEditTradingCommandVerifyCodeFailLastTime(System.currentTimeMillis());
                queryAccount.setEditTradingCommandVerifyCodeFailNumber(queryAccount.getEditTradingCommandVerifyCodeFailNumber()+1);
                ResponseServiceModel resAction = accountService.update(queryAccount);
                if(resAction.status==ResponseServiceModel.Status.Fail){
                    return new ResponseAPIModel(ResponseAPIModel.Status.Fail,resAction.error);
                }
                if(queryAccount.getEditTradingCommandVerifyCodeFailNumber()>EDIT_TRADING_COMMAND_VERIFY_CODE_FAIL_MAX_NUMBER){
                    return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Bạn đã sai quá nhiều lần trong thời gian ngắn. Bạn phải đợi "+EDIT_TRADING_COMMAND_VERIFY_CODE_FAIL_WAIT_HOUR+" giờ cho lần gửi tiếp theo.");
                }
                return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Sai mã OTP");
            }else{
                queryAccount.setEditTradingCommandVerifyCodeFailNumber(0);

                ResponseServiceModel resAction = accountService.update(queryAccount);
                if(resAction.status==ResponseServiceModel.Status.Fail){
                    return new ResponseAPIModel(ResponseAPIModel.Status.Fail,resAction.error);
                }
                return new ResponseAPIModel(ResponseAPIModel.Status.Success,"");
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            return new ResponseAPIModel(ResponseAPIModel.Status.Fail,"Lỗi hệ thống");
        }
    }

}
