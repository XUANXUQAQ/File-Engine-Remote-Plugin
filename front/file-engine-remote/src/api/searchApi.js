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
    download(filePath, abortController) {
        const url = `${baseUrl}/download`;
        return http.get(url, {
            signal: abortController.signal,
            params: {
                filePath
            },
            responseType: 'blob'
        })
    }
}

export default searchApi;