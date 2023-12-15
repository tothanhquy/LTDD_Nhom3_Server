package Nhom3.Server.model;


public class CoinsValueNow{
    private static FetchCoinsAPIModel.MainResponse _coinsValueNow=null;

    public static void set(FetchCoinsAPIModel.MainResponse coinsValueNow) {
        _coinsValueNow = coinsValueNow;
    }
    public static FetchCoinsAPIModel.MainResponse get() {
        return _coinsValueNow;
    }
}

