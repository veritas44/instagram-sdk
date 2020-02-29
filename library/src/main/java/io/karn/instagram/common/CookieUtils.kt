package io.karn.instagram.common

import khttp.structures.cookie.Cookie
import khttp.structures.cookie.CookieJar
import org.json.JSONArray

object CookieUtils {

    fun serializeToJson(cookieJar: CookieJar): JSONArray {
        val json = JSONArray()

        cookieJar.forEach { json.put(it.toString()) }

        return json
    }

    fun deserializeFromJson(cookieJson: JSONArray?, initial: CookieJar = CookieJar()): CookieJar {
        // Validate argument
        cookieJson ?: return initial

        (0 until cookieJson.length()).forEach { initial.setCookie(Cookie(cookieJson.getString(it))) }

        return initial
    }

    fun buildCookieHeader(cookieJar: CookieJar): String {
        val sb = StringBuilder()

        cookieJar.keys.forEach { cookieJar.getCookie(it) }

        return cookieJar.keys.map { Pair(it, cookieJar.getCookie(it)?.value?.toString()) }
                .filter { !it.second.isNullOrBlank() }
                .joinToString("; ") { (c, v) -> "$c=$v" }
                .trim()
//
//        cookieJar.getCookie("mid")?.value?.toString()?.takeUnless { it.isBlank() }?.run { sb.append("mid=$this; ") }
//        cookieJar.getCookie("csrftoken")?.value?.toString()?.takeUnless { it.isBlank() }?.run { sb.append("csrftoken=$this; ") }
//        cookieJar.getCookie("rur")?.value?.toString()?.takeUnless { it.isBlank() }?.run { sb.append("rur=$this; ") }
//        cookieJar.getCookie("urlgen")?.value?.toString()?.takeUnless { it.isBlank() }?.run { sb.append("urlgen={\\ ") }
//
//        return sb.trim().trimEnd(';').toString()
    }
}
