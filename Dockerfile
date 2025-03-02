FROM ubuntu:latest
LABEL authors="niyi"

ENTRYPOINT ["top", "-b"]