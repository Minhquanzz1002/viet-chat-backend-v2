package vn.edu.iuh.services;

import vn.edu.iuh.models.Account;

public interface AccountService {
    Account findByPhone(String phone);
    boolean existsByPhone(String phone);
    Account save(Account account);
}
