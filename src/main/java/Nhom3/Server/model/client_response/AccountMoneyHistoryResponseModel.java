package Nhom3.Server.model.client_response;

import Nhom3.Server.model.AccountMoneyHistoryModel;

import java.util.ArrayList;

public class AccountMoneyHistoryResponseModel {
    public static class Lists{
        public ArrayList<AccountMoneyHistoryModel> items;

        public Lists(ArrayList<AccountMoneyHistoryModel> items) {
            this.items = items;
        }
    }
}
