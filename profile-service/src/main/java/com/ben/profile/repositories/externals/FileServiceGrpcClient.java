package com.ben.profile.repositories.externals;

import com.ben.grpc.file.File;
import com.ben.grpc.file.FileMetadata;
import com.ben.grpc.file.FileServiceGrpc;
import com.ben.grpc.file.FileUploadRequest;
import com.ben.profile.dtos.responses.FileUploadResponse;
import com.ben.profile.enums.WriteStatus;
import com.ben.profile.exceptions.AppException;
import com.google.protobuf.ByteString;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

import static com.ben.grpc.utils.WriteStatus.FAILED;
import static com.ben.profile.exceptions.AppErrorCode.AVATAR_UPLOAD_FAILED;
import static com.ben.profile.exceptions.AppErrorCode.TOKEN_MISSING;
import static com.ben.profile.utils.Constants.FILE_METADATA_KEY;
import static com.ben.profile.utils.Constants.MICROSERVICE_NAME;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceGrpcClient {

    //@GrpcClient(value = "file-service")
    private final FileServiceGrpc.FileServiceStub fileServiceClient;

    @CircuitBreaker(name = "fileService", fallbackMethod = "fallbackUploadFile")
    public FileUploadResponse uploadFile(String filePath, final MultipartFile multipartFile) {
        FileUploadResponse response = new FileUploadResponse(null, WriteStatus.FAILED);

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

        StreamObserver<FileUploadRequest> fileUploadRequestStreamObserver = fileServiceClient
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
                .uploadFile(
                        new StreamObserver<>() {
                            @Override
                            public void onNext(com.ben.grpc.file.FileUploadResponse fileUploadResponse) {
                                FileMetadata fileMetadata = fileUploadResponse.getFileMetadata();
                                response.setFileMetadata(com.ben.profile.dtos.others.FileMetadata.builder()
                                        .objectKey(fileMetadata.getObjectKey())
                                        .contentType(fileMetadata.getContentType())
                                        .contentLength(fileMetadata.getContentLength())
                                        .build());
                                response.setUploadStatus(WriteStatus.valueOf(fileUploadResponse.getUploadStatus().name()));
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                if (throwable.getMessage().contains(Status.UNAUTHENTICATED.getCode().name())) {
                                    countDownLatch.countDown();
                                    throw new AppException(TOKEN_MISSING, Status.UNAUTHENTICATED, "Token missing");
                                }

                                try {
                                    inputStream.close();
                                } catch (IOException e) {
                                    countDownLatch.countDown();
                                    throw new AppException(AVATAR_UPLOAD_FAILED, Status.INVALID_ARGUMENT, "Avatar upload failed");
                                }

                                response.setUploadStatus(WriteStatus.valueOf(FAILED.name()));
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
                log.info("[{}]: Sending {} %", MICROSERVICE_NAME, currentPercentage);
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

    public FileUploadResponse fallbackUploadFile(Throwable throwable) {
        log.error("[{}]: Fallback method: {} | Caused by: {}",
                MICROSERVICE_NAME, "fallbackUploadFile", throwable.getMessage());
        return FileUploadResponse.builder()
                .fileMetadata(null)
                .uploadStatus(WriteStatus.FAILED)
                .build();
    }

}
