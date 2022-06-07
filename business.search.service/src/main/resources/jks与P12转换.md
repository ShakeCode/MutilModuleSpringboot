**p12(PKCS12)和jks互相转换**

**p12 -> jks**

keytool -importkeystore -srckeystore keystore.p12 -srcstoretype PKCS12 -deststoretype JKS -destkeystore keystore.jks

**jks -> p12**

keytool -importkeystore -srckeystore keystore.jks -srcstoretype JKS
-deststoretype PKCS12 -destkeystore keystore.p12
 

**从jks里面导出cert**

keytool -export -alias cert0001 -keystore trust.jks -storepass 123456 -file cert0001.cer

**将cert导入jks**

keytool -import -v -alias cert001 -file cert001.cer -keystore trust.jks -storepass 123456 -noprompt 

**去除pem格式的key的密码(输出的密码不输入即可)**

openssl rsa -in cert2.key -out cert22.key

**合并pem格式输出pfx(p12)**

openssl pkcs12 -export -inkey cert22.key -in cert2.crt -out cert2.pfx

**指定intermedian和CA**

openssl pkcs12 -export -out mypkcs12.pfx -inkey my.private.key -in mycert.crt -certfile intermediate.crt -CAfile ca.crt 
 

**pfx转回私钥pem**

openssl pkcs12 -in cert2.pfx -out cert22.pem -nodes

**私钥pem转key**

openssl rsa -in cert22.pem -out cert22.key

**私钥pem转crt**

openssl x509 -in cert22.pem -out cert22.crt

**cert转公钥pem**

openssl x509 -in cert2.cer -out cert2.pem -outform PEM

 **公钥pem转der**

openssl x509 -in cert22.pem -inform PEM -out cert22.der -outform DER
 

**私钥pem转der**

openssl rsa -in api_test01_prikey.pem -out test1.der -outform der
 

**der转私钥pem**

openssl x509 -in cert22.cer -inform DER -out cert22.pem -outform  PEM

 **pkcs8私钥转私钥pem**

openssl rsa -in test_pkcs8.pem -out test_pri.pem
 

**检验私钥和公钥是否为同一套, 可以通过查看 modulus**

openssl rsa -noout -modulus -in test.key
openssl req -noout -modulus -in test.csr
openssl x509 -noout -modulus -in test.cer