import { KnowApi } from "./common";
import { BASE_URL } from "@/http/config";
import axios from "axios";
import { DeleteFileDto, QueryFileDto } from "./dto";
import service from "@/http";
import handleAuthError from "@/api/authUtils";

type Res = any;

const fileService = axios.create({
  baseURL: BASE_URL,
  headers: {
    "Content-Type": "multipart/form-data",
  },
});


// 创建请求拦截器
fileService.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token"); 
    if (token !== null) {
      config.headers.Authorization = "Bearer " + token;
    }
    return config;
  },
  (error) => {
    console.log(error);
    return Promise.reject(error);
  }
);

// 创建响应拦截器
fileService.interceptors.response.use(
  (res: any) => { 
    
    return res;
  },
  (error) => { 
    // 检查是否是401错误
    if (error.response && error.response.status === 401) {
      handleAuthError();
    }
    
    console.log(error);
    return Promise.reject(error);
  }
);

// 查询所有知识库接口
export const queryFileApi = async (params: QueryFileDto): Promise<Res> => {
  console.log("请求参数：", params);

  return service.get(KnowApi.QueryFile, {
    params,
  });
};

// 删除指定ID列表的知识库
export const deleteFileApi = async (params: DeleteFileDto): Promise<Res> => {
  return service.delete(KnowApi.DeleteFile, {
    params,
  });
};

/**
 * 批量下载文件（返回 ZIP 流）
 */
export const downloadBatchApi = (ids: number[]) => {
  return service.post(KnowApi.DownloadFileBatch, ids, {
    responseType: 'blob'  // 关键：接收二进制流
  })
}

/**
 * 上传文件（支持指定分片策略）
 */
export const uploadFileApi = async (files: File[], strategy: string = 'token') => {
  const formData = new FormData()
  files.forEach(file => formData.append('file', file))
  formData.append('strategy', strategy)
  return service.post('/knowledge/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 分片预览
 */
export const previewChunkApi = async (file: File, strategy: string = 'token') => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('strategy', strategy)
  return service.post('/knowledge/chunk/preview', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}