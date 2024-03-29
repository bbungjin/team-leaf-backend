package com.team.leaf.user.account.entity;

import com.team.leaf.user.account.dto.common.LoginType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.team.leaf.user.account.entity.AccountPrivacy.createAccountPrivacy;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long userId;

    private String email;

    private String password;

    private String name;

    @Column(unique = true)
    private String nickname;

    private String birthday;

    private String birthyear;

    private String gender;

    private String universityName;

    private String major;

    private String shippingAddress;

    private String schoolAddress;

    private String workAddress;

    private LocalDate joinDate;

    private String phone;

    private LocalDate lastAccess;

    private int loginFailCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoginType loginType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountRole role;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true, fetch = FetchType.LAZY)
    private AccountPrivacy userDetail;

    @OneToMany(mappedBy = "accountDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccountInterest> accountInterests = new ArrayList<>();

    public static AccountDetail joinAccount(String email, String encodedPassword, String phone, String nickname) {
        AccountDetail accountDetail = new AccountDetail();
        accountDetail.email = email;
        accountDetail.password = encodedPassword;
        accountDetail.phone = phone;
        accountDetail.nickname = nickname;
        accountDetail.role = AccountRole.USER;
        accountDetail.loginType = LoginType.JWT;
        accountDetail.joinDate = LocalDate.now();
        accountDetail.userDetail = createAccountPrivacy();
        //accountDetail.lastAccess = LocalDate.now();

        return accountDetail;
    }

    public void updateAdditionalInfo(String name, String birthday, String gender, String universityName) {
        this.name = name;
        this.birthday = birthday;
        this.gender = gender;
        this.universityName = universityName;
    }

}
