package wellknown

type NodeInfo struct {
	Version           string       `json:"version"`
	Software          Software     `json:"software"`
	Protocols         []string     `json:"protocols"`
	NodeInfoURL       string       `json:"nodeinfo_url,omitempty"`
	OpenRegistrations bool         `json:"open_registrations"`
	Usage             Usage        `json:"usage,omitempty"`
	Metadata          NodeInfoMeta `json:"metadata,omitempty"`
	ProtocolsLocal    []string     `json:"protocols_local,omitempty"`
}

type Software struct {
	Name    string `json:"name"`
	Version string `json:"version"`
}

type Usage struct {
	Users       UserUsage  `json:"users"`
	LocalPosts  LocalUsage `json:"local_posts,omitempty"`
	Posts       TotalUsage `json:"posts,omitempty"`
	Connections TotalUsage `json:"connections,omitempty"`
}

type UserUsage struct {
	Total          int       `json:"total"`
	ActiveMonth    int       `json:"activeMonth,omitempty"`
	ActiveHalfYear int       `json:"activeHalfYear,omitempty"`
	ActiveUsers    UserCount `json:"activeUsers,omitempty"`
}

type UserCount struct {
	Total int `json:"total"`
	New   int `json:"new,omitempty"`
}

type LocalUsage struct {
	Total          int `json:"total"`
	ActiveMonth    int `json:"activeMonth,omitempty"`
	ActiveHalfYear int `json:"activeHalfYear,omitempty"`
}

type TotalUsage struct {
	Total int `json:"total"`
}

type NodeInfoMeta struct {
	Peers []string `json:"peers,omitempty"`
}
