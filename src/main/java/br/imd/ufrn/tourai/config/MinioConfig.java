package br.imd.ufrn.tourai.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    private static final Logger logger = LoggerFactory.getLogger(MinioConfig.class);

    @Value("${minio.url}")
    private String minioUrl;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Bean
    public MinioClient minioClient() {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(accessKey, secretKey)
                .build();
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                logger.info("Bucket '{}' criado com sucesso.", bucketName);
                tornarBucketPublico(minioClient, bucketName);
            } else {
                logger.info("Bucket '{}' já existe.", bucketName);
            }
        } catch (Exception e) {
            logger.error("Erro ao verificar ou criar bucket: {}", e.getMessage(), e);
            throw new RuntimeException("Não foi possível inicializar o MinIO", e);
        }

        return minioClient;
    }

    private void tornarBucketPublico(MinioClient minioClient, String bucketName) throws Exception {
        String policyJson = "{"
                + "\"Version\": \"2012-10-17\","
                + "\"Statement\": ["
                + "    {"
                + "        \"Effect\": \"Allow\","
                + "        \"Principal\": {\"AWS\": [\"*\"]},"
                + "        \"Action\": [\"s3:GetObject\"],"
                + "        \"Resource\": [\"arn:aws:s3:::" + bucketName + "/*\"]"
                + "    }"
                + "]"
                + "}";

        minioClient.setBucketPolicy(
                SetBucketPolicyArgs.builder()
                        .bucket(bucketName)
                        .config(policyJson)
                        .build()
        );

        logger.info("Política de acesso público definida para o bucket '{}'.", bucketName);
    }
}