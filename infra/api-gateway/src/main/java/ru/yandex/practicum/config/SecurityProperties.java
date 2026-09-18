package ru.yandex.practicum.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    private List<UserConfig> users;

    public List<UserConfig> getUsers() {
        return users;
    }

    public void setUsers(List<UserConfig> users){
        this.users = users;
    }

    public static class UserConfig {

        private String username;
        private String password;
        private List<String> roles;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }
    }

}
