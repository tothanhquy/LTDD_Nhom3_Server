package Nhom3.Server.model;


import java.util.ArrayList;
import java.util.Comparator;

public class TopChartUserNow {
    public static class User{
        public String id;
        public float moneyNow;

        public User(String id, float moneyNow) {
            this.id = id;
            this.moneyNow = moneyNow;
        }

        @Override
        public String toString() {
            return "User{" +
                    "id='" + id + '\'' +
                    ", moneyNow=" + moneyNow +
                    '}';
        }
    }
    private static ArrayList<User> sortedTopUsers=new ArrayList<>();

    private static synchronized void sort(){
        sortedTopUsers.sort(new Comparator<User>() {
            @Override
            public int compare(User a, User b) {
                return (int) (b.moneyNow-a.moneyNow);
            }
        });
//        for (int i = 0; i < sortedTopUsers.size(); i++) {
//            System.out.println(sortedTopUsers.get(i).toString());
//        }
    }
    public static void setInit(ArrayList<User> items){
        sortedTopUsers = items;
        sort();
    }

    public static int count(){
        return sortedTopUsers.size();
    }

    private static synchronized void _change(boolean isUpdate, User item){
        int ind = getIndex(item.id);
        if((isUpdate&&ind==-1)||(!isUpdate&&ind!=-1))return;
        if(!isUpdate)sortedTopUsers.add(item);
        else{
            sortedTopUsers.get(ind).moneyNow=item.moneyNow;
        }
        sort();
    }
    public static void update(User item){
        new Thread(()->{
            _change(true,item);
        }).start();
    }
    public static void add(User item) {
        new Thread(()->{
            _change(false,item);
        }).start();
    }
    public static int getIndex(String userId){
        for (int i = 0; i < sortedTopUsers.size(); i++) {
            if(sortedTopUsers.get(i).id.equals(userId))return i;
        }
        return -1;
    }
    public static ArrayList<User> getTopUsers(int start, int end){
        if(start<0||start>=count())return new ArrayList<>();
        if(end<0||end<start)return new ArrayList<>();
        if(end>=count())end=count()-1;
        return new ArrayList<>(sortedTopUsers.subList(start,end+1));
    }
}

