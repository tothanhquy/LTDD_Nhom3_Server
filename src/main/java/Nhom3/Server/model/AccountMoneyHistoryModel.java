package Nhom3.Server.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "accountMoneyHistorys")
public class AccountMoneyHistoryModel {
    @Id
    public String id;
    @DBRef
    public AccountModel account;
    public String name;
    public long time;
    public float money;

    public AccountMoneyHistoryModel(AccountModel account, String name, long time, float money) {
        this.account = account;
        this.name = name;
        this.time = time;
        this.money = money;
    }
}
