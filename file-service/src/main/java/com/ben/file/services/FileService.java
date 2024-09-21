package com.ben.file.services;

import com.ben.file.utils.DiskFileStorage;
import com.ben.grpc.file.*;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.io.IOException;

import static com.ben.file.Constants.FILE_METADATA_CONTEXT;
import static com.ben.file.Constants.MICROSERVICE_NAME;
import static com.ben.file.components.Translator.getLocalizedMessage;
import static com.ben.grpc.utils.WriteStatus.SUCCEEDED;
import static io.grpc.Status.*;

@Slf4j
@GrpcService
public class FileService extends FileServiceGrpc.FileServiceImplBase {

//    @PreAuthorize("authentication.name == 'benlun9999@gmail.com'")
    @Override
    public StreamObserver<FileUploadRequest> uploadFile(StreamObserver<FileUploadResponse> responseObserver) {
        FileMetadata fileMetadata = FILE_METADATA_CONTEXT.get();
        DiskFileStorage diskFileStorage = new DiskFileStorage();

        return new StreamObserver<>() {
            @Override
            public void onNext(FileUploadRequest fileUploadRequest) {
                //log.info(String.format("received %d length of data", fileUploadRequest.getFile().getContent().size()));
                try {
                    fileUploadRequest.getFile().getContent().writeTo(diskFileStorage.getStream());

                } catch (IOException e) {
                    responseObserver.onError(INTERNAL.withDescription(
                            String.format("[%s]: %s", MICROSERVICE_NAME, getLocalizedMessage("can_not_store_file")))
                            .asRuntimeException());
                }
            }

            @Override
            public void onError(Throwable throwable) {
                responseObserver.onError(INTERNAL.withDescription(
                        String.format("[%s]: %s", MICROSERVICE_NAME, getLocalizedMessage("can_not_store_file"))).withCause(throwable)
                        .asRuntimeException());
                try {
                    diskFileStorage.close();

                } catch (IOException e) {
                    log.error("[{}]: cannot close file storage due to : {}", MICROSERVICE_NAME, e.getMessage());
                }
            }

            @Override
            public void onCompleted() {
                try {
                    int totalBytesReceived = diskFileStorage.getStream().size();
                    if (totalBytesReceived == fileMetadata.getContentLength()) {
                        diskFileStorage.write(fileMetadata.getObjectKey());
                        diskFileStorage.close();

                    } else {
                        responseObserver.onError(FAILED_PRECONDITION.withDescription(String.format("[%s]: expected %d but received %d",
                                        MICROSERVICE_NAME, fileMetadata.getContentLength(), totalBytesReceived)).asRuntimeException());
                        return;
                    }

                } catch (IOException e) {
                    responseObserver.onError(INTERNAL.withDescription(String.format("[%s]: %s", MICROSERVICE_NAME,
                            getLocalizedMessage("can_not_store_file"))).asRuntimeException());
                    return;
                }

                responseObserver.onNext(
                        FileUploadResponse
                                .newBuilder()
                                .setFileMetadata(FileMetadata.newBuilder()
                                        .setObjectKey(fileMetadata.getObjectKey())
                                        .setContentType(fileMetadata.getContentType())
                                        .setContentLength(fileMetadata.getContentLength())
                                        .build())
                                .setUploadStatus(SUCCEEDED)
                                .build()
                );
                responseObserver.onCompleted();

            }
        };
    }

}
