package com.binaryigor.httpserver.htmlexamples;

import com.binaryigor.httpserver.server.HttpRequest;
import com.binaryigor.httpserver.server.HttpResponse;
import com.binaryigor.httpserver.server.SimpleHttpServer;
import com.binaryigor.httpserver.utils.HttpHeaders;
import com.binaryigor.httpserver.utils.HttpRequests;
import com.binaryigor.httpserver.utils.HttpResponses;
import com.binaryigor.httpserver.utils.MultipartFormDataReader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class HtmlMultipartFileUploadServer {

    private static final Path TMP_FILES_PATH = Path.of("/tmp/simple-http-server");
    private static final int MAX_IN_MEMORY_PART_SIZE = 250_000;

    public static void main(String[] args) {
        var server = new SimpleHttpServer(8080)
                .configure(c -> {
                    c.bodyAsFileThreshold = 250_000;
                    c.tmpFilesPath = TMP_FILES_PATH;
                    c.verboseMode = true;
                });

        var uploadRepository = new FileUploadRepository();

        server.start(r -> {
            if (r.method().equals("POST") && r.url().contains("/upload")) {
                return handleUpload(r, uploadRepository);
            }

            var queryParams = HttpRequests.parseQueryParams(r.url(), StandardCharsets.UTF_8);
            var fileError = Boolean.parseBoolean(queryParams.getOrDefault("fileError", "false"));
            var descriptionError = Boolean.parseBoolean(queryParams.getOrDefault("descriptionError", "false"));
            var description = queryParams.getOrDefault("description", "");

            var uploadForm = uploadFormHTML(fileError, description, descriptionError);
            var uploadsList = uploadsListHTML(uploadRepository.all());
            return HttpResponses.html(200, indexHTML(uploadForm, uploadsList), "utf-8");
        });
    }

    private static HttpResponse handleUpload(HttpRequest request, FileUploadRepository uploadRepository) {
        var contentTypeHeader = request.header(HttpHeaders.CONTENT_TYPE).orElse(null);
        if (contentTypeHeader == null) {
            return HttpResponses.text(400, "content type header required");
        }
        var boundaryPart = contentTypeHeader.split("boundary=", 2);
        if (boundaryPart.length != 2) {
            return HttpResponses.text(400, "boundary in content type header is required");
        }
        var boundary = boundaryPart[1];
        var contentLength = Integer.parseInt(request.header(HttpHeaders.CONTENT_LENGTH).orElse("0"));

        var readParams = new MultipartFormDataReader.Params(contentLength, MAX_IN_MEMORY_PART_SIZE, boundary,
                "text/plain", TMP_FILES_PATH.toFile());
        var form = MultipartFormDataReader.read(request::bodyAsStream, readParams);

        var file = uploadedFileFromForm(form).orElse(null);
        var description = uploadedDescriptionFromForm(form).orElse(null);

        var fileError = file == null;
        var descriptionError = description == null || description.isBlank();

        if (!fileError && !descriptionError) {
            uploadRepository.save(new FileUpload(file, description));
        }

        return HttpResponses.redirect("/&fileError=%s&descriptionError=%s".formatted(fileError, descriptionError));
    }

    private static Optional<String> uploadedFileFromForm(List<MultipartFormDataReader.FormPart> formParts) {
        return formParts.stream()
                .filter(fp -> fp.name().equals("file") &&
                        ((fp.contentBytes() != null && fp.contentBytes().length > 0 ) || fp.contentFile() != null))
                .map(fp -> {
                    if (fp.ofBytes()) {
                        return new String(fp.contentBytes(), StandardCharsets.UTF_8);
                    }
                    return fp.contentFile().getAbsolutePath();
                })
                .findAny();
    }

    private static Optional<String> uploadedDescriptionFromForm(List<MultipartFormDataReader.FormPart> formParts) {
        return formParts.stream()
                .filter(fp -> fp.name().equals("description") && fp.contentBytes() != null)
                .map(fp -> new String(fp.contentBytes(), StandardCharsets.UTF_8))
                .findAny();
    }

    private static String indexHTML(String uploadForm, String uploadsList) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <title>Multipart File Uploads</title>
                </head>
                <body>
                    <h1>Multipart File Uploads</h1>
                    %s
                    %s
                </body>
                </html>
                """.formatted(uploadForm, uploadsList);
    }

    private static String uploadFormHTML(boolean fileError,
                                         String description,
                                         boolean descriptionError) {
        return """
                <form style="padding: 4px" method="post" action="/upload" enctype="multipart/form-data">
                <input style="padding: 4px; display: block" type="file" name="file">
                <p style="margin-top: 2px; margin-bottom: 8px; font-style: italic; color: red;%s">File required</p>
                <input style="padding: 4px; margin-top: 4px; display: block" type="text" name="description" placeholder="Description" value="%s">
                <p style="margin-top: 2px; margin-bottom: 8px; font-style: italic; color: red;%s">Description required</p>
                <input style="padding: 4px; margin-top: 4px" type="submit" value="Upload">
                </form>""".formatted(
                fileError ? "" : "display: none",
                description, descriptionError ? "" : "display: none");
    }

    private static String uploadsListHTML(List<FileUpload> uploads) {
        var uploadsHTML = uploads.stream()
                .map(u -> """
                        <div>
                          <p>File/content: %s</p>
                          <p>Description: %s</p>
                        </div>
                        """.formatted(u.file(), u.description()))
                .collect(Collectors.joining("\n"));
        return """
                <div>
                    <h2>Uploaded</h2>
                    %s
                </div>
                """.formatted(uploadsHTML.isBlank() ? "<p>No uploads yet - upload something!</p>" : uploadsHTML);
    }

}
