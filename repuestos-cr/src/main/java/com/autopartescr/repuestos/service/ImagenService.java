package com.autopartescr.repuestos.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.StorageClass;
import com.google.firebase.cloud.StorageClient;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

// Sube imagenes de repuestos a Firebase Storage y devuelve la URL
// publica para guardarla en la columna Repuesto.imagenUrl.
//
// Solo el ADMINISTRADOR puede llegar a este service (la ruta
// /repuestos/** ya esta protegida en SecurityConfig), asi que aqui no
// se repite ninguna validacion de rol.
@Service
public class ImagenService {

    private static final Logger log = LoggerFactory.getLogger(ImagenService.class);

    private static final List<String> TIPOS_PERMITIDOS =
            List.of("image/jpeg", "image/png", "image/webp");

    private static final long TAMANO_MAXIMO_BYTES = 5L * 1024 * 1024; // 5 MB

    // Sube la imagen a la carpeta "repuestos/" del bucket y devuelve la
    // URL publica de descarga. Si el archivo esta vacio, devuelve null
    // (el repuesto se guarda sin imagen, sin lanzar error).
    public String subirImagenRepuesto(MultipartFile archivo) throws IOException {
        if (archivo == null || archivo.isEmpty()) {
            return null;
        }

        validarArchivo(archivo);

        String extension = obtenerExtension(archivo.getOriginalFilename());
        String nombreArchivo = "repuestos/" + UUID.randomUUID() + extension;

        Bucket bucket = StorageClient.getInstance().bucket();

        try (InputStream contenido = archivo.getInputStream()) {
            BlobId blobId = BlobId.of(bucket.getName(), nombreArchivo);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(archivo.getContentType())
                    .setStorageClass(StorageClass.STANDARD)
                    .build();

            Blob blob = bucket.getStorage().createFrom(blobInfo, contenido);

            log.info("Imagen subida a Firebase Storage: {}", nombreArchivo);

            // URL publica de descarga (funciona porque en las reglas de
            // Storage configuramos "allow read: if true").
            return String.format(
                    "https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                    bucket.getName(),
                    java.net.URLEncoder.encode(nombreArchivo, java.nio.charset.StandardCharsets.UTF_8)
            );
        }
    }

    private void validarArchivo(MultipartFile archivo) throws IOException {
        if (!TIPOS_PERMITIDOS.contains(archivo.getContentType())) {
            throw new IOException("Formato de imagen no permitido. Use JPG, PNG o WEBP.");
        }
        if (archivo.getSize() > TAMANO_MAXIMO_BYTES) {
            throw new IOException("La imagen no puede superar los 5 MB.");
        }
    }

    private String obtenerExtension(String nombreOriginal) {
        if (nombreOriginal == null || !nombreOriginal.contains(".")) {
            return ".jpg";
        }
        return nombreOriginal.substring(nombreOriginal.lastIndexOf('.'));
    }
}
