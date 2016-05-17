package backend.testHelper

import backend.Integration.getTokens
import backend.Integration.toJsonString
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder

const val APPLICATION_JSON_UTF_8 = "application/json;charset=UTF-8"

fun MockHttpServletRequestBuilder.asUser(mockMvc: MockMvc, email: String, password: String): MockHttpServletRequestBuilder {
    val tokens = getTokens(mockMvc, email, password)
    this.header("Authorization", "Bearer ${tokens.first}")
    return this
}

fun MockHttpServletRequestBuilder.json(json: String): MockHttpServletRequestBuilder {
    this.contentType(APPLICATION_JSON_UTF_8)
    this.content(json)
    return this
}

fun MockHttpServletRequestBuilder.json(body: Map<String, Any?>): MockHttpServletRequestBuilder {
    return this.json(body.toJsonString())
}

fun MockHttpServletRequestBuilder.json(body: List<Any?>): MockHttpServletRequestBuilder {
    return this.json(body.toJsonString())
}

