import  { NextFunction, Request, Response } from "express";

const HX_TRIGGER_HEADER = "HX-Trigger";
const HTMX_REQUEST_HEADER = "hx-request";
const HTMX_RESTORE_HISTORY_REQUEST = "hx-history-restore-request";

export const asyncHandler = (fn: Function) => (req: Request, res: Response, next: NextFunction) => {
    return Promise.resolve(fn(req, res, next)).catch(next);
}

export function returnCss(res: Response, css: string, cacheControl?: string) {
    res.contentType("text/css");
    if (cacheControl) {
        res.setHeader("cache-control", cacheControl);
    }
    res.send(css);
}

export function returnJs(res: Response, js: string, cacheControl?: string) {
    res.contentType("application/javascript");
    if (cacheControl) {
        res.setHeader("cache-control", cacheControl);
    }
    res.send(js);
}

export function returnHtml(res: Response, html: string, hxTrigger: string | null = null) {
    res.contentType("text/html");
    if (hxTrigger) {
        setTriggerHeader(res, hxTrigger);
    }
    res.send(html);
}

export function returnNotFound(res: Response) {
    res.sendStatus(404);
}

export function setTriggerHeader(res: Response, trigger: string) {
    res.setHeader(HX_TRIGGER_HEADER, trigger);
}

export function cookieValue(req: Request, cookie: string): string | null {
    const cookiesHeader = req.headers.cookie;
    if (!cookiesHeader) {
        return null;
    }
    const cookies = cookiesHeader.split(';');
    for (let c of cookies) {
        const kv = c.split("=", 2);
        if (kv.length != 2) {
            continue;
        }
        const k = kv[0].trim();
        const v = kv[1].trim();
        if (k == cookie) {
            return v;
        }
    }

    return null;
}

export function shouldReturnFullPage(req: Request): boolean {
    return (req.headers[HTMX_REQUEST_HEADER] ? false : true) &&
        (req.headers[HTMX_RESTORE_HISTORY_REQUEST] ? false : true)
}

export function numberPathParam(req: Request, param: string): number {
    return parseInt(req.params[param]);
}

export function fileNameFromPath(req: Request): string {
    return req.url.substring(req.url.lastIndexOf("/"));
}