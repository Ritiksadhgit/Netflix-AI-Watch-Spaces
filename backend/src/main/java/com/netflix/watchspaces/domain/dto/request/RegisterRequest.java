package com.netflix.watchspaces.domain.dto.request;

import com.netflix.watchspaces.domain.enums.RoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Display name is required")
    @Size(min = 2, max = 50, message = "Display name must be between 2 and 50 characters")
    private String displayName;

    private String avatarUrl;

    private RoleType role;

    public RegisterRequest() {}

    public RegisterRequest(String email, String password, String displayName, String avatarUrl, RoleType role) {
        this.email = email;
        this.password = password;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
        this.role = role;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String email;
        private String password;
        private String displayName;
        private String avatarUrl;
        private RoleType role;

        public Builder email(String email) { this.email = email; return this; }
        public Builder password(String password) { this.password = password; return this; }
        public Builder displayName(String displayName) { this.displayName = displayName; return this; }
        public Builder avatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; return this; }
        public Builder role(RoleType role) { this.role = role; return this; }

        public RegisterRequest build() {
            return new RegisterRequest(email, password, displayName, avatarUrl, role);
        }
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public RoleType getRole() { return role; }
    public void setRole(RoleType role) { this.role = role; }
}
