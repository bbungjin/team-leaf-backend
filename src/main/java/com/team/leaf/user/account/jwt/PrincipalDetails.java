package com.team.leaf.user.account.jwt;

import com.team.leaf.user.account.entity.AccountDetail;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

public class PrincipalDetails implements UserDetails {

    private AccountDetail accountDetail;

    public void setAccountDetail(AccountDetail accountDetail) {
        this.accountDetail = accountDetail;
    }

    public AccountDetail getAccountDetail() {
        return this.accountDetail;
    }



    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collections = new ArrayList<>();
        collections.add(() -> {
            return accountDetail.getRole().name();
        });


        return collections;
    }

    @Override
    public String getPassword() {
        return accountDetail.getPassword();
    }

    @Override
    public String getUsername() {
        return accountDetail.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
