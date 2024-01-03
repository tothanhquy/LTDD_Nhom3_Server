package Nhom3.Server.model.client_response;

import java.util.ArrayList;

public class CoinResponseModel {
    public static class CoinHistoryLineChart {
        public float priceUsd;
        public long time;

        public CoinHistoryLineChart(float priceUsd, long time) {
            this.priceUsd = priceUsd;
            this.time = time;
        }
    }
    public static class CoinsHistoryLineChart {
        public ArrayList<CoinHistoryLineChart> data=new ArrayList<>();

        public CoinsHistoryLineChart(ArrayList<CoinHistoryLineChart> data) {
            this.data = data;
        }
        public CoinsHistoryLineChart() {
        }
    }
    public static class CoinHistoryCandleChart {
        public float open;
        public float close;
        public float height;
        public float low;
        public long time;

        public CoinHistoryCandleChart(float open, float close, float height, float low, long time) {
            this.open = open;
            this.close = close;
            this.height = height;
            this.low = low;
            this.time = time;
        }
    }
    public static class CoinsHistoryCandleChart {
        public ArrayList<CoinHistoryCandleChart> data=new ArrayList<>();

        public CoinsHistoryCandleChart(ArrayList<CoinHistoryCandleChart> data) {
            this.data = data;
        }
        public CoinsHistoryCandleChart() {
        }
    }

}
