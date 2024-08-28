import {UserType} from "../models/user";

let currentCurrentUser: UserType;


const setCurrentUserState = (user: UserType) => {
    currentCurrentUser = user;
}

const gerCurrentUserState = (): UserType => {
    return currentCurrentUser;
}

export {
    setCurrentUserState,
    gerCurrentUserState
}
