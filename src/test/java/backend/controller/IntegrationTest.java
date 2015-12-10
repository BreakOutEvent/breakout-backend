package backend.controller;

import backend.TestBackendConfiguration;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestBackendConfiguration.class)
@WebAppConfiguration
@org.springframework.boot.test.IntegrationTest("server.port:0")
public abstract class IntegrationTest {

    @Autowired
    private WebApplicationContext context;
    protected MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    public MockHttpServletRequestBuilder post(String path, String json) {
        return MockMvcRequestBuilders.post(path)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(json);
    }

    public MockHttpServletRequestBuilder put(String path, String json) {
        return MockMvcRequestBuilders.put(path)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(json);
    }

    public MockHttpServletRequestBuilder get(String path, String json) {
        return MockMvcRequestBuilders.get(path)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(json);
    }
}

