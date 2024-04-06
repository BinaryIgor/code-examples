FROM alpine:3.19
RUN apk --no-cache add postgresql16-client
COPY restore.sh /.
RUN chmod +x /restore.sh
ENTRYPOINT ./restore.sh