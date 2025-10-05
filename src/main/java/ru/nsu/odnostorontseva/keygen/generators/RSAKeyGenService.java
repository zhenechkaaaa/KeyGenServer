package ru.nsu.odnostorontseva.keygen.generators;

import lombok.AllArgsConstructor;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import ru.nsu.odnostorontseva.keygen.entity.KeyAndCrt;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Date;

@AllArgsConstructor
public class RSAKeyGenService implements KeyGenService {

    private final PrivateKey caPrivateKey;
    private final String issuerName;

    @Override
    public KeyAndCrt generate(String clientName) throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair clientPair = keyGen.generateKeyPair();

        X500Name subject = new X500Name("CN=" + clientName);
        X500Name issuer = new X500Name("CN=" + issuerName);

        BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());
        Date notBefore = new Date();
        Date notAfter = new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000);

        JcaX509v3CertificateBuilder certBuilder =
                new JcaX509v3CertificateBuilder(
                        issuer,
                        serial,
                        notBefore,
                        notAfter,
                        subject,
                        clientPair.getPublic()
                );

        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA")
                .build(caPrivateKey);

        X509CertificateHolder holder = certBuilder.build(signer);
        X509Certificate clientCert = new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(holder);

        return new KeyAndCrt(clientPair, clientCert);
    }

    @Override
    public byte[] serialize(KeyAndCrt kp) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(new OutputStreamWriter(out))) {
            pemWriter.writeObject(kp.keyPair.getPrivate());
            pemWriter.writeObject(kp.cert);
        }
        return out.toByteArray();
    }
}
