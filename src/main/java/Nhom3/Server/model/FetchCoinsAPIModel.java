package Nhom3.Server.model;

import java.util.ArrayList;

public class FetchCoinsAPIModel {
    public class MainResponseCrude{
        public ArrayList<CoinCrude> data;
        public Long timestamp;

        public MainResponseCrude(ArrayList<CoinCrude> data, Long timestamp) {
            this.data = data;
            this.timestamp = timestamp;
        }
    }
    public class MainResponse{
        public ArrayList<Coin> data;
        public Long timestamp;

        public MainResponse(ArrayList<Coin> data, Long timestamp) {
            this.data = data;
            this.timestamp = timestamp;
        }
        public MainResponse(MainResponseCrude crude) {
            this.data = new ArrayList<>();
            crude.data.forEach(e->this.data.add(new Coin(e)));
            this.timestamp = crude.timestamp;
        }
    }
    public class CoinCrude{
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

        public CoinCrude(String id, String rank, String symbol, String name, String supply, String maxSupply, String marketCapUsd, String volumeUsd24Hr, String priceUsd, String changePercent24Hr, String vwap24Hr, String explorer) {
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

    public class Coin{
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

        public Coin(String id, int rank, String symbol, String name, float supply, float maxSupply, float marketCapUsd, float volumeUsd24Hr, float priceUsd, float changePercent24Hr, float vwap24Hr, String explorer) {
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
        public Coin(CoinCrude crude) {
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

}
