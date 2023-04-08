package onlinetutoring.com.teamelevenbackend.controller.models.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginRequest extends AbstractAuthModel {}
