class Api {

  /**
   * 
   * @param {string} baseUrl 
   * @param {Function} onUnauthenticated 
   */
  constructor(baseUrl, onUnauthenticated) {
    this._baseUrl = baseUrl;
    this._onUnauthenticated = onUnauthenticated;
  }

  /**
   * @param {Object} params
   * @param {string} params.path
   * @param {string} params.method
   * @param {Object | null} params.body
   * @returns {Promise<Response>}
   */
  async exchange({ path, method, body }) {
    try {
      const response = await fetch(this._fullUrl(path), {
        method: method,
        headers: {
          "content-type": "application/json"
        },
        body: body ? JSON.stringify(body) : null,
        credentials: "include"
      });
      if (response.status == 401) {
        this._onUnauthenticated();
      }
      const json = this._hasJsonContent(response.headers) ? await response.json() : null;
      return new Response(response.ok, json);
    } catch (e) {
      return new Response(false, {
        error: "UnknownError",
        message: e instanceof Error ? e.message : e
      });
    }
  }

  _hasJsonContent(headers) {
    const contentType = headers.get('content-type');
    return contentType != null && contentType.includes("application/json");
  }

  _fullUrl(path) {
    return `${this._baseUrl}${path.startsWith("/") ? path : `/${path}`}`;
  }

  post(path, body = null) {
    return this.exchange({ path, method: "POST", body });
  }

  put(path, body = null) {
    return this.exchange({ path, method: "PUT", body });
  }

  get(path) {
    return this.exchange({ path, method: "GET", body: null });
  }

  delete(path) {
    return this.exchange({ path, method: "DELETE", body: null });
  }
}


export class Response {
  /**
   * 
   * @param {boolean} success 
   * @param {Object} data 
   */
  constructor(success, data) {
    this.success = success;
    this.data = data;
  }

  /**
   * 
   * @returns {string | null}
   */
  error() {
    return this.data?.error;
  }
}

export const api = new Api(import.meta.env.VITE_API_BASE_URL,
  () => {
    setTimeout(() => location.href = "/sign-in", 500);
  }
);