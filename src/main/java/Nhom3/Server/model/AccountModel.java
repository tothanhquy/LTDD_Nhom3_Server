package Nhom3.Server.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Document(collection = "accounts")
public class AccountModel {
    @Id
    private String id;

    private String numberPhone;
    private String password;
    private String name;
    private String avatar;
    private ArrayList<String> accessTokens = new ArrayList<>();
    private boolean isVerifyNumberPhone = false;

    private int registerVerifyFailNumber = 0;
    private int registerSendCodeNumber = 0;
    private long registerVerifyBanToTime = 0L;
    private long registerLastTimeResend = 0L;

    private String passwordResetting = "";
    private int resetPasswordVerifyFailNumber = 0;
    private int resetPasswordSendCodeNumber = 0;
    private long resetPasswordVerifyBanToTime = 0L;
    private long resetPasswordLastTimeResend = 0L;

    private int tradeAuthOption = 0;//0 is empty | 1 is otp
    private String pinTrade;
    private int editTradeAuthVerifyPasswordFailNumber = 0;
    private long editTradeAuthVerifyPasswordFailLastTime = 0L;

    private int loginVerifyPasswordFailNumber = 0;
    private long loginVerifyPasswordFailLastTime = 0L;

    public AccountModel() {
    }

    public AccountModel(String id, String numberPhone, String password, String name) {
        this.id = id;
        this.numberPhone = numberPhone;
        this.password = password;
        this.name = name;
    }

    public AccountModel(String numberPhone, String password, String name, long lastTimeResendRegisterCode) {
        this.numberPhone = numberPhone;
        this.password = password;
        this.name = name;
        this.registerLastTimeResend = lastTimeResendRegisterCode;
    }

    public int getLoginVerifyPasswordFailNumber() {
        return loginVerifyPasswordFailNumber;
    }

    public void setLoginVerifyPasswordFailNumber(int loginVerifyPasswordFailNumber) {
        this.loginVerifyPasswordFailNumber = loginVerifyPasswordFailNumber;
    }

    public long getLoginVerifyPasswordFailLastTime() {
        return loginVerifyPasswordFailLastTime;
    }

    public void setLoginVerifyPasswordFailLastTime(long loginVerifyPasswordFailLastTime) {
        this.loginVerifyPasswordFailLastTime = loginVerifyPasswordFailLastTime;
    }

    public int getEditTradeAuthVerifyPasswordFailNumber() {
        return editTradeAuthVerifyPasswordFailNumber;
    }

    public void setEditTradeAuthVerifyPasswordFailNumber(int editTradeAuthVerifyPasswordFailNumber) {
        this.editTradeAuthVerifyPasswordFailNumber = editTradeAuthVerifyPasswordFailNumber;
    }

    public long getEditTradeAuthVerifyPasswordFailLastTime() {
        return editTradeAuthVerifyPasswordFailLastTime;
    }

    public void setEditTradeAuthVerifyPasswordFailLastTime(long editTradeAuthVerifyPasswordFailLastTime) {
        this.editTradeAuthVerifyPasswordFailLastTime = editTradeAuthVerifyPasswordFailLastTime;
    }

    public int getTradeAuthOption() {
        return tradeAuthOption;
    }

    public void setTradeAuthOption(int tradeAuthOption) {
        this.tradeAuthOption = tradeAuthOption;
    }

    public String getPinTrade() {
        return pinTrade;
    }

    public void setPinTrade(String pinTrade) {
        this.pinTrade = pinTrade;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumberPhone() {
        return numberPhone;
    }

    public void setNumberPhone(String numberPhone) {
        this.numberPhone = numberPhone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isVerifyNumberPhone() {
        return isVerifyNumberPhone;
    }

    public void setVerifyNumberPhone(boolean verifyNumberPhone) {
        isVerifyNumberPhone = verifyNumberPhone;
    }

    public ArrayList<String> getAccessTokens() {
        return accessTokens;
    }

    public void setAccessTokens(ArrayList<String> accessTokens) {
        this.accessTokens = accessTokens;
    }

    public int getRegisterVerifyFailNumber() {
        return registerVerifyFailNumber;
    }

    public void setRegisterVerifyFailNumber(int registerVerifyFailNumber) {
        this.registerVerifyFailNumber = registerVerifyFailNumber;
    }

    public int getRegisterSendCodeNumber() {
        return registerSendCodeNumber;
    }

    public void setRegisterSendCodeNumber(int registerSendCodeNumber) {
        this.registerSendCodeNumber = registerSendCodeNumber;
    }

    public long getRegisterVerifyBanToTime() {
        return registerVerifyBanToTime;
    }

    public void setRegisterVerifyBanToTime(long registerVerifyBanToTime) {
        this.registerVerifyBanToTime = registerVerifyBanToTime;
    }

    public String getPasswordResetting() {
        return passwordResetting;
    }

    public void setPasswordResetting(String passwordResetting) {
        this.passwordResetting = passwordResetting;
    }

    public int getResetPasswordVerifyFailNumber() {
        return resetPasswordVerifyFailNumber;
    }

    public void setResetPasswordVerifyFailNumber(int resetPasswordVerifyFailNumber) {
        this.resetPasswordVerifyFailNumber = resetPasswordVerifyFailNumber;
    }

    public int getResetPasswordSendCodeNumber() {
        return resetPasswordSendCodeNumber;
    }

    public void setResetPasswordSendCodeNumber(int resetPasswordSendCodeNumber) {
        this.resetPasswordSendCodeNumber = resetPasswordSendCodeNumber;
    }

    public long getResetPasswordVerifyBanToTime() {
        return resetPasswordVerifyBanToTime;
    }

    public void setResetPasswordVerifyBanToTime(long resetPasswordVerifyBanToTime) {
        this.resetPasswordVerifyBanToTime = resetPasswordVerifyBanToTime;
    }

    public long getRegisterLastTimeResend() {
        return registerLastTimeResend;
    }

    public void setRegisterLastTimeResend(long registerLastTimeResend) {
        this.registerLastTimeResend = registerLastTimeResend;
    }

    public long getResetPasswordLastTimeResend() {
        return resetPasswordLastTimeResend;
    }

    public void setResetPasswordLastTimeResend(long resetPasswordLastTimeResend) {
        this.resetPasswordLastTimeResend = resetPasswordLastTimeResend;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
