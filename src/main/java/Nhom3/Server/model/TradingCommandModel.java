package Nhom3.Server.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Document(collection = "tradingCommands")
public class TradingCommandModel {
    @Id
    public String id;
    @DBRef
    public AccountModel author;

    public String numberPhone;

}
