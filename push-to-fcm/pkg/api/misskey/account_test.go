package misskey

import (
	"context"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestFindSelf(t *testing.T) {
	// Create test server
	handler := func(w http.ResponseWriter, r *http.Request) {
		assert.Equal(t, "POST", r.Method)
		assert.Equal(t, "/api/i", r.URL.Path)

		// Check request body
		decoder := json.NewDecoder(r.Body)
		var req FindSelfRequest
		err := decoder.Decode(&req)
		assert.NoError(t, err)
		assert.Equal(t, "token", req.I)

		// Send response
		ac := Account{
			ID:       "test-id",
			UserName: "test-username",
			Name:     nil,
		}
		res, err := json.Marshal(&ac)
		assert.NoError(t, err)
		w.Header().Set("Content-Type", "application/json")
		w.Write(res)
	}
	testServer := httptest.NewServer(http.HandlerFunc(handler))
	defer testServer.Close()

	// Create account client
	client := AccountClient{
		BaseUrl: testServer.URL,
		Token:   "token",
	}

	// Call FindSelf method
	ac, err := client.FindSelf(context.Background())

	// Check response
	assert.NoError(t, err)
	assert.Equal(t, "test-id", ac.ID)
	assert.Equal(t, "test-username", ac.UserName)
	assert.Nil(t, ac.Name)
}
