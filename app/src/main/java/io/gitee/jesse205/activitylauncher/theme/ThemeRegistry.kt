package io.gitee.jesse205.activitylauncher.theme

object ThemeRegistry {
    private val themes = mutableListOf<AppTheme>()
    val availableThemes: List<AppTheme> = themes

    fun registerTheme(config: AppTheme) {
        themes.add(config)
    }

    fun getThemeById(id: String): AppTheme? {
        return themes.find { it.id == id }
    }

    fun getDefaultTheme(): AppTheme {
        return themes.firstOrNull() ?: throw IllegalStateException("No compatible theme available")
    }

    fun isThemeCompatible(id: String): Boolean {
        return getThemeById(id) != null
    }
}