package Nhom3.Server.service;

import Nhom3.Server.model.AccountModel;
import Nhom3.Server.model.ResponseServiceModel;
import Nhom3.Server.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountService {
    public static class JWTContent{
        public String id;
        public String accessToken;

        public JWTContent(String id, String accessToken) {
            this.id = id;
            this.accessToken = accessToken;
        }
    }
    public static class AccountAuth{
        public String id;
        public String name;
        public String numberPhone;

        public AccountAuth(String id, String name, String numberPhone) {
            this.id = id;
            this.name = name;
            this.numberPhone = numberPhone;
        }
    }
    public static class AccessToken{
        public static final Long ACCESS_TOKEN_TIME = 1000L*60*60*24*30*3;//3 month
        public String id;
        public String token;
        public long time;

        public AccessToken(String id, String token, long time) {
            this.id = id;
            this.token = token;
            this.time = time;
        }

        @Override
        public String toString() {
            return id+"-"+token+"-"+time;
        }
        public static AccessToken parse(String str){
            try{
                String split[] = str.split("-");
                return new AccessToken(split[0],split[1],Long.parseLong(split[2]));
            }catch(Exception e){
                return null;
            }
        }
        public static AccessToken general(String id){
            String token = General.getRandomString(20);
            Long time = System.currentTimeMillis()+ACCESS_TOKEN_TIME;
            return new AccessToken(id,token,time);
        }
    }

    @Autowired
    AccountRepository accountRepository;

    public AccountAuth checkAndGetAccountAuth(String jwt){
        try{
            JWTContent jwtParse = JWTService.parse(jwt);
            if(jwtParse==null)return null;

            AccountModel account = getById(jwtParse.id);
            if (account!=null) {
                if(!account.getAccessToken().equals(jwtParse.accessToken)){
                    return null;
                }else if(!account.isVerifyNumberPhone()){
                    return null;
                }else{
                    AccessToken accessToken = AccessToken.parse(jwtParse.accessToken);
                    if(accessToken.time<System.currentTimeMillis()){
                        //over date
                        return null;
                    }else{
                        return new AccountAuth(account.getId(),account.getName(),account.getNumberPhone());
                    }
                }
            }else{
                return null;
            }
        }catch(Exception e){
            System.out.println(e.toString());
            return null;
        }
    }

    public AccountModel getByNumberPhone(String numberPhone){
        Optional<AccountModel> optional = accountRepository.findByNumberPhone(numberPhone);
        if (optional.isPresent()) {
            AccountModel account = optional.get();
            return account;
        }else{
            return null;
        }
    }
    public AccountModel getById(String id){
        Optional<AccountModel> optional = accountRepository.findById(id);
        if (optional.isPresent()) {
            AccountModel account = optional.get();
            return account;
        }else{
            return null;
        }
    }
    public ResponseServiceModel update(AccountModel accountModel){
        try {
            accountRepository.save(accountModel);
            return new ResponseServiceModel<String>(ResponseServiceModel.Status.Success,"","");
        }catch(Exception e){
            return new ResponseServiceModel<String>(ResponseServiceModel.Status.Fail,"Lỗi hệ thống","");
        }
    }
    public ResponseServiceModel register(String numberPhone, String password, String name){
        try {
            if(getByNumberPhone(numberPhone)!=null){
                return new ResponseServiceModel<String>(ResponseServiceModel.Status.Fail,"Số điện thoại đã được đăng ký từ trước.","");
            }else{
                //check valid number phone
                if(!General.checkValidNumberPhone(numberPhone)){
                    return new ResponseServiceModel<String>(ResponseServiceModel.Status.Fail,"Số điện thoại không hợp lệ. Chỉ cho phép số điện thoại tại việt nam","");
                }
                //check valid password
                if(!General.checkValidPassword(password)){
                    return new ResponseServiceModel<String>(ResponseServiceModel.Status.Fail,General.ValidPasswordConstrain,"");
                }
                //hash password
                String encodedPass = General.hashPassword(password);
                AccountModel account = new AccountModel(numberPhone,encodedPass,name);
                accountRepository.save(account);
                return new ResponseServiceModel<String>(ResponseServiceModel.Status.Success,"","");
            }
        }catch(Exception e){
            return new ResponseServiceModel<String>(ResponseServiceModel.Status.Fail,"Lỗi hệ thống","");
        }
    }
    public ResponseServiceModel setVerifyNumberPhone(String id){
        try {
            Optional<AccountModel> optional = accountRepository.findById(id);
            if (optional.isPresent()) {
                AccountModel account = optional.get();
                if(account.isVerifyNumberPhone()){
                    return new ResponseServiceModel<String>(ResponseServiceModel.Status.Success,"","");
                }else{
                    account.setVerifyNumberPhone(true);
                    accountRepository.save(account);
                    return new ResponseServiceModel<String>(ResponseServiceModel.Status.Success,"","");
                }
            }else{
                return null;
            }
        }catch(Exception e){
            return new ResponseServiceModel<String>(ResponseServiceModel.Status.Fail,"Lỗi hệ thống","");
        }
    }
}
