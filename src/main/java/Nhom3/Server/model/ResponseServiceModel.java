package Nhom3.Server.model;

public class ResponseServiceModel<T> {
    public Status status;
    public String error;

    public T data;

    public ResponseServiceModel(Status status, String error, T data) {
        this.status = status;
        this.error = error;
        this.data = data;
    }

    public static enum Status{
        Success,Fail
    }
}
