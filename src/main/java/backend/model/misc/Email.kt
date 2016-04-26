package backend.model.misc;

import backend.model.BasicEntity
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Entity

@Entity
class Email : BasicEntity {

    constructor()

    constructor(to: List<EmailAddress>,
                subject: String,
                body: String,
                files: List<Url> = ArrayList(),
                bcc: List<EmailAddress> = ArrayList(),
                campaignCode: String? = null,
                buttonText: String? = null,
                buttonUrl: String? = null) {
        this.to = to
        this.subject = subject
        this.body = body
        this.files = files
        this.bcc = bcc
        this.campaignCode = campaignCode
        this.buttonText = buttonText
        this.buttonUrl = buttonUrl
    }

    @JsonIgnore
    var isSent: Boolean = false

    @JsonIgnore
    @ElementCollection
    lateinit var to: List<EmailAddress>

    @JsonIgnore
    @ElementCollection
    lateinit var bcc: List<EmailAddress>

    @JsonIgnore
    @ElementCollection
    lateinit var files: List<Url>

    // This is a workAround because I don't know  how to serialize the List<EmailAddress>
    // to an [] of strings using jackson
    @JsonProperty("tos")
    fun toAsString(): List<String> {
        return to.map { it.toString() }
    }

    @JsonProperty("bccs")
    fun bccAsString(): List<String> {
        return bcc.map { it.toString() }
    }

    @JsonProperty("files")
    fun filesAsString(): List<String> {
        return files.map { it.toString() }
    }

    lateinit var subject: String

    var buttonText: String? = null

    var buttonUrl: String? = null

    @JsonProperty("html")
    @Column(columnDefinition = "TEXT")
    lateinit var body: String

    @JsonProperty("campaign_code")
    var campaignCode: String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Email

        if (to != other.to) return false
        if (bcc != other.bcc) return false
        if (files != other.files) return false
        if (subject != other.subject) return false
        if (buttonText != other.buttonText) return false
        if (buttonUrl != other.buttonUrl) return false
        if (body != other.body) return false
        if (campaignCode != other.campaignCode) return false

        return true
    }

    override fun hashCode(): Int {
        var result = to.hashCode()
        result += 31 * result + bcc.hashCode()
        result += 31 * result + files.hashCode()
        result += 31 * result + subject.hashCode()
        result += 31 * result + (buttonText?.hashCode() ?: 0)
        result += 31 * result + (buttonUrl?.hashCode() ?: 0)
        result += 31 * result + body.hashCode()
        result += 31 * result + (campaignCode?.hashCode() ?: 0)
        return result
    }
}