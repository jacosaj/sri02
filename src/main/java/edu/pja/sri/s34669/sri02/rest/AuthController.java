package edu.pja.sri.s34669.sri02.rest;

import edu.pja.sri.s34669.sri02.dto.LoginRequestDto;
import edu.pja.sri.s34669.sri02.dto.LoginResponseDto;
import edu.pja.sri.s34669.sri02.security.JwtIssuer;
import edu.pja.sri.s34669.sri02.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtIssuer jwtIssuer;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto request) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        String token = jwtIssuer.issue(user);
        return ResponseEntity.ok(LoginResponseDto.builder().token(token).build());
    }
}
