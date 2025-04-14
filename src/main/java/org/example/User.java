package org.example;

public class User {
    private String username;
    private String password; // 为简化示例，这里直接存储明文密码，实际项目中建议使用加密

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    // 添加修改密码的方法
    public void setPassword(String password) {
        this.password = password;
    }
}


