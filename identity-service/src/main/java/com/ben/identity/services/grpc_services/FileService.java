package com.ben.identity.services.grpc_services;

import com.ben.grpc.file.*;
import com.ben.identity.exceptions.AppException;
import com.google.protobuf.ByteString;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

import static com.ben.grpc.utils.WriteStatus.FAILED;
import static com.ben.identity.Constants.FILE_METADATA_KEY;
import static com.ben.identity.exceptions.AppErrorCode.TOKEN_MISSING;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Slf4j
@Service
public class FileService {

    @GrpcClient(value = "file-service")
    private FileServiceGrpc.FileServiceStub client;

    public com.ben.identity.dtos.responses.FileUploadResponse uploadFile(String filePath, final MultipartFile multipartFile) {
        com.ben.identity.dtos.responses.FileUploadResponse response = new com.ben.identity.dtos.responses
                .FileUploadResponse(null, com.ben.identity.enums.WriteStatus.FAILED);
        int fileSize;
        InputStream inputStream;

        try {
            fileSize = multipartFile.getBytes().length;
            inputStream = multipartFile.getInputStream();

        } catch (IOException e) {
            return response;
        }

        CountDownLatch countDownLatch = new CountDownLatch(1);

        Metadata metadata = new Metadata();
        metadata.put(FILE_METADATA_KEY,
                FileMetadata.newBuilder()
                        .setObjectKey(filePath)
                        .setContentType(multipartFile.getContentType())
                        .setContentLength(fileSize)
                        .build()
                        .toByteArray());

        StreamObserver<FileUploadRequest> fileUploadRequestStreamObserver = this.client
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .uploadFile(
                        new StreamObserver<>() {
                            @Override
                            public void onNext(FileUploadResponse fileUploadResponse) {
                                FileMetadata fileMetadata = fileUploadResponse.getFileMetadata();
                                response.setFileMetadata(com.ben.identity.dtos.others.FileMetadata.builder()
                                        .objectKey(fileMetadata.getObjectKey())
                                        .contentType(fileMetadata.getContentType())
                                        .contentLength(fileMetadata.getContentLength())
                                        .build());
                                response.setUploadStatus(com.ben.identity.enums
                                        .WriteStatus.valueOf(fileUploadResponse.getUploadStatus().name()));
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                if (throwable.getMessage().contains(Status.UNAUTHENTICATED.getCode().name())) {
                                    countDownLatch.countDown();
                                    throw new AppException(TOKEN_MISSING, UNAUTHORIZED, "Token missing");
                                }

                                try {
                                    inputStream.close();
                                } catch (IOException e) {
                                    countDownLatch.countDown();
                                    throw new RuntimeException(e);
                                }

                                response.setUploadStatus(com.ben.identity.enums.WriteStatus.valueOf(FAILED.name()));
                                countDownLatch.countDown();
                            }

                            @Override
                            public void onCompleted() {
                                countDownLatch.countDown();
                            }
                        });

        byte[] fiveKB = new byte[5120];

        int length;
        long totalBytesRead = 0;
        int currentPercentage = 0;
        try {
            while ((length = inputStream.read(fiveKB)) > 0) {
                totalBytesRead += length;

                FileUploadRequest request = FileUploadRequest.newBuilder()
                        .setFile(File.newBuilder().setContent(ByteString.copyFrom(fiveKB, 0, length)))
                        .build();

                // Calculate the current upload percentage
                currentPercentage = (int) (((double) totalBytesRead / fileSize) * 100);
                log.info(String.format("Sending %d%%", currentPercentage));
                fileUploadRequestStreamObserver.onNext(request);
            }
            inputStream.close();
            fileUploadRequestStreamObserver.onCompleted();
            countDownLatch.await();

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

}
