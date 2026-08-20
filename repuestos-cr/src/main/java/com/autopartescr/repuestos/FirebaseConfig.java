package com.autopartescr.repuestos;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

// Inicializa la conexion con Firebase (usada para Firebase Storage,
// almacenamiento de imagenes de repuestos - HU de tecnologia no vista
// en clase).
//
// Las credenciales NUNCA se suben al repositorio. Se leen desde:
//   1. La ruta indicada en la variable de entorno FIREBASE_CREDENTIALS_PATH
//   2. O, si no esta definida, desde application-local.properties
//      (que tampoco se sube a git, ver .gitignore).
//
// Si no se encuentran credenciales, la aplicacion sigue arrancando
// normalmente: solo queda deshabilitada la subida de imagenes, sin
// tumbar el resto del sistema (login, catalogo, pedidos, etc.).
@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.credentials.path:}")
    private String credentialsPath;

    @Value("${firebase.storage.bucket:}")
    private String storageBucket;

    @PostConstruct
    public void inicializar() {
        if (credentialsPath == null || credentialsPath.isBlank()) {
            log.warn("FIREBASE_CREDENTIALS_PATH no configurado. "
                    + "La subida de imagenes a Firebase Storage quedara deshabilitada.");
            return;
        }

        if (!FirebaseApp.getApps().isEmpty()) {
            // Ya inicializado (por ejemplo, en tests que recargan el contexto).
            return;
        }

        try (FileInputStream serviceAccount = new FileInputStream(credentialsPath)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket(storageBucket)
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("Firebase inicializado correctamente. Bucket: {}", storageBucket);

        } catch (IOException e) {
            log.error("No se pudo inicializar Firebase. Verifique la ruta de credenciales "
                    + "en FIREBASE_CREDENTIALS_PATH: {}", credentialsPath, e);
        }
    }
}
