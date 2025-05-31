package org.example.booknuri.global.security.provider;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.booknuri.global.security.entity.CustomUser;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.example.booknuri.global.security.constrants.SecurityConstants;
import org.example.booknuri.global.security.props.JwtProps;
import org.example.booknuri.domain.user.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;


@RequiredArgsConstructor
@Slf4j
@Component
public class JwtProvider {

    private final JwtProps jwtProps;


    @Lazy // 여기서 @Lazy를 붙여주면 의존성 주입이 지연됨(순환참조방지)
    private UserService usersService;



    //실제 사용할 수 있는 시크릿키를 반환하는 메서드
    public SecretKey getShaKey() {
        String secretKey = jwtProps.getSecretKey();



        // 바이트 배열로 변환하여 HMAC-SHA 알고리즘에서 사용할 수 있는 SecretKey 객체를 생성한다.
        byte[] signingKey = secretKey.getBytes();
        return Keys.hmacShaKeyFor(signingKey); // SecretKey 객체 반환
    }



   //엑세스 토큰 300분으로 연장(개발용)
    public String createAccessToken(UserEntity user) {

        String username=user.getUsername();
        String role=user.getRole();
        String nickname=user.getNickname();

        SecretKey shaKey = getShaKey();

        long exp = 1000 * 60 * 300;  // 300분

        // JWT 토큰을 생성한다.
        String accessjwt = Jwts.builder()
                // 서명 생성: HMAC-SHA512 알고리즘을 사용하여 서명을 생성
                .signWith(shaKey, Jwts.SIG.HS512)
                // JWT 헤더에 "typ" 값 설정, "jwt"는 토큰의 유형을 나타냄
                .header().add("typ", SecurityConstants.TOKEN_TYPE)
                .and()
                // 토큰 만료 시간 설정
                .expiration(new Date(System.currentTimeMillis() + exp))
                // 페이로드에 username, role을 포함시켜서 토큰에 사용자 정보를 담음

                .claim("username", username)
                .claim("role", role)
                // 모든 설정이 끝나면 최종적으로 JWT 토큰을 생성하고 반환
                .compact();


        log.info("✅  accesstoken생성:" + accessjwt);
        return accessjwt;
    }



     //리프레시 토큰 (30일) 생성
    public String createRefreshToken(String username) {
        SecretKey shaKey = getShaKey();

        long exp = 1000L * 60 * 60 * 24 * 30;

        // JWT 토큰을 생성한다.
        String refreshjwt = Jwts.builder()
                .signWith(shaKey, Jwts.SIG.HS512)
                .header().add("typ", SecurityConstants.TOKEN_TYPE)
                .and()
                .expiration(new Date(System.currentTimeMillis() + exp))
                .claim("username", username)
                .compact();

        log.info("✅  리프레시 토큰 생성 완료! 만료 시간: " + new Date(System.currentTimeMillis() + exp));
        return refreshjwt;
    }


    //엑세스토큰 재발급 메서드
    public String refreshAccessToken(String refreshToken) {

        try {
            //  리프레시 토큰 검증
            Jws<Claims> claims = Jwts.parser()
                    .setSigningKey(getShaKey()) // ✅ 서명 검증
                    .build()
                    .parseClaimsJws(refreshToken);

            //  리프레시 토큰에서 유저 정보 가져오기
            String username = claims.getBody().get("username").toString(); // 유저 ID 가져오기
            UserEntity user = usersService.getUserByUsername(username);

            // 🔹 새로운 엑세스 토큰 발급
            return createAccessToken(user);


        } catch (ExpiredJwtException e) {
            log.error("리프레시 토큰 만료됨!");
        } catch (JwtException e) {
            log.error("리프레시 토큰이 유효하지 않음!");
        }

        return null; //  리프레시 토큰이 유효하지 않다면 null 반환
    }






    // UsernamePasswordAuthenticationToken: Spring Security에서 인증된 사용자 정보를 담는 객체
    //jwt 추출후 CustomUser 만들어서 UsernamePasswordAuthenticationToken에 넣음
    //Spring Security의 인증 객체로 만들어주는 것!
    public UsernamePasswordAuthenticationToken getAuthenticationToken(String authorization) {

        // Authorization 헤더가 null 이거나 빈 값일 경우, 인증을 진행할 수 없으므로 null 반환
        if (authorization == null || authorization.length() == 0)
            return null;

        try {
            // JWT 토큰 추출: Authorization 헤더에서 "Bearer " 부분을 제거하고, 실제 JWT 토큰만 추출
            String jwt = authorization.replace("Bearer ", "");

            // JWT 파싱(해석) (서명 검증 및 페이로드 추출)
            Jws<Claims> parsedToken = Jwts.parser()
                    .setSigningKey(getShaKey()) // 시크릿키를 사용해 서명 검증
                    .build()
                    .parseClaimsJws(jwt);





            // 사용자 아이디(유저네임)
            String username = parsedToken.getBody().get("username").toString();

            // 회원 권한
            String role = parsedToken.getBody().get("role").toString();


            // 해당 유저의 정보 담기 위해 Users 객체 생성
            UserEntity user = new UserEntity();
            user.setUsername(username);
            user.setRole(role);



            // CustomUser 객체는 UserDetails 인터페이스를 구현하여 Spring Security에서 사용할 수 있게 함
            UserDetails userDetails = new CustomUser(user);

            // UsernamePasswordAuthenticationToken을 생성하여 인증 정보를 반환
            //첫 번째 매개변수 (userDetails): 인증된 사용자의 상세 정보.(이거 구현체가 customuser임)
            //두 번째 매개변수 (null): 사용자의 비밀번호인데, 이미 인증이 끝난 후라 비밀번호는 필요하지 않아서 null
            //세 번째 매개변수 (userDetails.getAuthorities()): 사용자의 권한 목록.
            // getAuthorities()는 UserDetails 객체에(여기선 customuser)서 사용자 권한들을 반환하는 메서드
            return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        } catch (ExpiredJwtException exception) {
            log.warn("만료된 JWT 토큰을 파싱하려는 시도: {}", exception.getMessage());
        } catch (UnsupportedJwtException exception) {
            log.warn("지원되지 않는 JWT 토큰을 파싱하려는 시도: {}", exception.getMessage());
        } catch (MalformedJwtException exception) {
            log.warn("잘못된 형식의 JWT 토큰을 파싱하려는 시도: {}", exception.getMessage());
        } catch (IllegalArgumentException exception) {
            log.warn("빈 JWT 토큰을 파싱하려는 시도: {}", exception.getMessage());
        }

        // 예외가 발생하면 null을 반환하여 인증을 실패시킨다.
        return null;
    }

    //토큰 검증 메서드
     public boolean validateToken(String jwt) {

        try{
            Jws<Claims> claims= Jwts.parser().verifyWith(getShaKey()).build().parseSignedClaims(jwt);
            Date expiration = claims.getBody().getExpiration();
            //만료날짜인 expiration과 현재오늘 날짜 비교하기
            //날짜a.after(날짜b): 날짜a가 날짜b보다 더 뒤에 있으면 true
            boolean result=expiration.after(new Date()); //만료안됐으면 true임
            return result;

        } catch(ExpiredJwtException exception){
            log.error("토큰 만료");
        }

        catch (JwtException e) {
            log.error("토큰 손상");

        }catch (NullPointerException e) {
            log.error("토큰 없음");


        }catch( Exception e) {
        }
        return false;


    }

    // 리프레시 토큰 암호화
    public String encrypt(String plainToken) {
        return Base64.getEncoder().encodeToString(plainToken.getBytes(StandardCharsets.UTF_8));
    }

    // 복호화
    public String decrypt(String encryptedToken) {
        return new String(Base64.getDecoder().decode(encryptedToken), StandardCharsets.UTF_8);
    }

    public String extractUsername(String token) {
        try {
            // JWT 파싱해서 username 클레임 추출
            return Jwts.parser()
                    .setSigningKey(getShaKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("username", String.class);
        } catch (Exception e) {
            log.error("username 추출 실패: {}", e.getMessage());
            return null;
        }
    }






}
