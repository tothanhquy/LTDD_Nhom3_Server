package Nhom3.Server.model;

import com.google.gson.Gson;

public class ResponseAPIModel {
    public int code;
    public String status;
    public String error;
    public String data;

    public static class Status{
        public static String Success = "success";
        public static String Fail = "fail";
    }

    public ResponseAPIModel(int code, String status,String error, String data) {
        this.code = code;
        this.status = status;
        this.error = error;
        this.data = data;
    }
    public ResponseAPIModel(String status, String error) {
        this.code = 0;
        this.status = status;
        this.error = error;
        this.data = "";
    }
    public <T>ResponseAPIModel(int code, String status,String error, T data) {
        this.code = code;
        this.status = status;
        this.error = error;
        this.data = new Gson().toJson(data);
    }
    public <T>ResponseAPIModel(int code, String status, T data) {
        this.code = code;
        this.status = status;
        this.error = "";
        if(data instanceof String){
            this.data = (String)data;
        }else{
            this.data = new Gson().toJson(data);
        }
    }
}
