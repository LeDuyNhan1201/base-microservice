#________________________________________________________CA-CERTIFICATE_______________________________________________________________
# Generate a CA key and certificate
openssl genrsa -out openssl/CA/ca.key 2048
openssl req -new -x509 -key openssl/CA/ca.key -out openssl/CA/ca.crt -days 3650 -passin pass:mypass -subj "/CN=com.ben/OU=IT/O=SGU/L=HCM/ST=BinhTan/C=VN"


#_______________________________________________________KAFKA-BROKER__________________________________________________________________  
keytool -list -v -keystore docker/kafka/certs/kafka.keystore.jks
# Create a server certificate signed by the CA
openssl genrsa -out docker/kafka/certs/kafka.key 2048
openssl req -new -key docker/kafka/certs/kafka.key -out docker/kafka/certs/kafka.csr -config openssl/kafka.cnf
openssl x509 -req -in docker/kafka/certs/kafka.csr -CA openssl/CA/ca.crt -CAkey openssl/CA/ca.key -CAcreateserial -out docker/kafka/certs/kafka.crt -days 365 -extfile openssl/kafka.cnf -extensions req_ext


# Create the keystore
openssl pkcs12 -export -in docker/kafka/certs/kafka.crt -inkey docker/kafka/certs/kafka.key -out docker/kafka/certs/kafka.p12 -name kafka-cert
keytool -importkeystore -destkeystore docker/kafka/certs/kafka.keystore.jks -srckeystore docker/kafka/certs/kafka.p12 -srcstoretype PKCS12 -alias kafka-cert -deststorepass kafkapass


# Create the truststore and import the CA certificate
keytool -import -file openssl/CA/ca.crt -alias ca-cert -keystore docker/kafka/certs/kafka.truststore.jks -storepass kafkapass

#______________________________________________________SCHEMA-REGISTRY________________________________________________________________
keytool -list -v -keystore docker/schema-registry/certs/schema-registry.keystore.jks
# Create a server certificate signed by the CA
openssl genrsa -out docker/schema-registry/certs/schema-registry.key 2048
openssl req -new -key docker/schema-registry/certs/schema-registry.key -out docker/schema-registry/certs/schema-registry.csr -config openssl/schema-registry.cnf
openssl x509 -req -in docker/schema-registry/certs/schema-registry.csr -CA openssl/CA/ca.crt -CAkey openssl/CA/ca.key -CAcreateserial -out docker/schema-registry/certs/schema-registry.crt -days 365 -extfile openssl/schema-registry.cnf -extensions req_ext


# Create the keystore
openssl pkcs12 -export -in docker/schema-registry/certs/schema-registry.crt -inkey docker/schema-registry/certs/schema-registry.key -out docker/schema-registry/certs/schema-registry.p12 -name schema-registry-cert
keytool -importkeystore -destkeystore docker/schema-registry/certs/schema-registry.keystore.jks -srckeystore docker/schema-registry/certs/schema-registry.p12 -srcstoretype PKCS12 -alias schema-registry-cert -deststorepass schemaregistrypass


# Create the truststore and import the CA certificate
keytool -import -file openssl/CA/ca.crt -alias ca-cert -keystore docker/schema-registry/certs/schema-registry.truststore.jks -storepass schemaregistrypass

keytool -importcert -file docker/kafka/certs/kafka.crt -keystore docker/schema-registry/certs/schema-registry.truststore.jks -alias kafka-cert -storepass schemaregistrypass

#______________________________________________________IDENTITY-SERVICE________________________________________________________________
keytool -list -v -keystore identity-service/certs/identity.keystore.jks
# Create a server certificate signed by the CA
openssl genrsa -out identity-service/certs/identity.key 2048
openssl req -new -key identity-service/certs/identity.key -out identity-service/certs/identity.csr -passin pass:identitypass -subj "/CN=localhost/OU=IT/O=SGU/L=HCM/ST=BinhTan/C=VN"
openssl x509 -req -in identity-service/certs/identity.csr -CA openssl/CA/ca.crt -CAkey openssl/CA/ca.key -CAcreateserial -out identity-service/certs/identity.crt -days 365


# Create the keystore
openssl pkcs12 -export -in identity-service/certs/identity.crt -inkey identity-service/certs/identity.key -out identity-service/certs/identity.p12 -name identity-cert
keytool -importkeystore -destkeystore identity-service/certs/identity.keystore.jks -srckeystore identity-service/certs/identity.p12 -srcstoretype PKCS12 -alias identity-cert -deststorepass identitypass


# Create the truststore and import the CA certificate
keytool -import -file openssl/CA/ca.crt -alias ca-cert -keystore identity-service/certs/identity.truststore.jks -storepass identitypass

keytool -importcert -file docker/kafka/certs/kafka.crt -keystore identity-service/certs/identity.truststore.jks -alias kafka-cert -storepass identitypass

#______________________________________________________PROFILE-SERVICE________________________________________________________________  
keytool -list -v -keystore profile-service/certs/profile.keystore.jks
# Create a server certificate signed by the CA
openssl genrsa -out profile-service/certs/profile.key 2048
openssl req -new -key profile-service/certs/profile.key -out profile-service/certs/profile.csr -passin pass:profilepass -subj "/CN=localhost/OU=IT/O=SGU/L=HCM/ST=BinhTan/C=VN"
openssl x509 -req -in profile-service/certs/profile.csr -CA openssl/CA/ca.crt -CAkey openssl/CA/ca.key -CAcreateserial -out profile-service/certs/profile.crt -days 365


# Create the keystore
openssl pkcs12 -export -in profile-service/certs/profile.crt -inkey profile-service/certs/profile.key -out profile-service/certs/profile.p12 -name profile-cert
keytool -importkeystore -destkeystore profile-service/certs/profile.keystore.jks -srckeystore profile-service/certs/profile.p12 -srcstoretype PKCS12 -alias profile-cert -deststorepass profilepass


# Create the truststore and import the CA certificate
keytool -import -file openssl/CA/ca.crt -alias ca-cert -keystore profile-service/certs/profile.truststore.jks -storepass profilepass

keytool -importcert -file docker/kafka/certs/kafka.crt -keystore profile-service/certs/profile.truststore.jks -alias kafka-cert -storepass profilepass

#______________________________________________________MAIL-SERVICE________________________________________________________________  
keytool -list -v -keystore mail-service/certs/mail.keystore.jks
# Create a server certificate signed by the CA
openssl genrsa -out mail-service/certs/mail.key 2048
openssl req -new -key mail-service/certs/mail.key -out mail-service/certs/mail.csr -passin pass:mailpass -subj "/CN=localhost/OU=IT/O=SGU/L=HCM/ST=BinhTan/C=VN"
openssl x509 -req -in mail-service/certs/mail.csr -CA openssl/CA/ca.crt -CAkey openssl/CA/ca.key -CAcreateserial -out mail-service/certs/mail.crt -days 365


# Create the keystore
openssl pkcs12 -export -in mail-service/certs/mail.crt -inkey mail-service/certs/mail.key -out mail-service/certs/mail.p12 -name mail-cert
keytool -importkeystore -destkeystore mail-service/certs/mail.keystore.jks -srckeystore mail-service/certs/mail.p12 -srcstoretype PKCS12 -alias mail-cert -deststorepass mailpass


# Create the truststore and import the CA certificate
keytool -import -file openssl/CA/ca.crt -alias ca-cert -keystore mail-service/certs/mail.truststore.jks -storepass mailpass

keytool -importcert -file docker/kafka/certs/kafka.crt -keystore mail-service/certs/mail.truststore.jks -alias kafka-cert -storepass mailpass








