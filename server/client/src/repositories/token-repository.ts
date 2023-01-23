export class TokenRepository {
    getToken(): string | null {
        return localStorage.getItem("Token")
    }

    setToken(token: string | null) {
        localStorage.setItem("Token", token ?? "")
    }
}