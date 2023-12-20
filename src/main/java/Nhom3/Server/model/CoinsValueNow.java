package Nhom3.Server.model;


public class CoinsValueNow{
    private static FetchCoinsAPIModel.CoinsNow _coinsValueNow=null;

    public static void set(FetchCoinsAPIModel.CoinsNow coinsValueNow) {
        _coinsValueNow = coinsValueNow;
    }
    public static FetchCoinsAPIModel.CoinsNow get() {
        return _coinsValueNow;
    }
    public static FetchCoinsAPIModel.CoinNow getCoin(String coinId){
        for (int i = 0; i < _coinsValueNow.data.size(); i++) {
            if(_coinsValueNow.data.get(i).id.contains(coinId))return _coinsValueNow.data.get(i);
        }
        return null;
    }
}

