package com.cainabonfiglioli.helpdesk.api.security.jwt;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;

public class JwtTokenUtil implements Serializable{

	private static final long serialVersionUID = 1L;
	
	static final String CLAIM_KEY_USERNAME = "sub";
	static final String CLAIM_KEY_CREATED = "created";
	static final String CLAIM_KEY_EXPIRED = "exp";
	
	@Value("${jwt.secret}")
	private String secret;
	
	@Value("${jwt.expiration}")
	private Long expiretion;

	public String getUsernameFromToken(String token){
		String username;
		try {
			final Claims claims = getClaimsFromToken(token);
			username = claims.getSubject();
		}catch (Exception e){
			username = null;
		}

		return username;
	}

	public  Date getExpirationDateFromToken(String token){
		Date expiration;
		try {
			final Claims claims = getClaimsFromToken(token);
			expiration = claims.getExpiration();
		}catch (Exception e){
			expiration = null;
		}

		return expiration;
	}

	private Claims getClaimsFromToken(String token){
		Claims claims;
		try {
			claims = Jwts.parser().
					setSigningKey(secret).
					parseClaimsJws(token).
					getBody();
		}catch (Exception e){
			claims = null;
		}

		return claims;
	}

	private Boolean isTokenExpired(String token){
		final Date expiration = getExpirationDateFromToken(token);
		return expiration.before(new Date());
	}

	public String genereteToken(UserDetails userDetails){
		Map<String, Object> claims = new HashMap<>();

		claims.put(CLAIM_KEY_USERNAME, userDetails.getUsername());
		final Date createDate = new Date();
		claims.put(CLAIM_KEY_CREATED, createDate);

		return doGenerateToken(claims);
	}

	private String doGenerateToken(Map<String,Object> claims){
		final Date createDate = (Date) claims.get(CLAIM_KEY_CREATED);
		final Date expirationDate = new Date(createDate.getTime() + expiretion * 1000);
		return Jwts.builder()
				.setClaims(claims)
				.setExpiration(expirationDate)
				.signWith(SignatureAlgorithm.HS512, secret)
				.compact();
	}

	public Boolean canTokenBeRefreshed(String token){
		return (!isTokenExpired(token));
	}

	public String refreshToken(String token){
		String refreshToken;
		try {
			final Claims claims = getClaimsFromToken(token);
			claims.put(CLAIM_KEY_CREATED, new Date());
			refreshToken = doGenerateToken(claims);
		}catch (Exception e){
			refreshToken = null;
		}

		return refreshToken;
	}

	public Boolean validateToken(String token, UserDetails userDetails){
		JwtUser user = (JwtUser) userDetails;
		final String username = getUsernameFromToken(token);
		return  (
				username.equals(user.getUsername())
				&& !isTokenExpired(token));
	}

}
