package vn.edu.iuh.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.iuh.exceptions.DataNotFoundException;
import vn.edu.iuh.models.Account;
import vn.edu.iuh.repositories.AccountRepository;
import vn.edu.iuh.services.AccountService;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public Account findByPhone(String phone) {
        return accountRepository.findByPhone(phone).orElseThrow(() -> new DataNotFoundException("Account not found with phone: " + phone));
    }

    @Override
    public boolean existsByPhone(String phone) {
        return accountRepository.existsByPhone(phone);
    }

    @Override
    public Account save(Account account) {
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        return accountRepository.save(account);
    }


}
