package com.loveever.app.data

/**
 * 全局 token 内存持有者：Coil 图片加载与语音播放需要携带鉴权头。
 * 登录/注册/配对/恢复会话时由 ViewModel 写入，登出时清空。
 */
object TokenHolder {
    @Volatile
    var token: String? = null
}
