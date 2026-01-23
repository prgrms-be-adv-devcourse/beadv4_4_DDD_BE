package com.modeunsa.global.s3;

import com.modeunsa.global.s3.exception.S3FileNotFoundException;
import com.modeunsa.global.s3.exception.S3OperationException;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
public class S3UploadExecutor {

  public <T> T execute(String operation, Supplier<T> action) {
    try {
      return action.get();
    } catch (NoSuchKeyException e) {
      throw new S3FileNotFoundException(operation, e);
    } catch (S3Exception e) {
      throw new S3OperationException(operation, e);
    } catch (Exception e) {
      throw new S3OperationException(operation, e);
    }
  }

  public void executeVoid(String operation, Runnable action) {
    execute(
        operation,
        () -> {
          action.run();
          return null;
        });
  }
}
