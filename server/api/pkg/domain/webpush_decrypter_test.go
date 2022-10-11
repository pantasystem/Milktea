package domain_test

import (
	"encoding/base64"
	"testing"

	"github.com/stretchr/testify/assert"
	"systems.panta.milktea/pkg/domain"
)

func TestSha256(t *testing.T) {
	expect := "yb1GUyy4+k/dcGubgS2+QRWvBW0VgTpw3H9JLVx2Iss="
	result := domain.Sha256([]byte("hogehoge"), []byte("piyopiyo"))
	assert.Equal(t, expect, base64.StdEncoding.EncodeToString(result))
}

func TestDecodeBase64(t *testing.T) {
	result, err := domain.DecodeBase64("test")
	assert.Nil(t, err)
	assert.Equal(t, []byte{0xb5, 0xeb, 0x2d}, result)
}
