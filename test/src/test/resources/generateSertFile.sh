keytool -genkey -alias server-alias -keyalg RSA -keypass keypassword -storepass storePassword -keystore keystore.jks
keytool -export -alias server-alias -storepass storePassword -file server.cer -keystore keystore.jks
keytool -import -v -trustcacerts -alias server-alias -file server.cer -keystore clientkeystore.jks -keypass keypassword -storepass storePassword



keytool -genkey -alias server-alias -keyalg RSA -keypass keypassword -storepass storePassword -keystore dummykeystore.jks
keytool -export -alias server-alias -storepass storePassword -file dummyserver.cer -keystore dummykeystore.jks
keytool -import -v -trustcacerts -alias server-alias -file dummyserver.cer -keystore dummyclientkeystore.jks -keypass keypassword -storepass storePassword

// for server 1

keytool -genkey -alias server-alias -keyalg RSA -keypass keypassword -storepass storePassword -keystore keystore1.jks
keytool -export -alias server-alias -storepass storePassword -file server1.cer -keystore keystore1.jks
keytool -import -v -trustcacerts -alias server-alias -file server1.cer -keystore clientkeystore1.jks -keypass keypassword -storepass storePassword
