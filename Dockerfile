FROM ubuntu:latest
LABEL authors="marko"

ENTRYPOINT ["top", "-b"]