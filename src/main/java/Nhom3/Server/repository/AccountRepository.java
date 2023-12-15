package Nhom3.Server.repository;

import Nhom3.Server.model.AccountModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends MongoRepository<AccountModel, String> {
    Optional<AccountModel> findById(String id);
    Optional<AccountModel> findByNumberPhone(String numberPhone);
}
