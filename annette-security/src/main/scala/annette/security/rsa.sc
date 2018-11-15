import java.security.spec.RSAPublicKeySpec
import java.util.Base64
import java.security.KeyFactory

val n =  "yzJ49k1an6_1AmVL88e1TP8mPJetKIFoZkfMmmXWOoK0E_o_tlQjEJvsWHYTIDweAXTdxv1awaQOnJfnYShK7ZNdM1JXBY4Yc00Mnrqtwb8kZ90-AlOZtsjONs4AIU0NY13fFIVjTLfa6rdWV6KFe_MGAcmiHc4uKS-oGZuzjM8Y6rKb2JG5r4N1mWW09gTlqxJK2rjk1FBEnl9exDm0AmyXij_mDByz-pkOV4QQZtlp-csd55VDsH358-Vu4UK-lfxrC7-7PrQS-w4pxTroTW2wEIIvrdJFjKRsjbs1rTYpxG7WeKqnTAMGbmQzvpYJobeK8gDdW5x-4sUsu79Puw"
val e = "AQAB"

val decoder = Base64.getUrlDecoder()
val encoder = Base64.getEncoder

val modulus = BigInt(1, decoder.decode(n))
val exp = BigInt(1, decoder.decode(e))

val spec = new RSAPublicKeySpec(modulus.bigInteger, exp.bigInteger)

val factory = KeyFactory.getInstance("RSA")
val publicKey = factory.generatePublic(spec)
val pem = new String(encoder.encode(publicKey.getEncoded))

val targetPem = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyzJ49k1an6/1AmVL88e1TP8mPJetKIFoZkfMmmXWOoK0E/o/tlQjEJvsWHYTIDweAXTdxv1awaQOnJfnYShK7ZNdM1JXBY4Yc00Mnrqtwb8kZ90+AlOZtsjONs4AIU0NY13fFIVjTLfa6rdWV6KFe/MGAcmiHc4uKS+oGZuzjM8Y6rKb2JG5r4N1mWW09gTlqxJK2rjk1FBEnl9exDm0AmyXij/mDByz+pkOV4QQZtlp+csd55VDsH358+Vu4UK+lfxrC7+7PrQS+w4pxTroTW2wEIIvrdJFjKRsjbs1rTYpxG7WeKqnTAMGbmQzvpYJobeK8gDdW5x+4sUsu79PuwIDAQAB"

pem == targetPem