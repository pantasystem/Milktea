package wellknown

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestFindNodeInfo(t *testing.T) {
	wellknown := WellKnown{
		BaseUrl: "https://misskey.io",
	}

	ni, err := wellknown.FindNodeInfo()
	assert.NoError(t, err)
	assert.NotNil(t, ni)
	assert.Equal(t, "2.0", ni.Version)
	assert.Equal(t, "misskey", ni.Software.Name)
	assert.True(t, len(ni.Protocols) > 0)
	assert.True(t, ni.Usage.Users.Total >= 0)
}
