package com.otofarma.telehealth.data

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class ApiException(val code: Int, message: String) : Exception(message)

class Api(private val store: SessionStore) {
    val http: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val token = store.token
            val request = if (token.isNullOrBlank()) {
                chain.request()
            } else {
                chain.request().newBuilder().header("Authorization", "Bearer $token").build()
            }
            chain.proceed(request)
        }
        .build()

    fun login(email: String, password: String): String {
        val body = JSONObject()
            .put("email", email.trim())
            .put("password", password)
            .toString()
            .toRequestBody(JSON)
        val response = post("/auth/login", body)
        val kind = response.getString("kind")
        if (kind != "device") {
            throw ApiException(403, "این برنامه فقط برای کاربر دستگاه است")
        }
        return response.getString("access_token")
    }

    fun me(): Profile = profileOf(get("/me"))

    fun updatePasswords(gmailPassword: String, appPassword: String): Profile {
        val body = JSONObject()
        if (gmailPassword.isNotBlank()) body.put("gmail_password", gmailPassword)
        if (appPassword.isNotBlank()) body.put("app_password", appPassword)
        return profileOf(patch("/me/passwords", body.toString().toRequestBody(JSON)))
    }

    private fun profileOf(response: JSONObject): Profile {
        val role = response.optString("role").ifBlank { null }
        return Profile(
            name = response.getString("name"),
            email = response.optString("gmail", response.optString("email")),
            role = role,
            gmailPassword = response.optString("gmail_password").ifBlank { null },
            appPassword = response.optString("app_password").ifBlank { null },
        )
    }

    fun apps(): List<AssignedApp> {
        val array = getArray("/apps")
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                add(
                    AssignedApp(
                        id = item.getString("id"),
                        name = item.getString("name"),
                        packageName = item.getString("package_name"),
                        version = item.getString("version"),
                        installMethod = item.getString("install_method"),
                        playUrl = item.optString("play_url").ifBlank { null },
                        hasFile = item.optBoolean("has_file"),
                        guidePublished = item.optBoolean("guide_published"),
                    ),
                )
            }
        }
    }

    fun guide(appId: String): Guide {
        val response = get("/apps/$appId/guide")
        return Guide(
            overview = blocks(response.optJSONArray("overview")),
            setup = blocks(response.optJSONArray("setup")),
            usage = blocks(response.optJSONArray("usage")),
            problems = blocks(response.optJSONArray("problems")),
        )
    }

    fun report(apps: List<AssignedApp>) {
        val items = JSONArray()
        apps.forEach { app ->
            items.put(
                JSONObject()
                    .put("app_id", app.id)
                    .put("installed", app.installed)
                    .put("version", app.installedVersion),
            )
        }
        post("/device/status", JSONObject().put("items", items).toString().toRequestBody(JSON))
    }

    fun downloadApk(appId: String, destination: File) {
        val request = Request.Builder().url(url("/apps/$appId/file")).build()
        val response = try {
            http.newCall(request).execute()
        } catch (_: IOException) {
            throw ApiException(0, "به سرور وصل نشد. آدرس و روشن بودن بکند را بررسی کنید.")
        }
        response.use {
            if (!it.isSuccessful) throw errorOf(it.code, it.body?.string().orEmpty())
            destination.parentFile?.mkdirs()
            destination.outputStream().use { output ->
                it.body?.byteStream()?.copyTo(output) ?: throw ApiException(it.code, "فایل نصب خالی است")
            }
        }
    }

    fun absolute(path: String): String {
        return if (path.startsWith("http://") || path.startsWith("https://")) path else url(path)
    }

    private fun blocks(array: JSONArray?): List<GuideBlock> {
        if (array == null) return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                val solution = if (item.isNull("solution")) null else item.optString("solution").ifBlank { null }
                add(
                    GuideBlock(
                        title = item.optString("title"),
                        body = item.optString("body"),
                        solution = solution,
                        images = media(item.optJSONArray("images")),
                        videos = media(item.optJSONArray("videos")),
                    ),
                )
            }
        }
    }

    private fun media(array: JSONArray?): List<GuideMedia> {
        if (array == null) return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                add(GuideMedia(name = item.optString("name"), url = absolute(item.getString("url"))))
            }
        }
    }

    private fun get(path: String): JSONObject {
        val request = Request.Builder().url(url(path)).build()
        return JSONObject(execute(request))
    }

    private fun getArray(path: String): JSONArray {
        val request = Request.Builder().url(url(path)).build()
        return JSONArray(execute(request))
    }

    private fun post(path: String, body: okhttp3.RequestBody): JSONObject {
        val request = Request.Builder().url(url(path)).post(body).build()
        val text = execute(request)
        if (text.isBlank()) return JSONObject()
        return JSONObject(text)
    }

    private fun patch(path: String, body: okhttp3.RequestBody): JSONObject {
        val request = Request.Builder().url(url(path)).patch(body).build()
        val text = execute(request)
        if (text.isBlank()) return JSONObject()
        return JSONObject(text)
    }

    private fun execute(request: Request): String {
        val response = try {
            http.newCall(request).execute()
        } catch (error: IOException) {
            throw ApiException(0, "به سرور وصل نشد. آدرس و روشن بودن بکند را بررسی کنید.")
        }
        response.use {
            val text = it.body?.string().orEmpty()
            if (!it.isSuccessful) throw errorOf(it.code, text)
            return text
        }
    }

    private fun url(path: String): String {
        val base = store.baseUrl.trimEnd('/')
        return if (path.startsWith("/")) base + path else "$base/$path"
    }

    private fun errorOf(code: Int, raw: String): ApiException {
        val detail = try {
            val value = JSONObject(raw).opt("detail")
            if (value is String) value else null
        } catch (_: Exception) {
            null
        }
        val message = when (detail) {
            "Wrong email or password" -> "ایمیل یا رمز نادرست است"
            "App not found" -> "اپ پیدا نشد"
            "Guide is not published yet" -> "راهنما هنوز منتشر نشده"
            "Install file is not available" -> "فایل نصب موجود نیست"
            "This app is not assigned to you" -> "این اپ به شما داده نشده"
            null -> if (code == 0) "به سرور وصل نشد" else "خطای سرور ($code)"
            else -> detail
        }
        return ApiException(code, message)
    }

    private companion object {
        val JSON = "application/json; charset=utf-8".toMediaType()
    }
}
