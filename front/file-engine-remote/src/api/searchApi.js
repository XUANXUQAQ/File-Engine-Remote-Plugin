import http from "./axios.config";

const baseUrl = process.env.NODE_ENV == 'development' ? 'http://localhost:23333' : '';

const searchApi = {
    search(inputText) {
        const url = `${baseUrl}/search`;
        return http.post(url, null, {
            params: {
                inputText
            }
        });
    },
    getResults(pageNum, pageSize) {
        const url = `${baseUrl}/results`;
        return http.get(url, {
            params: {
                pageNum,
                pageSize
            }
        });
    },
    download(fileName, filePath, isDir) {
        let downloadFileName = fileName;
        if (isDir) {
            downloadFileName += ".zip";
        }
        const url = `${baseUrl}/${downloadFileName}?filePath=${filePath}`;
        window.location.href = url;
    }
    // download(filePath, abortController) {
    //     const url = `${baseUrl}/download`;
    //     return http.get(url, {
    //         signal: abortController.signal,
    //         params: {
    //             filePath
    //         },
    //         responseType: 'blob'
    //     })
    // }
}

export default searchApi;