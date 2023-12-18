package Nhom3.Server.model;


public class CoinsValueNow{
    private static FetchCoinsAPIModel.CoinsNow _coinsValueNow=null;

    public static void set(FetchCoinsAPIModel.CoinsNow coinsValueNow) {
        _coinsValueNow = coinsValueNow;
    }
    public static FetchCoinsAPIModel.CoinsNow get() {
        return _coinsValueNow;
    }
}

