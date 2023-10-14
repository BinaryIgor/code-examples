import path from "path";

function envVariableOrDefault(key: string, defaultValue: any) {
    return process.env[key] ?? defaultValue;
}

function envVariableAsNumberOrDefault(key: string, defaultValue: any) {
    try {
        return parseInt(envVariableOrDefault(key, defaultValue));
    } catch (e) {
        throw new Error(`Can't parse ${key} to number`);
    }
}

//TODO: download htmx to assets and serve it directly from there
export const getAssetsSrc = () => ({
    htmx: envVariableOrDefault("ASSETS_HTMX_SRC", "https://unpkg.com/htmx.org@1.9.3"),
    styles: envVariableOrDefault("ASSETS_STYLES_SRC", "/style.css"),
    indexJs: envVariableOrDefault("ASSETS_INDEX_JS_SRC", "/index.js")
});

export const getAppConfig = () => {
    const assetsPath = envVariableOrDefault("ASSETS_PATH", path.join(__dirname, "..", "assets"));
    return {
        profile: envVariableOrDefault("PROFILE", "default"),
        server: {
            port: envVariableOrDefault("SERVER_PORT", 8080)
        },
        session: {
            //5 hours
            duration: envVariableAsNumberOrDefault("SESSION_DURATION", 5 * 60 * 60 * 1000),
            dir: envVariableOrDefault("SESSION_DIR", path.join("/tmp", "session")),
            refreshInterval: envVariableOrDefault("SESSION_REFRESH_INTERVAL", 60 * 1000),
        },
        db: {
            path: envVariableOrDefault("DB_PATH", path.join(__dirname, "..", "assets", "db")),
            e2eTestsPath: envVariableOrDefault("E2E_TESTS_DB_PATH", path.join(__dirname, "..", "assets", "e2e-tests-db"))
        },
        assets: {
            path: assetsPath,
            stylesPath: envVariableOrDefault("ASSETS_STYLES_PATH", assetsPath),
            cacheControl: envVariableOrDefault("ASSETS_CACHE_CONTROL", "")
        }
    }
};