package ui.javafx.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 백엔드 Account 엔티티와 동일 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    private Long accountId;
    private String username;
    private String password;
    private boolean admin;

    public Account(String username, String password, boolean admin){
        this.username=username;
        this.password=password;
        this.admin=admin;
    }
}
