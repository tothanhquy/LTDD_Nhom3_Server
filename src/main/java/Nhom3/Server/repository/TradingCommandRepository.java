package Nhom3.Server.repository;

import Nhom3.Server.model.AccountModel;
import Nhom3.Server.model.TradingCommandModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TradingCommandRepository extends MongoRepository<TradingCommandModel, String> {
    Optional<TradingCommandModel> findById(String id);
}
