import { AuthUser } from "../auth/auth";
import { AppError, Errors, OptionalErrorCode } from "../shared/errors";
import * as Validator from "../shared/validator";

const MIN_USER_NAME_LENGTH = 3;
const MAX_USER_NAME_LENGTH = 30;
const MIN_USER_PASSWORD_LENGTH = 8;
const MAX_USER_PASSWORD_LENGTH = 50;

export class UserService {

    constructor(private readonly userRepository: UserRepository,
        private readonly passwordHasher: PasswordHasher) {}

    signIn(name: string, password: string): AuthUser {
        const nameError = this.validateUserName(name);
        const passwordError = this.validateUserPassword(password);

        AppError.throwIfThereAreErrors(nameError, passwordError);

        const user = this.userRepository.ofName(name);
        if (!user) {
            throw AppError.ofSingleError(Errors.USER_DOES_NOT_EXIST);
        }
        
        if (!this.passwordHasher.verify(password, user.password)) {
            throw AppError.ofSingleError(Errors.INCORRECT_USER_PASSWORD);
        }

        return new AuthUser(user.id, user.name);
    }

    validateUserName(name: string): OptionalErrorCode {
        return Validator.hasAnyContent(name) 
            && Validator.hasLength(name, MIN_USER_NAME_LENGTH, MAX_USER_NAME_LENGTH) ?
            null : Errors.INVALID_USER_NAME;
    }

    validateUserPassword(password: string): OptionalErrorCode {
        return Validator.hasAnyContent(password) 
            && Validator.hasLength(password, MIN_USER_PASSWORD_LENGTH, MAX_USER_PASSWORD_LENGTH) ?
            null : Errors.INVALID_USER_PASSWORD;
    }

    usersOfIds(ids: number[]): Map<number, User> {
        return this.userRepository.ofIds(ids);
    }
}


export class User {
    constructor(readonly id: number,
        readonly name: string,
        readonly password: string) { }
}

export interface UserRepository {
    
    ofId(id: number): User | null;

    ofIds(ids: number[]): Map<number, User>

    ofName(name: string): User | null;
    
    create(user: User): void;
}

export interface PasswordHasher {
    verify(rawPassword: string, hashedPassword: string): boolean
}