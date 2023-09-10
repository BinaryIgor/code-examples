export type ErrorCode = string;
export type OptionalErrorCode = ErrorCode | null;

export const Errors = {
    INVALID_QUOTE_NOTE_CONTENT: "INVALID_QUOTE_NOTE_CONTENT",
    INVALID_QUOTE_NOTE_AUTHOR: "INVALID_QUOTE_NOTE_AUTHOR",
    QUOTE_NOTE_DOES_NOT_EXIST: "QUOTE_NOTE_DOES_NOT_EXIST",
    NOT_USER_QUOTE_NOTE: "NOT_USER_QUOTE_NOTE",
    INVALID_USER_NAME: "INVALID_USER_NAME",
    INVALID_USER_PASSWORD: "INVALID_USER_PASSWORD",
    USER_DOES_NOT_EXIST: "USER_DOES_NOT_EXIST",
    INCORRECT_USER_PASSWORD: "INCORRECT_USER_PASSWORD",
    NOT_AUTHENTICATED: "NOT_AUTHENTICATED",
    INVALID_SESSION: "INVALID_SESSION",
    EXPIRED_SESSION: "EXPIRED_SESSION"
};


export class AppError extends Error {
    constructor(readonly errors: ErrorCode[], 
        readonly message: string = `There were ${errors.length}`) {
        super(message)
    }

    static throwIfThereAreErrors(...errors: (ErrorCode | null)[]) {
        const definedErrors = [...errors].filter(e => e != null).map(e => e as ErrorCode);
        if (definedErrors.length > 0) {
            throw new AppError(definedErrors);
        }
    }

    static ofSingleError(error: ErrorCode): AppError {
        return new AppError([error]);
    }
}   