package domain

import (
	"crypto/aes"
	"crypto/cipher"
	"crypto/elliptic"
	"crypto/hmac"
	"crypto/sha256"
	"encoding/base64"
	"encoding/hex"
	"fmt"
	"strconv"

	"github.com/aead/ecdh"
)

type Decrypter struct {
	authSecret      []byte
	receiverPublic  []byte
	receiverPrivate []byte
}

func Sha256(key []byte, data []byte) []byte {
	hash := hmac.New(sha256.New, []byte(key))
	hash.Write(data)
	return hash.Sum(nil)
}

func DecodeBase64(src string) ([]byte, error) {
	result, err := base64.URLEncoding.DecodeString(src)
	if err == nil {
		return result, err
	}
	result, err = base64.RawURLEncoding.DecodeString(src)
	if err == nil {
		return result, err
	}

	result, err = base64.StdEncoding.DecodeString(src)
	if err == nil {
		return result, err
	}
	return base64.RawStdEncoding.DecodeString(src)
}

func NewDecrypter(authSecret string, receiverPublic string, receiverPrivate string) (*Decrypter, error) {

	asb, err := DecodeBase64(authSecret)
	if err != nil {
		fmt.Printf("decode failed:%s", err.Error())
		return nil, err
	}
	rcpb, err := DecodeBase64(receiverPublic)
	if err != nil {
		return nil, err
	}

	rpb, err := DecodeBase64(receiverPrivate)
	if err != nil {
		return nil, err
	}
	decrypter := Decrypter{
		authSecret:      asb,
		receiverPublic:  rcpb,
		receiverPrivate: rpb,
	}
	return &decrypter, nil
}

func (r Decrypter) Decrypt(base64Body string) (*string, error) {

	enc, err := DecodeBase64(base64Body)
	if err != nil {
		fmt.Printf("bodyをdecode base64するのに失敗:%s\n", err.Error())
		return nil, err
	}
	body := enc

	salt := body[0:16]
	// rs := body[16:(16 + 4)]
	idlen, err := r.GetIdlen(body)
	if err != nil {
		return nil, err
	}
	keyId := r.GetKeyId(body, idlen)
	content := body[(16 + 4 + 1 + idlen):]

	senderPublic := base64.StdEncoding.EncodeToString(keyId)
	fmt.Printf("senderPublic:%s\n", senderPublic)

	// fmt.Printf("salt: %s, rs:%s, idlenHex:%s, idlen:%s, keyId:%s, content:%s, senderPublic:%s", salt, rs, idlenHex, idlen, keyId, content, &senderPublic)

	fmt.Printf("keyid:%s\n", keyId)

	sharedSecret, err := r.GenerateSharedKey(keyId)
	if err != nil {
		fmt.Printf("Get SharedSecret failed\n")
		return nil, err
	}
	fmt.Printf("sharedSecret:%s\n", sharedSecret)
	fmt.Printf("salt:%s, content:%s\n", salt, content)

	prkKey := Sha256([]byte(r.authSecret), sharedSecret)
	keyInfo := "WebPush: info\000" + string(r.receiverPublic) + string(senderPublic) + "\001"
	ikm := Sha256(prkKey, []byte(keyInfo))
	prk := Sha256([]byte(salt), ikm)

	cekInfo := "Content-Encoding: aes128gcm\000\001"
	cek := Sha256(prk, []byte(cekInfo))[0:16]

	nonceInfo := "Content-Encoding: nonce\000\001"
	nonce := Sha256(prk, []byte(nonceInfo))[0:12]
	fmt.Printf("cek:%s, nonce:%s\n", cek, nonce)
	iv := nonce
	block, err := aes.NewCipher([]byte(cek))
	if err != nil {
		fmt.Printf("aes.NewCipherに失敗")
		return nil, err
	}
	aesgcm, err := cipher.NewGCM(block)
	if err != nil {
		return nil, err
	}
	plaintext, err := aesgcm.Open(nil, []byte(iv), []byte(content), nil)
	if err != nil {
		fmt.Printf("aesgcm.Openに失敗:%s", err.Error())

		return nil, err
	}

	for string(plaintext[(len(plaintext)-1):]) != "}" {
		plaintext = plaintext[(len(plaintext) - 1):]
	}
	hoge := string(plaintext)
	return &hoge, nil

}

func (r Decrypter) GetKeyId(body []byte, idlen int64) []byte {
	return body[(16 + 4 + 1):(16 + 4 + 1 + idlen)]
}

func (r Decrypter) GetIdlen(body []byte) (int64, error) {
	idlenHex := hex.EncodeToString(body[(16 + 4):(16 + 4 + 1)])
	return strconv.ParseInt(idlenHex, 16, 64)
}
func (r Decrypter) GenerateSharedKey(keyId []byte) ([]byte, error) {

	p256 := elliptic.P256()

	generic := ecdh.Generic(p256)

	x, y := elliptic.Unmarshal(p256, keyId)
	if x == nil || y == nil {
		return nil, fmt.Errorf("Invalid keyId")
	}
	fmt.Printf("x:%s, y:%s\n", x, y)
	return generic.ComputeSecret(r.receiverPrivate, ecdh.Point{X: x, Y: y}), nil
}
