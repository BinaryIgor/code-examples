FROM python:3.11-alpine3.19
RUN apk --no-cache add postgresql16-client

COPY requirements.txt /.
RUN pip3 install -r /requirements.txt

COPY backup.sh remove_old_backups.py upload_backup_to_do_spaces.py /.
RUN chmod +x /backup.sh

ENTRYPOINT ./backup.sh