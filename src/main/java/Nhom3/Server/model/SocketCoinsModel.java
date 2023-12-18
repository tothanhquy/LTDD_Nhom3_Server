package Nhom3.Server.model;

import java.util.ArrayList;
import java.util.Locale;

public class SocketCoinsModel {
    public static class Coin{
        public String id;
        public int rank;
        public String symbol;
        public String icon;
        public String name;
        public float volumeUsd24Hr;
        public float priceUsd;
        public float changePercent24Hr;
        public float vwap24Hr;

        public Coin(String id, int rank, String symbol, String name, float volumeUsd24Hr, float priceUsd, float changePercent24Hr, float vwap24Hr) {
            this.id = id;
            this.rank = rank;
            this.icon = "https://assets.coincap.io/assets/icons/"+symbol.toLowerCase()+"@2x.png";
            this.symbol = symbol;
            this.name = name;
            this.volumeUsd24Hr = volumeUsd24Hr;
            this.priceUsd = priceUsd;
            this.changePercent24Hr = changePercent24Hr;
            this.vwap24Hr = vwap24Hr;
        }
    }
    public static class Coins{
        public ArrayList<Coin> data;
        public Long timestamp;

        public Coins(ArrayList<Coin> data, Long timestamp) {
            this.data = data;
            this.timestamp = timestamp;
        }

        public Coins() {
        }
    }
}
