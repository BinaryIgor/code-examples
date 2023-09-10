import fs from "fs";

export function textFileContent(path: string): Promise<string> {
    return fs.promises.readFile(path, 'utf-8');
}

export function writeTextFileContent(path: string, content: string): Promise<void> {
    return fs.promises.writeFile(path, content);
}

export function fileExists(path: string): Promise<boolean> {
    return fs.promises.stat(path).catch(e => false).then(e => true);
}

export function deleteFile(path: string): Promise<void> {
    return fs.promises.unlink(path);
}