import axios from 'axios';

// 创建axios实例，最多60秒的请求时延
const http = axios.create({
  timeout: 1000 * 60,
});

/**
 * 请求拦截器
 */
http.interceptors.request.use(
  (config) => {
    // 请求开始时，启动加载条
    return config;
  },
  (error) => {
    Promise.reject(error);
  },
);

/**
 * 响应拦截器
 */
http.interceptors.response.use(
  (config) => {
    if (config.status === 200) {
      const {
        data
      } = config;
      const {
        code
      } = data;
      if (code === undefined || code === 20000) {
        return Promise.resolve(data);
      }
    }
    return Promise.reject(config.data);
  },
  (error) => {
    const {
      response
    } = error;

    if (response) {
      // 请求已发出，但是不在2xx的范围
      return Promise.reject(response);
    }
    return Promise.reject(error);
  },
);

export default http;