package dev.alexnader.framed.client.transform;

import javax.annotation.Nullable;

public class TransformResult {
    public enum Status {
        DID_SOMETHING,
        NOTHING_TO_DO,
        FAILED,
        ;
    }

    public static final TransformResult DID_SOMETHING = new TransformResult(Status.DID_SOMETHING, null);
    public static final TransformResult NOTHING_TO_DO = new TransformResult(Status.NOTHING_TO_DO, null);

    public final Status status;
    public final @Nullable String message;

    private TransformResult(Status status, @Nullable String message) {
        this.status = status;
        this.message = message;
    }

    public static TransformResult failed(String message) {
        return new TransformResult(Status.FAILED, message);
    }
}
