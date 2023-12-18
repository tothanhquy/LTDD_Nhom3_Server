package Nhom3.Server.model;

import java.util.ArrayList;

public class FetchCoinsAPIModel {
    public class CoinsNowCrude{
        public ArrayList<CoinNowCrude> data;
        public Long timestamp;

        public CoinsNowCrude(ArrayList<CoinNowCrude> data, Long timestamp) {
            this.data = data;
            this.timestamp = timestamp;
        }
    }
    public class CoinsNow{
        public ArrayList<CoinNow> data;
        public Long timestamp;

        public CoinsNow(ArrayList<CoinNow> data, Long timestamp) {
            this.data = data;
            this.timestamp = timestamp;
        }
        public CoinsNow(CoinsNowCrude crude) {
            this.data = new ArrayList<>();
            crude.data.forEach(e->this.data.add(new CoinNow(e)));
            this.timestamp = crude.timestamp;
        }
    }
    public class CoinNowCrude{
        public String id;
        public String rank;
        public String symbol;
        public String name;
        public String supply;
        public String maxSupply;
        public String marketCapUsd;
        public String volumeUsd24Hr;
        public String priceUsd;
        public String changePercent24Hr;
        public String vwap24Hr;
        public String explorer;

        public CoinNowCrude(String id, String rank, String symbol, String name, String supply, String maxSupply, String marketCapUsd, String volumeUsd24Hr, String priceUsd, String changePercent24Hr, String vwap24Hr, String explorer) {
            this.id = id;
            this.rank = rank;
            this.symbol = symbol;
            this.name = name;
            this.supply = supply;
            this.maxSupply = maxSupply;
            this.marketCapUsd = marketCapUsd;
            this.volumeUsd24Hr = volumeUsd24Hr;
            this.priceUsd = priceUsd;
            this.changePercent24Hr = changePercent24Hr;
            this.vwap24Hr = vwap24Hr;
            this.explorer = explorer;
        }
    }
    public class CoinNow{
        public String id;
        public int rank;
        public String symbol;
        public String name;
        public float supply;
        public float maxSupply;
        public float marketCapUsd;
        public float volumeUsd24Hr;
        public float priceUsd;
        public float changePercent24Hr;
        public float vwap24Hr;
        public String explorer;

        public CoinNow(String id, int rank, String symbol, String name, float supply, float maxSupply, float marketCapUsd, float volumeUsd24Hr, float priceUsd, float changePercent24Hr, float vwap24Hr, String explorer) {
            this.id = id;
            this.rank = rank;
            this.symbol = symbol;
            this.name = name;
            this.supply = supply;
            this.maxSupply = maxSupply;
            this.marketCapUsd = marketCapUsd;
            this.volumeUsd24Hr = volumeUsd24Hr;
            this.priceUsd = priceUsd;
            this.changePercent24Hr = changePercent24Hr;
            this.vwap24Hr = vwap24Hr;
            this.explorer = explorer;
        }
        public CoinNow(CoinNowCrude crude) {
            this.id = crude.id;
            this.rank = crude.rank==null?0:Integer.parseInt(crude.rank);
            this.symbol = crude.symbol;
            this.name = crude.name;
            this.supply = crude.supply==null?0:Float.parseFloat(crude.supply);
            this.maxSupply = crude.maxSupply==null?0:Float.parseFloat(crude.maxSupply);
            this.marketCapUsd = crude.marketCapUsd==null?0:Float.parseFloat(crude.marketCapUsd);
            this.volumeUsd24Hr = crude.volumeUsd24Hr==null?0:Float.parseFloat(crude.volumeUsd24Hr);
            this.priceUsd = crude.priceUsd==null?0:Float.parseFloat(crude.priceUsd);
            this.changePercent24Hr = crude.changePercent24Hr==null?0:Float.parseFloat(crude.changePercent24Hr);
            this.vwap24Hr = crude.vwap24Hr==null?0:Float.parseFloat(crude.vwap24Hr);
            this.explorer = crude.explorer;
        }

    }

    public static int COIN_M1_HISTORY_MAX_DAY = 1;
    public static int COIN_M5_HISTORY_MAX_DAY = 5;
    public static int COIN_M15_HISTORY_MAX_DAY = 7;
    public static int COIN_M30_HISTORY_MAX_DAY = 14;
    public static int COIN_H1_HISTORY_MAX_DAY = 30;
    public static int COIN_H2_HISTORY_MAX_DAY = 61;
    public static int COIN_H6_HISTORY_MAX_DAY = 183;
    public static int COIN_H12_HISTORY_MAX_DAY = 365;
    public static class CoinM1HistoryCrude{
        public String priceUsd;
        public long time;
        public String circulatingSupply;
        public String date;
//        "priceUsd":"23079.3110167513378291",
//        "time":1674950460000,
//        "circulatingSupply":"19275550.0000000000000000",
//        "date":"2023-01-29T00:01:00.000Z"

        public CoinM1HistoryCrude(String priceUsd, long time, String circulatingSupply, String date) {
            this.priceUsd = priceUsd;
            this.time = time;
            this.circulatingSupply = circulatingSupply;
            this.date = date;
        }
    }
    public static class CoinM1History{
        public float priceUsd;
        public long time;
        public String circulatingSupply;
        public String date;
        public CoinM1History(float priceUsd, long time, String circulatingSupply, String date) {
            this.priceUsd = priceUsd;
            this.time = time;
            this.circulatingSupply = circulatingSupply;
            this.date = date;
        }
        public CoinM1History(CoinM1HistoryCrude crude) {
            this.priceUsd = crude.priceUsd==null?0L:Float.parseFloat(crude.priceUsd);
            this.time = crude.time;
            this.circulatingSupply = crude.circulatingSupply;
            this.date = crude.date;
        }
    }
    public static class CoinsM1HistoryCrude{
        public ArrayList<CoinM1HistoryCrude> data;
        public long timestamp;

        public CoinsM1HistoryCrude(ArrayList<CoinM1HistoryCrude> data, long timestamp) {
            this.data = data;
            this.timestamp = timestamp;
        }
    }
    public static class CoinsM1History{
        public ArrayList<CoinM1History> data;
        public long timestamp;

        public CoinsM1History(ArrayList<CoinM1History> data, long timestamp) {
            this.data = data;
            this.timestamp = timestamp;
        }
        public CoinsM1History(CoinsM1HistoryCrude crude) {
            this.data = new ArrayList<>();
            crude.data.forEach(e->this.data.add(new CoinM1History(e)));
            this.timestamp = crude.timestamp;
        }

        public CoinsM1History() {
            data = new ArrayList<>();
            timestamp=0;
        }
    }

    public static class CoinD1HistoryCrude{
        public String priceUsd;
        public long time;
        public String date;
//        "priceUsd":"23079.3110167513378291",
//        "time":1674950460000,
//        "date":"2023-01-29T00:01:00.000Z"

        public CoinD1HistoryCrude(String priceUsd, long time, String date) {
            this.priceUsd = priceUsd;
            this.time = time;
            this.date = date;
        }
    }
    public static class CoinD1History{
        public float priceUsd;
        public long time;
        public String date;
        public CoinD1History(float priceUsd, long time, String date) {
            this.priceUsd = priceUsd;
            this.time = time;
            this.date = date;
        }
        public CoinD1History(CoinD1HistoryCrude crude) {
            this.priceUsd = crude.priceUsd==null?0L:Float.parseFloat(crude.priceUsd);
            this.time = crude.time;
            this.date = crude.date;
        }
    }
    public static class CoinsD1HistoryCrude{
        public ArrayList<CoinD1HistoryCrude> data;
        public long timestamp;

        public CoinsD1HistoryCrude(ArrayList<CoinD1HistoryCrude> data, long timestamp) {
            this.data = data;
            this.timestamp = timestamp;
        }
    }
    public static class CoinsD1History{
        public ArrayList<CoinD1History> data;
        public long timestamp;

        public CoinsD1History(ArrayList<CoinD1History> data, long timestamp) {
            this.data = data;
            this.timestamp = timestamp;
        }
        public CoinsD1History(CoinsD1HistoryCrude crude) {
            this.data = new ArrayList<>();
            crude.data.forEach(e->this.data.add(new CoinD1History(e)));
            this.timestamp = crude.timestamp;
        }
    }


}
