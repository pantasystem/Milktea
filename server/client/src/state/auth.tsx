import { atom } from "recoil";


export type AuthState = "Loading" | "Unauthorized" | "Authorized";

export const authAtom = atom<AuthState>({
    key: "authState",
    default: "Loading"
});
export const tokenAtom = atom<string>({
    key: "authToken",
    default: "",
});