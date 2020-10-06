package main

import (
	"encoding/json"
	"fmt"
	"github.com/gorilla/mux"
	"github.com/hashicorp/go-version"
	"io/ioutil"
	"log"
	"net/http"
	"os"
	"path/filepath"
	"sort"
)

var (
	mainDir       string
	apiDir        string
	wikiDir       string
	prototypesDir string
)

func main() {
	args := os.Args
	if len(args) >= 2 {
		mainDir = args[1]
	}

	apiDir = filepath.Join(mainDir, "api")
	prototypesDir = filepath.Join(mainDir, "prototypes")
	wikiDir = filepath.Join(prototypesDir, "wiki")

	router := mux.NewRouter()
	//router.Methods("GET")

	apiRouter := router.PathPrefix("/api").Methods("GET").Subrouter()
	apiRouter.HandleFunc("/", ApiListVersionsHandler)
	apiRouter.HandleFunc("/{version}/", ApiListVersionFilesHandler)
	apiRouter.HandleFunc("/{version}/{file}", ApiDownloadFileHandler)

	wikiRouter := router.PathPrefix("/wiki").Methods("GET").Subrouter()
	wikiRouter.HandleFunc("/{file}", WikiDownloadFileHandler)

	address := "0.0.0.0"
	port := "80"

	log.Printf("started listening on [%s:%s]", address, port)

	err := http.ListenAndServe(fmt.Sprintf("%s:%s", address, port), router)
	if err != nil {
		log.Fatalf("Error starting webserver: %s", err)
	}
}

func ApiListVersionsHandler(w http.ResponseWriter, r *http.Request) {
	// read out all folders in the main directory (all versions)
	fileInfos, err := ioutil.ReadDir(apiDir)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		return
	}

	var allDirs []string
	for _, fileInfo := range fileInfos {
		if fileInfo.IsDir() {
			allDirs = append(allDirs, fileInfo.Name())
		}
	}

	versions := make([]*version.Version, len(allDirs))
	for i, raw := range allDirs {
		v, _ := version.NewVersion(raw)
		versions[i] = v
	}

	// After this, the versions are properly sorted
	sort.Sort(version.Collection(versions))

	allDirs = nil
	for _, v := range versions {
		allDirs = append(allDirs, v.String())
	}

	json.NewEncoder(w).Encode(allDirs)
}

func ApiListVersionFilesHandler(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	version := vars["version"]
	fileInfos, err := ioutil.ReadDir(fmt.Sprintf("%s/%s", apiDir, version))
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		return
	}

	var allDirs []string
	for _, fileInfo := range fileInfos {
		allDirs = append(allDirs, fileInfo.Name())
	}

	json.NewEncoder(w).Encode(allDirs)
}

func ApiDownloadFileHandler(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	version := vars["version"]
	file := vars["file"]
	http.ServeFile(w, r, fmt.Sprintf("%s/%s/%s", apiDir, version, file))
}

func WikiDownloadFileHandler(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	file := vars["file"]
	http.ServeFile(w, r, fmt.Sprintf("%s/%s", wikiDir, file))
}
