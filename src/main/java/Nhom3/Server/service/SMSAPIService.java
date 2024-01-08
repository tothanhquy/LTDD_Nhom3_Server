package Nhom3.Server.service;

import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;

public class SMSAPIService {
    private static class TokenAPI{
        public static class QuyTo{
            public final String ACCOUNT_SID = "ACcc947d23c0d3c3190abe60a12837da01";
            public  final String AUTH_TOKEN = "f99e136983e840c994606de79dc5ea42";
            public final String SERVICE_SID = "VA709b5a740abe23cc2ba257e9b95878cc";
            public final String NUMBER_PHONE = "+84868964631";
        }
        public static class Quan{
            public final String ACCOUNT_SID = "ACcc947d23c0d3c3190abe60a12837da01";
            public final String AUTH_TOKEN = "f99e136983e840c994606de79dc5ea42";
            public final String SERVICE_SID = "VA709b5a740abe23cc2ba257e9b95878cc";
            public final String NUMBER_PHONE = "+84868964631";
        }
        public static class Thuc{
            public final String ACCOUNT_SID = "AC7e77f325ddfeab1ca25b58da892c358d";
            public final String AUTH_TOKEN = "6404b7712acc9919813f7c7d775b5193";
            public final String SERVICE_SID = "VA3fc896d445cb4078271824c753c66864";
            public final String NUMBER_PHONE = "+84901633324";
        }
        public static class Ban{
            public final String ACCOUNT_SID = "AC403c6995ece12c3a996cb0afa6a9137e";
            public final String AUTH_TOKEN = "35c5ffb3d3eb1b2d827c86fed98282c3";
            public final String SERVICE_SID = "VA48db51f55a3f59842039951831729f81";
            public final String NUMBER_PHONE = "+84868964631";
        }
        public static class Binh{
            public final String ACCOUNT_SID = "ACf80f1d7732acc9a1530ca2dce7fb52b1";
            public final String AUTH_TOKEN = "6e30398cbfd09dc71efdce3e56226ba0";
            public final String SERVICE_SID = "VA19eca2d3599644e5ac541833cc87a301";
            public final String NUMBER_PHONE = "+84918363502";
        }
    }

//    private static TokenAPI.QuyTo TOKEN_API = new TokenAPI.QuyTo();
    private static TokenAPI.Ban TOKEN_API = new TokenAPI.Ban();

    static {
        Twilio.init(TOKEN_API.ACCOUNT_SID, TOKEN_API.AUTH_TOKEN);
    }

    //code co hieu luc trong 10 phut

//    public static void main(String arg[]){
////        sendSMS("+84868964631");
//
//        verifyCode("+84868964631","316397");
//    }

    public static void sendSMS(String numberPhone) {
        Verification verification = Verification.creator(
            TOKEN_API.SERVICE_SID,
                "+"+numberPhone,
            "sms"
        ).create();

//        System.out.println("send: "+verification.getStatus());
    }
    public static boolean verifyCode(String numberPhone, String code) {
        try{
            VerificationCheck verificationCheck = VerificationCheck.creator(
                            TOKEN_API.SERVICE_SID
                    ).setTo("+"+numberPhone)
                    .setCode(code)
                    .create();

//            System.out.println("check: "+verificationCheck.getStatus());
            return verificationCheck.getStatus().equals("approved");
        }catch(Exception e){
            return false;
        }
    }

}
