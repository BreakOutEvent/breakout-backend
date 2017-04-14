package backend.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service

@Service
class ConfigurationServiceImpl : ConfigurationService {

    private val environment: Environment
    private val systemWrapper: SystemWrapper

    @Autowired
    constructor(environment: Environment, systemWrapper: SystemWrapper) {
        this.environment = environment
        this.systemWrapper = systemWrapper
    }

    override fun get(key: String): String? {
        val envKey = propertiesToEnv(key)
        return systemWrapper.getenv(envKey) ?: environment.getProperty(key)
    }

    override fun getRequired(key: String): String {
        return this.get(key) ?: throw Exception("$key neither found in properties nor environment")
    }

    private fun propertiesToEnv(properties: String): String {
        return properties.slice(4..properties.length - 1).replace(".", "_").toUpperCase()
    }
}

@Service
class SystemWrapper {
    fun getenv(key: String): String? = System.getenv(key)
}
