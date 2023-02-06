package com.cgi.fic.tests.security.jwt;

import com.cgi.fic.tests.config.JHipsterProperties;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class TokenProvider {
    private final Logger log = LoggerFactory.getLogger(TokenProvider.class);

    private static final String AUTHORITIES_KEY = "auth"; // Claim JHiptser front-end uses
    public static final String GROUPS_KEY = "groups"; // Default claim for MP-JWT

    private final Key key;

    private final String issuer;

    private final long tokenValidityInMilliseconds;

    private final long tokenValidityInMillisecondsForRememberMe;

    @Inject
    public TokenProvider(JHipsterProperties jHipsterProperties)
        throws Exception {
        this.key = readPrivateKey(jHipsterProperties.security.authentication.jwt.privateKey.location);
        this.issuer = jHipsterProperties.security.authentication.jwt.issuer;
        this.tokenValidityInMilliseconds = jHipsterProperties.security.authentication.jwt.tokenValidityInSeconds * 1000;
        this.tokenValidityInMillisecondsForRememberMe =
            jHipsterProperties.security.authentication.jwt.tokenValidityInSecondsForRememberMe * 1000;
    }

    @PostConstruct
    void init() throws Exception {}

    public String createToken(QuarkusSecurityIdentity identity, boolean rememberMe) {
        String authorities = String.join(", ", identity.getRoles());
        long now = (new Date()).getTime();
        Date validity;
        if (rememberMe) {
            validity = new Date(now + tokenValidityInMillisecondsForRememberMe);
        } else {
            validity = new Date(now + tokenValidityInMilliseconds);
        }

        JwtClaims claims = new JwtClaims();
        claims.setSubject(identity.getPrincipal().getName());
        claims.setClaim(AUTHORITIES_KEY, authorities);
        claims.setClaim(GROUPS_KEY, identity.getRoles());
        claims.setIssuedAt(NumericDate.fromMilliseconds(now));
        claims.setIssuer(this.issuer);
        claims.setExpirationTime(NumericDate.fromMilliseconds(validity.getTime()));

        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setKey(key);
        jws.setKeyIdHeaderValue(UUID.randomUUID().toString());
        jws.setHeader("typ", "JWT");
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
        try {
            return jws.getCompactSerialization();
        } catch (JoseException e) {
            throw new RuntimeException(e);
        }
    }

    public static PrivateKey readPrivateKey(final String pemResName) throws Exception {
        InputStream contentIS = TokenProvider.class.getResourceAsStream(pemResName);
        byte[] tmp = new byte[4096];
        int length = contentIS.read(tmp);
        return decodePrivateKey(new String(tmp, 0, length, "UTF-8"));
    }

    public static PrivateKey decodePrivateKey(final String pemEncoded) throws Exception {
        byte[] encodedBytes = toEncodedBytes(pemEncoded);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encodedBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);
    }

    private static byte[] toEncodedBytes(final String pemEncoded) {
        final String normalizedPem = removeBeginEnd(pemEncoded);
        return Base64.getDecoder().decode(normalizedPem);
    }

    private static String removeBeginEnd(String pem) {
        pem = pem.replaceAll("-----BEGIN (.*)-----", "");
        pem = pem.replaceAll("-----END (.*)----", "");
        pem = pem.replaceAll("\r\n", "");
        pem = pem.replaceAll("\n", "");
        return pem.trim();
    }
}
