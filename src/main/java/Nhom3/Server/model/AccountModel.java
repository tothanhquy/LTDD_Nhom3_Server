package Nhom3.Server.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "accounts")
public class AccountModel {
    @Id
    private String id;

    private String numberPhone;
    private String password;
    private String name;
    private String accessToken;
    private boolean isVerifyNumberPhone = false;

    public AccountModel() {
    }

    public AccountModel(String id, String numberPhone, String password, String name) {
        this.id = id;
        this.numberPhone = numberPhone;
        this.password = password;
        this.name = name;
    }

    public AccountModel(String numberPhone, String password, String name) {
        this.numberPhone = numberPhone;
        this.password = password;
        this.name = name;
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

    public String getAccessToken() {
        return accessToken;
    }
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
