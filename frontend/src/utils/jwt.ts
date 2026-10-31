interface JwtPayload {
  sub?: string;
  userId?: number;
  role?: string;
  exp?: number;
  iat?: number;
}

/**
 * Checks whether a given JWT token is expired on the client side.
 * Note: Client-side decoding only checks timestamp expiration, not cryptographic signature validity.
 */
export const isTokenExpired = (token: string): boolean => {
  if (!token) return true;

  try {
    const parts = token.split('.');
    if (parts.length !== 3) {
      // Mock tokens or non-standard format
      if (token.startsWith('mock-token-')) {
        return false;
      }
      return true;
    }

    const base64Url = parts[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );

    const payload = JSON.parse(jsonPayload) as JwtPayload;
    if (!payload.exp) {
      return false;
    }

    // exp is in seconds, Date.now() is in milliseconds
    const expirationTimeMs = payload.exp * 1000;
    return expirationTimeMs <= Date.now();
  } catch {
    return true;
  }
};
