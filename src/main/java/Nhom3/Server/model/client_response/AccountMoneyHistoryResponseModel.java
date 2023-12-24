package Nhom3.Server.model.client_response;

import Nhom3.Server.model.AccountModel;
import Nhom3.Server.model.AccountMoneyHistoryModel;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.ArrayList;
import java.util.List;

public class AccountMoneyHistoryResponseModel {
    public static class Item{
        public String id;
        public String name;
        public long time;
        public float money;

        public Item(String id, String name, long time, float money) {
            this.id = id;
            this.name = name;
            this.time = time;
            this.money = money;
        }
    }
    public static class Lists{
        public List<Item> items;

        public Lists(List<AccountMoneyHistoryModel> items) {
            this.items = items.stream().map(e->new Item(e.id,e.name,e.time,e.money)).toList();
        }
    }
}
