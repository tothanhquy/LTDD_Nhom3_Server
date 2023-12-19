package Nhom3.Server.repository;

import Nhom3.Server.model.AccountModel;
import Nhom3.Server.model.AccountMoneyHistoryModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountMoneyHistoryRepository extends MongoRepository<AccountMoneyHistoryModel, String> {
    Optional<AccountMoneyHistoryModel> findById(String id);
}
