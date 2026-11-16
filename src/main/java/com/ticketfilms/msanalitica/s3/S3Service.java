package com.ticketfilms.msanalitica.s3;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketfilms.msanalitica.dto.ResumenVentana;
import com.ticketfilms.msanalitica.excepcion.S3SubidaException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
public class S3Service {

    private final String bucket;
    private final boolean habilitado;
    private final String region;
    private final ObjectMapper objectMapper;

    private volatile S3Client s3Client;

    public S3Service(@Value("${s3.bucket-name:}") String bucket,
                     @Value("${s3.habilitado:true}") boolean habilitado,
                     @Value("${aws.region:us-east-1}") String region,
                     ObjectMapper objectMapper) {
        this.bucket = bucket;
        this.habilitado = habilitado;
        this.region = region;
        this.objectMapper = objectMapper;
    }

    public String construirKey(ResumenVentana resumen) {
        ZonedDateTime z = resumen.ventanaInicio().atZone(ZoneOffset.UTC);
        String anio = String.valueOf(z.getYear());
        String mes = String.format("%02d", z.getMonthValue());
        String dia = String.format("%02d", z.getDayOfMonth());
        String hora = String.format("%02d", z.getHour());
        String minuto = String.format("%02d", z.getMinute());
        return "analitica/compras-fallidas/" + anio + "/" + mes + "/" + dia + "/" + hora
                + "/funcion-" + resumen.funcionId() + "_" + anio + mes + dia + "T" + hora + minuto
                + ".jsonl";
    }

    public void guardarResumen(ResumenVentana resumen) {
        String json = serializar(resumen);
        String key = construirKey(resumen);

        if (!habilitado) {
            log.warn("[s3] S3_HABILITADO=false (local): s3://{} / {} <- {}",
                    bucket.isBlank() ? "(sin bucket)" : bucket, key, json);
            return;
        }
        if (bucket == null || bucket.isBlank()) {
            throw new S3SubidaException("Falta la variable S3_BUCKET_NAME");
        }

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType("application/jsonlines")
                    .build();
            cliente().putObject(request, RequestBody.fromString(json));
            log.info("[s3] subido s3://{}/{} ({} bytes)", bucket, key, json.length());
        } catch (SdkException e) {
            throw new S3SubidaException("No se pudo subir s3://" + bucket + "/" + key, e);
        }
    }

    private String serializar(ResumenVentana resumen) {
        try {
            return objectMapper.writeValueAsString(resumen) + "\n";
        } catch (JsonProcessingException e) {
            throw new S3SubidaException("No se pudo serializar el resumen", e);
        }
    }

    private S3Client cliente() {
        if (s3Client == null) {
            synchronized (this) {
                if (s3Client == null) {
                    s3Client = S3Client.builder()
                            .region(Region.of(region))
                            .credentialsProvider(DefaultCredentialsProvider.create())
                            .build();
                }
            }
        }
        return s3Client;
    }
}