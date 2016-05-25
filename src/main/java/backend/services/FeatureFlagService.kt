package backend.services

import backend.model.BasicEntity
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import javax.persistence.Column
import javax.persistence.Entity

interface FeatureFlagService {
    fun isEnabled(key: String): Boolean
}

@Service
class FeatureFlagServiceImpl : FeatureFlagService {

    private var repository: FeatureRepository
    private var logger: Logger

    @Autowired
    constructor(featureRepository: FeatureRepository) {
        this.repository = featureRepository
        this.logger = LoggerFactory.getLogger(FeatureFlagServiceImpl::class.java)
    }

    override fun isEnabled(key: String): Boolean {
        try {
            val feature = repository.findByName(key) ?: return true
            return feature.isEnabled
        } catch (e: Exception) {
            logger.warn("An exception occured while checking feature '$key'. Setting feature '$key' to enabled (default).")
            e.printStackTrace()
            return true
        }
    }
}

@Entity
class Feature : BasicEntity {

    @Column(unique = true)
    lateinit var name: String

    var isEnabled: Boolean = false

    /**
     * private no-args constructor for hibernate / jpa
     * */
    private constructor()

    constructor(name: String, isEnabled: Boolean) : super() {
        this.name = name
        this.isEnabled = isEnabled
    }
}

@Repository
interface FeatureRepository : CrudRepository<Feature, Long> {
    fun findByName(name: String): Feature?
}
