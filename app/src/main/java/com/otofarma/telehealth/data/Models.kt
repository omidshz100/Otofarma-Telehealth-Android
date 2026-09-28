package com.otofarma.telehealth.data

data class Profile(
    val name: String,
    val email: String,
    val role: String?,
    val gmailPassword: String?,
    val appPassword: String?,
)

data class AssignedApp(
    val id: String,
    val name: String,
    val packageName: String,
    val version: String,
    val installMethod: String,
    val playUrl: String?,
    val hasFile: Boolean,
    val guidePublished: Boolean,
    val installed: Boolean = false,
    val installedVersion: String? = null,
)

data class GuideMedia(
    val name: String,
    val url: String,
)

data class GuideBlock(
    val title: String,
    val body: String,
    val solution: String?,
    val images: List<GuideMedia>,
    val videos: List<GuideMedia>,
)

data class Guide(
    val overview: List<GuideBlock>,
    val setup: List<GuideBlock>,
    val usage: List<GuideBlock>,
    val problems: List<GuideBlock>,
)
