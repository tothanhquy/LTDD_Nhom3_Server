package Nhom3.Server;

import Nhom3.Server.service.LoopService;
import Nhom3.Server.service.SocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
//@EnableAsync
public class ServerApplication {

//	@Autowired
//	public static LoopService loopService;
//	@Autowired
	public static SocketService socketService;

	public static void main(String[] args) {
		SpringApplication.run(ServerApplication.class, args);
		SocketService socketService = new SocketService();
		new Thread(socketService).start();

//		new Thread(loopService).start();
//		loopService.start();
	}
}
