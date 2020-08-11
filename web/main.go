package main

import (
	"encoding/json"
	"fmt"
	"github.com/gorilla/mux"
	"io/ioutil"
	"log"
	"net/http"
	"os"
)

var mainDir string

func main() {
	args := os.Args
	if len(args) >= 2 {
		mainDir = args[1]
	}

	router := mux.NewRouter()
	router.Methods("GET")
	apiRouter := router.PathPrefix("/api").Subrouter()
	apiRouter.HandleFunc("/", ListVersionsHandler)
	apiRouter.HandleFunc("/{version}/", ListVersionFilesHandler)
	apiRouter.HandleFunc("/{version}/{file}", DownloadFileHandler)

	log.Println("started listening!")

	err := http.ListenAndServe("0.0.0.0:8080", apiRouter)
	if err != nil {
		log.Fatalf("Error starting webserver: %s", err)
	}
}

func ListVersionsHandler(w http.ResponseWriter, r *http.Request) {
	// read out all folders in the main directory (all versions)
	fileInfos, err := ioutil.ReadDir(mainDir)
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

	json.NewEncoder(w).Encode(allDirs)
}

func ListVersionFilesHandler(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	version := vars["version"]
	fileInfos, err := ioutil.ReadDir(fmt.Sprintf("%s/%s", mainDir, version))
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

func DownloadFileHandler(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	version := vars["version"]
	file := vars["file"]
	http.ServeFile(w, r, fmt.Sprintf("%s/%s/%s", mainDir, version, file))
}
