import { fetchEventSource } from "@microsoft/fetch-event-source";
import { BASE_URL } from "@/http/config.ts";
import service from "@/http";

class FatalError extends Error {}
type ResultCallBack = (e: any | null) => void;

const BaseUrl = BASE_URL;

export const postStreamChat = (
    author: string,
    onMessage: ResultCallBack,
    onError: ResultCallBack,
    onClose: ResultCallBack
) => {
    const ctrl = new AbortController();
    fetchEventSource(BaseUrl + "/post-chat", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            author: author,
        }),
        signal: ctrl.signal,
        onmessage: onMessage,
        onerror: (err: any) => {
            onError(err);
        },
        onclose: () => {
            onClose(null);
        },
        onopen: async (response: any) => {
            if (response.ok) {
                return;
            } else if (
                response.status >= 400 &&
                response.status < 500 &&
                response.status !== 429
            ) {
                onError(new Error(`HTTP ${response.status}: ${response.statusText}`));
                return new Promise(() => {});
            } else {
                onError(new Error(`HTTP ${response.status}: ${response.statusText}`));
                return new Promise(() => {});
            }
        },
    });
};

/**
 * 通用流式对话
 *
 * @param message   用户消息
 * @param url       接口地址
 * @param onMessage 收到消息回调
 * @param onError   错误回调
 * @param onClose   关闭回调
 * @param sources   数据源文件名列表（可选）
 * @param model     大模型名称（可选）
 */
export const getStreamChat = (
    message: string,
    url: string = "/chat/stream",
    onMessage: ResultCallBack,
    onError: ResultCallBack,
    onClose: ResultCallBack,
    sources?: string[],
    model?: string
) => {
    const ctrl = new AbortController();

    // 统一使用 POST 请求发送数据
    const formData = new FormData();
    formData.append('message', message);

    // 可选：数据源
    if (sources && sources.length > 0) {
        sources.forEach(source => {
            formData.append('sources', source);
        });
    }

    // 可选：大模型
    if (model) {
        formData.append('model', model);
    }

    fetchEventSource(service.defaults.baseURL + url, {
        method: "POST",
        headers: {
            "Authorization": `Bearer ${localStorage.getItem("token") || ""}`
        },
        body: formData,
        signal: ctrl.signal,
        onmessage: onMessage,
        onerror: (err: any) => {
            onError(err);
        },
        onclose: () => {
            onClose(null);
        },
        onopen: async (response: any) => {
            if (response.ok) {
                return;
            }
            else if (response.status === 401) {
                // 处理 401 未授权错误
                import('@/api/authUtils').then(module => {
                    module.default();
                });
            }
            else {
                onError(new Error(`HTTP ${response.status}: ${response.statusText}`));
                return new Promise(() => {});
            }
        },
    });
};

/**
 * 专门用于 POST 请求的流式 RAG 对话
 *
 * @param message   用户消息
 * @param sources   数据源文件名列表
 * @param url       接口地址
 * @param onMessage 收到消息回调
 * @param onError   错误回调
 * @param onClose   关闭回调
 * @param model     大模型名称（可选）
 */
export const postStreamChatWithSources = (
    message: string,
    sources: string[],
    url: string = "/ai/rag",
    onMessage: ResultCallBack,
    onError: ResultCallBack,
    onClose: ResultCallBack,
    model?: string
) => {
    const ctrl = new AbortController();

    const formData = new FormData();
    formData.append('message', message);
    sources.forEach(source => {
        formData.append('sources', source);
    });
    if (model) {
        formData.append('model', model);
    }

    fetchEventSource(service.defaults.baseURL + url, {
        method: "POST",
        headers: {
            "Authorization": `Bearer ${localStorage.getItem("token") || ""}`
        },
        body: formData,
        signal: ctrl.signal,
        onmessage: onMessage,
        onerror: (err: any) => {
            onError(err);
        },
        onclose: () => {
            onClose(null);
        },
        onopen: async (response: any) => {
            if (response.ok) {
                return;
            }
            else if (response.status === 401) {
                import('@/api/authUtils').then(module => {
                    module.default();
                });
            }
            else {
                onError(new Error(`HTTP ${response.status}: ${response.statusText}`));
                return new Promise(() => {});
            }
        },
    });
};