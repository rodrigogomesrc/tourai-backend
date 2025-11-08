package br.imd.ufrn.tourai.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
public class ArquivoService {

    @Value("${minio.bucket-name}")
    private String bucket;

    @Value("${minio.url}")
    private String minioUrl;

    private final MinioClient minioClient;

    public ArquivoService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public String salvarArquivo(MultipartFile arquivo) {
        try {
            String nomeArquivo = UUID.randomUUID() + "_" + System.currentTimeMillis();
            InputStream inputStream = arquivo.getInputStream();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(nomeArquivo)
                            .stream(inputStream, arquivo.getSize(), -1)
                            .contentType(arquivo.getContentType())
                            .build()
            );

            inputStream.close();
            String cleanMinioUrl = minioUrl.endsWith("/") ? minioUrl.substring(0, minioUrl.length() - 1) : minioUrl;
            return cleanMinioUrl + "/" + bucket + "/" + nomeArquivo;


        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar arquivo no MinIO: " + e.getMessage(), e);
        }
    }

}
