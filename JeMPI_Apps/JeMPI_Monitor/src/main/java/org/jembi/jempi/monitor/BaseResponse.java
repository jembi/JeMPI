package org.jembi.jempi.monitor;

public class BaseResponse {
    private final Object error;
    private final Object data;
    public BaseResponse(Object data, Boolean isError) {
        if (isError)
        {
            this.error = data;
            this.data = null;
        }
        else{
            this.error = null;
            this.data = data;
        }
    }

    public Object getError() {
        return error;
    }

    public Object getData() {
        return data;
    }
}
