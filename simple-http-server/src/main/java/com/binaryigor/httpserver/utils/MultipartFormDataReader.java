package com.binaryigor.httpserver.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class MultipartFormDataReader {

    private static final int TWO_DASHES_BYTES_SIZE = toAsciiBytes("--").length;
    private static final String HEADERS_SEPARATOR = "\r\n";
    private static final String HEAD_BODY_SEPARATOR = HEADERS_SEPARATOR + HEADERS_SEPARATOR;
    private static final int PART_BYTES_PACKET_SIZE = 100_000;
    private static final int MAX_HEADERS_SIZE = 5_000;

    public static Form read(Supplier<InputStream> source, Params params) {
        var dashedBoundary = "--" + params.boundary;
        var dashedBoundaryBytesSize = toAsciiBytes(dashedBoundary).length;
        var firstBoundaryBytesSize = dashedBoundaryBytesSize + TWO_DASHES_BYTES_SIZE;
        var betweenOrLastBoundaryPrefixBytes = toAsciiBytes("\r\n" + dashedBoundary);
        var betweenBoundaryBytesSize = TWO_DASHES_BYTES_SIZE + dashedBoundaryBytesSize + TWO_DASHES_BYTES_SIZE;
        var lastBoundaryBytesSize = betweenBoundaryBytesSize + TWO_DASHES_BYTES_SIZE;
        var betweenBoundaryPostfixBytes = toAsciiBytes("\r\n");
        var lastBoundaryPostfixBytes = toAsciiBytes("--\r\n");

        var multipartFilePrefix = "multipart-" + UUID.randomUUID() + "-";

        var rawParts = new ArrayList<RawFormPart>();

        // assumption, we don't check it
        long read = firstBoundaryBytesSize;
        int partIdx = 0;

        // TODO

        return new Form(toFormParts(rawParts, params.defaultContentType));
    }

    private static byte[] toAsciiBytes(String string) {
        return string.getBytes(StandardCharsets.US_ASCII);
    }

    private static RawFormPart readUntil(InputStream source,
                                         int maxInMemorySize,
                                         File tmpFile,
                                         byte[] boundaryPrefix,
                                         byte[] boundaryBetweenPartsPostfix,
                                         byte[] boundaryLastPartPostfix) throws Exception {
        var buffer = new ByteArrayOutputStream();
        var fileOutputStream = new AtomicReference<FileOutputStream>();

        var packet = new byte[PART_BYTES_PACKET_SIZE];
        var read = source.read(packet);

        // TODO

        return rawFormPartFromMemoryOrFile(fileOutputStream.get(), buffer, tmpFile, false);
    }

    private static boolean isPatternIndex(byte[] bytes, int length, int idx, byte[] pattern) {
        int matchingBytes = 0;
        while (matchingBytes < pattern.length && (idx + matchingBytes) < length) {
            if (pattern[matchingBytes] != bytes[idx + matchingBytes]) {
                return false;
            }
            matchingBytes++;
        }
        return matchingBytes == pattern.length;
    }

    private static RawFormPart rawFormPartFromMemoryOrFile(FileOutputStream fileOutputStream,
                                                           ByteArrayOutputStream buffer,
                                                           File tmpFile,
                                                           boolean last) {
        if (fileOutputStream == null) {
            return RawFormPart.ofBytes(buffer.toByteArray(), last);
        }
        try (var os = fileOutputStream) {
            os.write(buffer.toByteArray());
            return RawFormPart.ofFile(tmpFile, last);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static List<FormPart> toFormParts(List<RawFormPart> rawFormParts,
                                              String defaultContentType) {
        return rawFormParts.stream()
                .map(rp -> toFormPart(rp, defaultContentType))
                .toList();
    }

    private static FormPart toFormPart(RawFormPart formPart,
                                       String defaultContentType) {
        int bodyIdx = 0;
        String name = null;
        String filename = null;
        String contentType = null;

        // TODO

        if (formPart.ofBytes()) {
            var body = formPartBytesContent(formPart, bodyIdx);
            return FormPart.ofContent(name, filename, contentType, body);
        }

        var body = formPartFileContent(formPart, bodyIdx);
        return FormPart.ofFile(name, filename, contentType, body);
    }

    private static HeadersWithBodyIndex headersWithBodyIndex(InputStream source) throws Exception {
        int bodyIdx = 0;
        var headBytes = source.readNBytes(MAX_HEADERS_SIZE);
        var headersString = new String(headBytes, StandardCharsets.US_ASCII);

        // TODO

        var headers = new HashMap<String, String>();

        // TODO

        return new HeadersWithBodyIndex(headers, bodyIdx);
    }

    private static ContentDispositionData contentDispositionData(Map<String, String> headers) {
        var contentDispositionLine = headers.get("content-disposition");
        if (contentDispositionLine == null) {
            throw new RuntimeException("Content disposition is required but cannot be found");
        }

        String name = null;
        String filename = null;

        // TODO

        return new ContentDispositionData(name, filename);
    }

    private static byte[] formPartBytesContent(RawFormPart rawFormPart, int bodyIndex) {
        // TODO
        return null;
    }

    private static File formPartFileContent(RawFormPart rawFormPart, int bodyIndex) {
        var formBodyFile = new File(rawFormPart.contentFile().getAbsolutePath() + "-body");
        // TODO
        return formBodyFile;
    }

    public record Form(List<FormPart> parts) {

        public Optional<FormPart> part(String name) {
            return parts.stream().filter(p -> p.name.equals(name)).findAny();
        }
    }

    public record FormPart(String name,
                           String filename,
                           String contentType,
                           byte[] contentBytes,
                           File contentFile) {

        public static FormPart ofContent(String name, String filename, String contentType, byte[] content) {
            return new FormPart(name, filename, contentType, content, null);
        }

        public static FormPart ofFile(String name, String filename, String contentType, File file) {
            return new FormPart(name, filename, contentType, null, file);
        }

        public boolean ofBytes() {
            return contentBytes != null;
        }
    }

    public record RawFormPart(byte[] contentBytes,
                              File contentFile,
                              boolean last) {

        static RawFormPart ofBytes(byte[] content, boolean last) {
            return new RawFormPart(content, null, last);
        }

        static RawFormPart ofFile(File content, boolean last) {
            return new RawFormPart(null, content, last);
        }

        public boolean ofBytes() {
            return contentBytes != null;
        }

        public InputStream contentStream() {
            try {
                if (ofBytes()) {
                    return new ByteArrayInputStream(contentBytes);
                }
                return new FileInputStream(contentFile);
            } catch (Exception e) {
                throw new RuntimeException("Failed to turn request body into bytes stream", e);
            }
        }

        public long size() {
            return ofBytes() ? contentBytes.length : contentFile.length();
        }
    }

    private record HeadersWithBodyIndex(Map<String, String> headers, int bodyIdx) {
    }

    private record ContentDispositionData(String name, String filename) {
    }

    public record Params(int size,
                         int maxInMemorySize,
                         String boundary,
                         String defaultContentType,
                         File tmpFilesDir) {
    }
}
