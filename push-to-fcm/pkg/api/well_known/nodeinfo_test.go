package wellknown

import (
	"encoding/json"
	"io"
	"net/http"
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestNodeInfo_GiveMisskeyPantasystemCom(t *testing.T) {
	url := "https://misskey.pantasystem.com/nodeinfo/2.0"

	resp, err := http.Get(url)
	assert.NoError(t, err)
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	assert.NoError(t, err)

	var ni NodeInfo
	err = json.Unmarshal(body, &ni)
	assert.NoError(t, err)

	assert.Equal(t, "2.0", ni.Version)
	assert.Equal(t, "misskey", ni.Software.Name)
	assert.NotEmpty(t, ni.Software.Version)
	assert.Contains(t, ni.Protocols, "activitypub")
	assert.NotNil(t, ni.OpenRegistrations)

	assert.True(t, ni.Usage.Users.Total > 0)
	assert.True(t, ni.Usage.Users.ActiveMonth >= 0)
	assert.True(t, ni.Usage.Users.ActiveHalfYear >= 0)
	assert.True(t, ni.Usage.Users.ActiveUsers.Total >= 0)

	assert.True(t, ni.Usage.LocalPosts.Total >= 0)
	assert.True(t, ni.Usage.LocalPosts.ActiveMonth >= 0)
	assert.True(t, ni.Usage.LocalPosts.ActiveHalfYear >= 0)

	assert.True(t, ni.Usage.Posts.Total >= 0)
	assert.True(t, ni.Usage.Connections.Total >= 0)
}

func TestNodeInfo_GiveFedibirdCom(t *testing.T) {
	url := "https://fedibird.com/nodeinfo/2.0"

	resp, err := http.Get(url)
	assert.NoError(t, err)
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	assert.NoError(t, err)

	var ni NodeInfo
	err = json.Unmarshal(body, &ni)
	assert.NoError(t, err)

	assert.Equal(t, "2.0", ni.Version)
	assert.Equal(t, "fedibird", ni.Software.Name)
	assert.NotEmpty(t, ni.Software.Version)
	assert.Contains(t, ni.Protocols, "activitypub")
	assert.NotNil(t, ni.OpenRegistrations)

	assert.NotNil(t, ni.Usage.Users)
	assert.True(t, ni.Usage.Users.Total > 0)
	assert.True(t, ni.Usage.Users.ActiveMonth >= 0)
	assert.True(t, ni.Usage.Users.ActiveHalfYear >= 0)
	assert.True(t, ni.Usage.Users.ActiveUsers.Total >= 0)

	assert.NotNil(t, ni.Usage.LocalPosts)
	assert.True(t, ni.Usage.LocalPosts.Total >= 0)
	assert.True(t, ni.Usage.LocalPosts.ActiveMonth >= 0)
	assert.True(t, ni.Usage.LocalPosts.ActiveHalfYear >= 0)

	assert.NotNil(t, ni.Usage.Posts)
	assert.True(t, ni.Usage.Posts.Total >= 0)
	assert.NotNil(t, ni.Usage.Connections)
	assert.True(t, ni.Usage.Connections.Total >= 0)

	if ni.Metadata.Peers != nil {
		assert.NotEmpty(t, ni.Metadata.Peers)
	} else {
		assert.Nil(t, ni.Metadata.Peers)
	}
}

func TestNodeInfo_GiveMastodonSocial(t *testing.T) {
	url := "https://mastodon.social/nodeinfo/2.0"

	resp, err := http.Get(url)
	assert.NoError(t, err)
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	assert.NoError(t, err)

	var ni NodeInfo
	err = json.Unmarshal(body, &ni)
	assert.NoError(t, err)

	assert.Equal(t, "2.0", ni.Version)
	assert.Equal(t, "mastodon", ni.Software.Name)
	assert.NotEmpty(t, ni.Software.Version)
	assert.Contains(t, ni.Protocols, "activitypub")
	assert.NotNil(t, ni.OpenRegistrations)

	assert.NotNil(t, ni.Usage.Users)
	assert.True(t, ni.Usage.Users.Total > 0)
	assert.True(t, ni.Usage.Users.ActiveMonth >= 0)
	assert.True(t, ni.Usage.Users.ActiveHalfYear >= 0)
	assert.True(t, ni.Usage.Users.ActiveUsers.Total >= 0)

	assert.NotNil(t, ni.Usage.LocalPosts)
	assert.True(t, ni.Usage.LocalPosts.Total >= 0)
	assert.True(t, ni.Usage.LocalPosts.ActiveMonth >= 0)
	assert.True(t, ni.Usage.LocalPosts.ActiveHalfYear >= 0)

	assert.NotNil(t, ni.Usage.Posts)
	assert.True(t, ni.Usage.Posts.Total >= 0)
	assert.NotNil(t, ni.Usage.Connections)
	assert.True(t, ni.Usage.Connections.Total >= 0)

}

func TestNodeInfo_GiveCalckey(t *testing.T) {
	url := "https://calc.panta.systems/nodeinfo/2.0"

	resp, err := http.Get(url)
	assert.NoError(t, err)
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	assert.NoError(t, err)

	var ni NodeInfo
	err = json.Unmarshal(body, &ni)
	assert.NoError(t, err)

	assert.Equal(t, "2.0", ni.Version)
	assert.Equal(t, "calckey", ni.Software.Name)
	assert.NotEmpty(t, ni.Software.Version)
	assert.Contains(t, ni.Protocols, "activitypub")
	assert.NotNil(t, ni.OpenRegistrations)

	assert.True(t, ni.Usage.Users.Total > 0)
	assert.True(t, ni.Usage.Users.ActiveMonth >= 0)
	assert.True(t, ni.Usage.Users.ActiveHalfYear >= 0)
	assert.True(t, ni.Usage.Users.ActiveUsers.Total >= 0)

	assert.True(t, ni.Usage.LocalPosts.Total >= 0)
	assert.True(t, ni.Usage.LocalPosts.ActiveMonth >= 0)
	assert.True(t, ni.Usage.LocalPosts.ActiveHalfYear >= 0)

	assert.True(t, ni.Usage.Posts.Total >= 0)
	assert.True(t, ni.Usage.Connections.Total >= 0)
}

func TestNodeInfo_GiveMeisskey(t *testing.T) {
	url := "https://misskey.m544.net/nodeinfo/2.0"

	resp, err := http.Get(url)
	assert.NoError(t, err)
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	assert.NoError(t, err)

	var ni NodeInfo
	err = json.Unmarshal(body, &ni)
	assert.NoError(t, err)

	assert.Equal(t, "2.0", ni.Version)
	assert.Equal(t, "meisskey", ni.Software.Name)
	assert.NotEmpty(t, ni.Software.Version)
	assert.Contains(t, ni.Protocols, "activitypub")
	assert.NotNil(t, ni.OpenRegistrations)

	assert.True(t, ni.Usage.Users.Total > 0)
	assert.True(t, ni.Usage.Users.ActiveMonth >= 0)
	assert.True(t, ni.Usage.Users.ActiveHalfYear >= 0)
	assert.True(t, ni.Usage.Users.ActiveUsers.Total >= 0)

	assert.True(t, ni.Usage.LocalPosts.Total >= 0)
	assert.True(t, ni.Usage.LocalPosts.ActiveMonth >= 0)
	assert.True(t, ni.Usage.LocalPosts.ActiveHalfYear >= 0)

	assert.True(t, ni.Usage.Posts.Total >= 0)
	assert.True(t, ni.Usage.Connections.Total >= 0)
}
