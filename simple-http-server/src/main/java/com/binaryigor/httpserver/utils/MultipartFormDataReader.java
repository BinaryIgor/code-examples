package com.binaryigor.httpserver.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class MultipartFormDataReader {

    private static final int TWO_DASHES_BYTES_SIZE = toAsciiBytes("--").length;
    private static final String HEADERS_SEPARATOR = "\r\n";
    private static final String HEAD_BODY_SEPARATOR = HEADERS_SEPARATOR + HEADERS_SEPARATOR;
    private static final int PART_BYTES_PACKET_SIZE = 250_000;
    private static final int MAX_HEADERS_SIZE = 5_000;

    public static List<FormPart> read(Supplier<InputStream> source, Params params) {
        var dashedBoundary = "--" + params.boundary;
        var dashedBoundaryByteSize = toAsciiBytes(dashedBoundary).length;
        var firstBoundaryBytesSize = dashedBoundaryByteSize + TWO_DASHES_BYTES_SIZE;
        var betweenOrLastBoundaryPrefixBytes = toAsciiBytes("\r\n" + dashedBoundary);
        var betweenBoundaryBytesSize = TWO_DASHES_BYTES_SIZE + dashedBoundaryByteSize + TWO_DASHES_BYTES_SIZE;
        var lastBoundaryBytesSize = betweenBoundaryBytesSize + TWO_DASHES_BYTES_SIZE;
        var betweenBoundaryPostfixBytes = toAsciiBytes("\r\n");
        var lastBoundaryPostfixBytes = toAsciiBytes("--\r\n");

        var multipartFilePrefix = "multipart-" + UUID.randomUUID() + "-";

        var rawParts = new ArrayList<RawFormPart>();

        long read = firstBoundaryBytesSize;
        int part = 0;
        while (read < params.size) {
            try (var is = source.get()) {
                is.skip(read);

                var partFile = Path.of(params.tmpFilesDir.getAbsolutePath(), multipartFilePrefix + part).toFile();
                var formPart = readUntil(is, params.maxInMemorySize, partFile,
                        betweenOrLastBoundaryPrefixBytes,
                        betweenBoundaryPostfixBytes,
                        lastBoundaryPostfixBytes);

                rawParts.add(formPart);

                var boundaryBytesSize = formPart.last() ? lastBoundaryBytesSize : betweenBoundaryBytesSize;
                read = read + boundaryBytesSize + formPart.size();
                part++;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return toFormParts(rawParts, params.defaultContentType);
    }

    private static byte[] toAsciiBytes(String string) {
        return string.getBytes(StandardCharsets.US_ASCII);
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

        while (read > 0) {
            int i = 0;
            while (i < read) {
                if (isPatternIndex(packet, read, i, boundaryPrefix)) {
                    if (isPatternIndex(packet, read,
                            i + boundaryPrefix.length, boundaryBetweenPartsPostfix)) {
                        return rawFormPartFromMemoryOrFile(fileOutputStream.get(), buffer, tmpFile, false);
                    }
                    if (isPatternIndex(packet, read,
                            i + boundaryPrefix.length,
                            boundaryLastPartPostfix)) {
                        return rawFormPartFromMemoryOrFile(fileOutputStream.get(), buffer, tmpFile, true);
                    }
                }

                buffer.write(packet[i++]);

                if (buffer.size() >= maxInMemorySize) {
                    if (fileOutputStream.get() == null) {
                        fileOutputStream.set(new FileOutputStream(tmpFile, true));
                    }
                    fileOutputStream.get().write(buffer.toByteArray());
                    buffer.reset();
                }
            }

            read = source.read(packet);
        }

        return rawFormPartFromMemoryOrFile(fileOutputStream.get(), buffer, tmpFile, false);
    }

    private static RawFormPart rawFormPartFromMemoryOrFile(FileOutputStream fileOutputStream,
                                                           ByteArrayOutputStream buffer,
                                                           File tmpFile,
                                                           boolean last) {
        if (fileOutputStream == null) {
            return RawFormPart.ofBytes(buffer.toByteArray(), last);
        }
        try {
            fileOutputStream.write(buffer.toByteArray());
            return RawFormPart.ofFile(tmpFile, last);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
        int bodyIdx;
        String name = null;
        String filename = null;
        String contentType;

        try (var is = formPart.contentStream()) {
            var headBytes = is.readNBytes(MAX_HEADERS_SIZE);
            var headersString = new String(headBytes, StandardCharsets.US_ASCII);

            bodyIdx = headersString.indexOf(HEAD_BODY_SEPARATOR);
            if (bodyIdx < 0) {
                throw new RuntimeException("Body index cannot be found in a part!");
            }
            bodyIdx += HEAD_BODY_SEPARATOR.length();

            var headers = new HashMap<String, String>();

            for (var l : headersString.split(HEADERS_SEPARATOR)) {
                if (l.isBlank()) {
                    break;
                }
                var kv = l.split(":", 2);
                if (kv.length == 2) {
                    headers.put(kv[0].strip().toLowerCase(), kv[1].strip());
                }
            }

            contentType = headers.getOrDefault(HttpHeaders.CONTENT_TYPE, defaultContentType);

            var contentDispositionLine = headers.get("content-disposition");
            if (contentDispositionLine == null) {
                throw new RuntimeException("Content disposition is required but cannot be found");
            }

            for (var parts : contentDispositionLine.split(";")) {
                var kv = parts.split("=", 2);
                if (kv.length != 2) {
                    continue;
                }
                var key = kv[0].strip();
                var value = kv[1].strip();
                if (value.startsWith("\"")) {
                    value = value.substring(1);
                }
                if (value.endsWith("\"")) {
                    value = value.substring(0, value.length() - 1);
                }
                if (key.equals("name")) {
                    name = value;
                } else if (key.equals("filename")) {
                    filename = value;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (formPart.ofBytes()) {
            var body = formPartBytesContent(formPart, bodyIdx);
            return FormPart.ofContent(name, filename, contentType, body);
        }

        var body = formPartFileContent(formPart, bodyIdx);
        return FormPart.ofFile(name, filename, contentType, body);
    }

    private static byte[] formPartBytesContent(RawFormPart rawFormPart, int bodyIndex) {
        return Arrays.copyOfRange(rawFormPart.contentBytes(), bodyIndex, rawFormPart.contentBytes().length);
    }

    private static File formPartFileContent(RawFormPart rawFormPart, int bodyIndex) {
        var formBodyFile = new File(rawFormPart.contentFile().getAbsolutePath() + "-body");
        try (var is = rawFormPart.contentStream();
             var os = new FileOutputStream(formBodyFile)) {
            is.skip(bodyIndex);
            is.transferTo(os);
            return formBodyFile;
        } catch (Exception e) {
            throw new RuntimeException(e);
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

        public long size() {
            return ofBytes() ? contentBytes.length : contentFile.length();
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
    }

    public record Params(int size,
                         int maxInMemorySize,
                         String boundary,
                         String defaultContentType,
                         File tmpFilesDir) {
    }
}
