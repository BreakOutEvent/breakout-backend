package backend.services

interface ConfigurationService {
    fun get(key: String): String?
    fun getRequired(key: String): String
}
