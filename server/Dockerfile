FROM golang:1.18.7-bullseye

ENV GOPATH /go
ENV GO111MODULE on

RUN apt update && \
    apt install git

RUN mkdir /go/src/app
WORKDIR /go/src/app

ADD ./api /go/src/app

RUN go mod tidy && \
    go install github.com/cosmtrek/air@v1.27.3

CMD ["air", "-c", ".air.toml"]