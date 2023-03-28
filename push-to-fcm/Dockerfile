FROM golang:1.20.2-alpine3.17

ENV GOPATH /go
ENV GO111MODULE on

RUN apk update && \
    apk --no-cache add git

RUN mkdir /go/src/app
WORKDIR /go/src/app

ADD . /go/src/app

RUN go mod tidy && \
    go install github.com/cosmtrek/air@v1.27.3

CMD ["air", "-c", ".air.toml"]