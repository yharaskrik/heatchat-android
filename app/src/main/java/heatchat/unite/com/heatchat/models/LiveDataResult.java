package heatchat.unite.com.heatchat.models;

import javax.annotation.Nullable;

/**
 * A simple wrapper class for passing an error from a LiveData object.
 * Created by Andrew on 12/10/2017.
 */

public class LiveDataResult<T> {
    private T result;
    private Exception exception;

    public LiveDataResult(T result) {
        this.result = result;
    }

    public LiveDataResult(Exception exception) {
        this.exception = exception;
    }

    @Nullable
    public T getResult() {
        return result;
    }

    @Nullable
    public Exception getException() {
        return exception;
    }

    public boolean isSuccess() {
        return exception == null;
    }
}
