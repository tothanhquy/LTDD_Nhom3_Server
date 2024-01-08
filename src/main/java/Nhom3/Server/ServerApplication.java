package Nhom3.Server;

import Nhom3.Server.repository.AccountMoneyHistoryRepository;
import Nhom3.Server.repository.AccountRepository;
import Nhom3.Server.repository.TradingCommandRepository;
import Nhom3.Server.service.LoopService;
import Nhom3.Server.service.SocketService;
import com.mongodb.client.MongoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableMongoRepositories
//@EnableAsync
public class ServerApplication {


//	@Autowired
//	public static SocketService socketService;

	public static void main(String[] args) {
		SpringApplication.run(ServerApplication.class, args);

//		new Thread(loopService).start();
//		loopService.start();
	}
}
