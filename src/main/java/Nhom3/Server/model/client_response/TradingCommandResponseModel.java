package Nhom3.Server.model.client_response;

import Nhom3.Server.model.AccountMoneyHistoryModel;

import java.util.ArrayList;

public class TradingCommandResponseModel {
    public static class Created{
        public String newId;

        public Created(String newId) {
            this.newId = newId;
        }
    }
}
