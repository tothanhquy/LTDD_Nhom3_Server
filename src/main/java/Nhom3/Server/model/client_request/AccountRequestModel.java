package Nhom3.Server.model.client_request;

public class AccountRequestModel {
    public static class RegisterStep1{
        public String numberPhone;
        public String password;
        public String name;
    }
    public static class RegisterStep2{
        public String code;
        public String numberPhone;
    }
    public static class Login{
        public String password;
        public String numberPhone;
    }
    public static class ChangeName{
        public String newName;
    }
}
