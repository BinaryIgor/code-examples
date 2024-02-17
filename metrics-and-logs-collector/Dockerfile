FROM python:3.10-slim

COPY src/ /
COPY requirements.txt /requirements.txt

RUN pip install -r requirements.txt

ENTRYPOINT ["python3", "-u", "collector.py"]