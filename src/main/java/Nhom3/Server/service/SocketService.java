package Nhom3.Server.service;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Service
public class SocketService implements Runnable{
    @Autowired
    static AccountService accountService;
    private static final int PORT = 8081;
    public static ServerSocket serverSocket;
    private static ArrayList<Client> clients = new ArrayList<>();
    private static ArrayList<Event> events = new ArrayList<>();


    static {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Socket server is listening on port " + PORT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");
//                clientSocket.getOutputStream().write("hello client".getBytes());
//                clientSocket.getOutputStream().flush();
                Client client = new Client(clientSocket, new ServerReceiveAction(), new ClientDisconnectAction());
                clients.add(client);

                new Thread(client).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendToRoom(String event, String roomName, String content){
        SocketRequestAndResponse response = new SocketRequestAndResponse(event,content);
        String responseContent = new Gson().toJson(response);
        for (int i=0;i<clients.size();i++){
            if(clients.get(i).hasRoom(roomName)){
                clients.get(i).send(responseContent);
            }
        }
    }
    public static void sendToAll(String event, String content){
        SocketRequestAndResponse response = new SocketRequestAndResponse(event,content);
        String responseContent = new Gson().toJson(response);
        for (int i=0;i<clients.size();i++){
            clients.get(i).send(responseContent);
        }
    }
    public static void registerEvent(String actorName, String eventName, EventCallback eventCallback){
        int ind=-1;
        //check exist
        for(int i=0;i< events.size();i++){
            if(events.get(i).actorName==actorName&&events.get(i).eventName==eventName){
                ind=i;break;
            }
        }
        if(ind==-1){
            events.add(new Event(actorName,eventName,eventCallback));
        }
    }
    public static void removeEvent(String actorName, String eventName){
        int ind=-1;
        //check exist
        for(int i=0;i< events.size();i++){
            if(events.get(i).actorName==actorName&&events.get(i).eventName==eventName){
                ind=i;break;
            }
        }
        if(ind!=-1){
            events.remove(ind);
        }
    }
    public static void removeEventsOfActor(String actorName){
        while(true){
            int ind=-1;
            //check exist
            for(int i=0;i< events.size();i++){
                if(events.get(i).actorName==actorName){
                    ind=i;break;
                }
            }
            if(ind!=-1){
                events.remove(ind);
            }else{
                break;
            }
        }

    }
    public static class ServerReceiveAction implements ServerReceive{
        @Override
        public void receive(String id, String message) {
            handelServerReceiveRequest(id,message);
        }
    }
    public static class ClientDisconnectAction implements ClientDisconnect{
        @Override
        public void disconnect(String id) {
            int ind=-1;
            for(int i=0;i<clients.size();i++){
                if(clients.get(i).id==id){
                    ind=i;
                    break;
                }
            }
            if(ind!=-1){
                clients.remove(ind);
            }
        }
    }

    public static void handelServerReceiveRequest(String id, String message){
        System.out.println("receive:"+message);
        if(message==null||message.isEmpty())return;

        try{
            Gson gson = new Gson();
            SocketRequestAndResponse request = gson.fromJson(message, SocketRequestAndResponse.class);
            //event callback
            for(int i=0;i<events.size();i++){
                if(events.get(i).eventName==request.event){
                    events.get(i).eventCallback.handle(request.content);
                }
            }
            //room
            handelRoomRequest(id,request);
        }catch(Exception e){}
    }
    public static void handelRoomRequest(String id, SocketRequestAndResponse request){
        Gson gson = new Gson();
//        SocketRequestAndResponse request = gson.fromJson(message, SocketRequestAndResponse.class);

        if(request.event.equals(EventNames.Receive.JoinPersonalRoom)){
            String jwt = request.content;
            AccountService.AccountAuth accountAuth = accountService.checkAndGetAccountAuth(jwt);
            if(accountAuth!=null){
                int ind = findIndexOfClientById(id);
                if(ind!=-1){
                    String roomName = RoomNamesPrefix.PersonalRoom+accountAuth.id;
                    if(!clients.get(ind).hasRoom(roomName)){
                        clients.get(ind).joinRoom(roomName);
                    }
                }

            }
        }else if(request.event.equals(EventNames.Receive.OutPersonalRoom)){
            String jwt = request.content;
            AccountService.AccountAuth accountAuth = accountService.checkAndGetAccountAuth(jwt);
            if(accountAuth!=null){
                int ind = findIndexOfClientById(id);
                if(ind!=-1){
                    String roomName = RoomNamesPrefix.PersonalRoom+accountAuth.id;
                    if(!clients.get(ind).hasRoom(roomName)){
                        clients.get(ind).outRoom(roomName);
                    }
                }

            }
        }

    }
    public static int findIndexOfClientById(String id){
        int ind=-1;
        for(int i=0;i<clients.size();i++){
            if(clients.get(i).id.contains(id)){
                ind=i;break;
            }
        }
        return ind;
    }

    public static class SocketRequestAndResponse{
        public String event;
        public String content;

        public SocketRequestAndResponse(String event, String content) {
            this.event = event;
            this.content = content;
        }
    }

    public static interface ServerReceive{
        public void receive(String id, String message);
    }
    public static interface ClientDisconnect{
        public void disconnect(String id);
    }
    private static class Client implements Runnable{
        public final Socket clientSocket;
        public final String id;
        private final PrintWriter outputWriter;
        private final InputStream inputStream;
        private final ServerReceive serverReceive;
        private final ClientDisconnect clientDisconnect;
        private ArrayList<String> rooms;

        public Client(Socket clientSocket,ServerReceive serverReceive,ClientDisconnect clientDisconnect) throws IOException {
            this.rooms = new ArrayList<>();
            this.clientSocket = clientSocket;
            this.id = General.getRandomString(20);
            outputWriter = new PrintWriter(clientSocket.getOutputStream(), true);

            inputStream = clientSocket.getInputStream();
            this.clientDisconnect = clientDisconnect;
            this.serverReceive = serverReceive;
        }

        @Override
        public void run() {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            try {
                String line;
                while((line=reader.readLine())!=null){
//                    System.out.println(line);
                    if(!line.isEmpty()){
                        serverReceive.receive(id,line);
                    }
                }
            } catch (IOException e) {

            } finally {
                try {
                    inputStream.close();
                    outputWriter.close();
                    clientSocket.close();
                    System.out.println("Client disconnected");
                    clientDisconnect.disconnect(id);
                } catch (IOException e) {

                }
            }
        }
        public void send(String messageJson){
            try{
//                System.out.println("send message: "+messageJson);
                outputWriter.println(messageJson);
            }catch(Exception e){
                System.out.println(e.toString());
            }
        }
        public void joinRoom(String room){
            if(rooms.indexOf(room)==-1){
                rooms.add(room);
            }
        }
        public void outRoom(String room){
            int ind = rooms.indexOf(room);
            if(ind!=-1){
                rooms.remove(ind);
            }
        }
        public boolean hasRoom(String room){
            return rooms.indexOf(room)!=-1;
        }
    }

    public static interface EventCallback{
        public void handle(String data);
    }
    public static class Event{
        public String actorName;
        public String eventName;
        public EventCallback eventCallback;

        public Event(String actorName, String eventName, EventCallback eventCallback) {
            this.actorName = actorName;
            this.eventName = eventName;
            this.eventCallback = eventCallback;
        }
    }
    public static class EventNames{
        public static class Receive{
            public static String JoinPersonalRoom="join-personal-room";
            public static String OutPersonalRoom="out-personal-room";
        }
        public static class Send{
            public static String CoinsPriceNow="coins-price-now";
            public static String AutoCloseTradingCommand="auto-close-trading-command";
        }
    }
    public static class RoomNamesPrefix{
        public static String PersonalRoom="user-";
    }
}
