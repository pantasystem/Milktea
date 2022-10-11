package domain_test

import (
	"encoding/base64"
	"encoding/hex"
	"fmt"
	"strconv"
	"testing"

	"github.com/stretchr/testify/assert"
	"systems.panta.milktea/pkg/domain"
)

// var (
// 	publicKey  = "BJgVD2cj1pNKNR2Ss3U_8e7P9AyoL5kWaxVio5aO16Cvnx-P1r7HH8SRb-h5tuxaydZ1ky3oO0V40s6t_uN1SdA"
// 	privateKey = "ciQ800G-6jyKWf6KKG94g5rCSU_l_rgbHbyHny_UsIM"
// 	authSecret = "43w_wOVYeF9XzyRyZL3O8g"
// )

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

func TestDecodeBase642(t *testing.T) {
	publicKey := "BJgVD2cj1pNKNR2Ss3U_8e7P9AyoL5kWaxVio5aO16Cvnx-P1r7HH8SRb-h5tuxaydZ1ky3oO0V40s6t_uN1SdA"
	privateKey := "ciQ800G-6jyKWf6KKG94g5rCSU_l_rgbHbyHny_UsIM"
	authSecret := "43w_wOVYeF9XzyRyZL3O8g"
	result, err := domain.DecodeBase64(publicKey)

	assert.Nil(t, err)
	assert.NotNil(t, result)
	fmt.Printf("base 64 result %s", result)

	result, err = domain.DecodeBase64(privateKey)
	assert.Nil(t, err)
	assert.NotNil(t, result)

	result, err = domain.DecodeBase64(authSecret)
	fmt.Printf("error:%s\n", err.Error())
	assert.Nil(t, err)
	assert.NotNil(t, result)

}
func TestDecrypt(t *testing.T) {
	publicKey := "BJgVD2cj1pNKNR2Ss3U_8e7P9AyoL5kWaxVio5aO16Cvnx-P1r7HH8SRb-h5tuxaydZ1ky3oO0V40s6t_uN1SdA"
	privateKey := "ciQ800G-6jyKWf6KKG94g5rCSU_l_rgbHbyHny_UsIM"
	authSecret := "43w_wOVYeF9XzyRyZL3O8g"
	body := "B8TZpK9vmRlCbkFTLG6l5gAAEABBBE8bArSb8EH1d8PH0J4Wrf/p2CY2rLUx55TgRayuyH0B3ZjJ2JiMDJH+c2FsA526yd08GVf7QjwqGWnNo4+LwpI4dyaI36CvBMPCf1lOAF50FV7JkgvMGyuYnzgUOh5KSvjDygDpygjRdFYI7amKXXCeRSMcwqn8lXgP1G6CUI43z88+bg/piRVBEnnALn2d60vzUzQNSXUdHAGCQ6aElewpxe3xT74ua0Bxcd4tB3fFaUzpOzYApQRpIlG5ITDTd1haMCuarE5vUGO+oPMsIv1nJO5keRhcvKBWSynj3d0+pGkgajrNjdQentooEQt5GmntKus+mUzLT+UQN1KNWnR3FN9LjsXX8fTk3Vhl7NabiW+N/vDPxI0/lw0VdBNeI460XcWi8aJd6Yb4THB0DjJ5p2JwXHB3zZ1dGSOA6f2hQWTQbVM/9Kisji0SEYdmysFNJaiajax2IeP8eG5lmJ/Lsq6Fs+CCRHnEUZIENEo0Mw3H44Wx/zWG1mEJxffIeuWUFbCcyfD8JYZkvilUu6azhPkTbhidbZ/NQO8oJHU21K5qQbOxiFhPD//cUORE/aCeF8uMdS1PgCvD1I0wfGOxmj3tLOXyvjmTNVzR7jZ7jRMro69/9K2dZ0YrcVMHgZuXfr9OtBD25FoHkkZ6uOxU8rX+FBDt4Af5A3mseX72dUFuhSCYh2akdrhUldvqnjb7IQKQpLrBDh4t4YGmPgXvVs/uSa48MOWNfgWUgng2BReDD7RwT1MeF3KQObAdOaocRZFRWx51JgDv16o7qr5Q/vd5TPIXGKloMgMMwCA5sAsIhmBdBZLjpPrFEspPVJvfGgcu7Hdb/9/xRAOsXBjlcg4iDetdTjkBpr5Gysn1EJfEZZAJaOTD4kgKtKURmynxvtX/nDKLmaIUt1f8Dfo/UKhq+9HcSfoohH/9AcFQCiSPPJrfJFt1BstJi/PhwbmWAMvxwbrzRZXzmXk8iiTEqbnlOqml+dnDZ4aJUjTrCb5OxbfaGsr3kXATcKT8jNKTOK6/0r25NNyGHCXlaF7awdjzQXLamWrsa/ieFmRnrpWKGobW45uNXEwiKmXvDWcVBH5PDeKQpu3IQSdzBXJSpYtrwpwnyyMbI0h+Y8JufjLWFZqNqCpcHG7CiwnKAR3icC3MLbZOS8oxo2AHYfhWXxzuqU5apbuCAzKNtk4N86wEuFLb1XkpI2AJ9u9k8MDBUdRxhxWhgKbQ2EOh4KZeKQDD6olVWR7ECU0otyM1g3+Ej89WOSrqJGhk+S704v1A73+EED0MFaY+MjrIOhCsrgL/Tcu0bbHYjOiuoEFN3Uf75Nr/+qTS6SatkHtqyIi24Y/NWCIZ976946gtym8iWi9aJieCLNYE4IuKjhgIOVrWqo57nbg0/4OQvTubSzBbis3x1X+PrL3IrSKufEs5pzEPuRbyhQfLHJ2gL4v2S0od5v9mxT3XacJAq2rHEd3/GXIxfgwWm/s/aSAIOnxU5DIM71UbVuEKBFtpk0IGt6DzsZiC7lELGUfR/VSv7Hg0d4aMmAsHPmo5r9FDDsALxp2OfL1XMg8/rPnUvRoKjdkY61MHfH7rQoXfy3Yidtx7dHeZ/O21tT/V83HXC6xNXpcGew0wyTjWGBqhVXkRZUjN3EteFniv90gg0SIPW4zzKJ42/uf0tzg76gZiJboAqWwlXmt+FmjnMg4990vA1obltewm6NIRha6EJb4fpafsnA8Hqk/rM7zu57vBLCr22o0VC7gery4xbRMW/es="

	decrypter, err := domain.NewDecrypter(authSecret, publicKey, privateKey)
	assert.NotNil(t, decrypter)
	assert.Nil(t, err)
	// fmt.Printf("decrypter generatedああああ\n")
	// assert.Nil(t, err)
	result, err := decrypter.Decrypt(body)
	assert.Nil(t, err)
	fmt.Printf("result:%s\n", *result)

}

func TestGenerateSharedSecret(t *testing.T) {

}

func TestHex(t *testing.T) {
	result := hex.EncodeToString([]byte{5, 10, 7})
	assert.Equal(t, "050a07", result)
	fmt.Printf("hex result:%s\n", result)
	length, err := strconv.ParseInt(result, 16, 64)
	assert.Nil(t, err)
	assert.Equal(t, int64(330247), length)
}
