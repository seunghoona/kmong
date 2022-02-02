package com.seunghoona.kmong;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;

import static io.restassured.RestAssured.given;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.document;
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.documentationConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AcceptanceTest {
    @LocalServerPort
    int port;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @RegisterExtension
    final RestDocumentationExtension restDocumentation = new RestDocumentationExtension(
            "build/generated-snippets");

    protected RequestSpecification spec;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {
        if (RestAssured.port == RestAssured.UNDEFINED_PORT) {
            RestAssured.port = port;
            databaseCleanUp.afterPropertiesSet();
        }
        databaseCleanUp.execute();
        this.spec = new
                RequestSpecBuilder()
                .addFilter(documentationConfiguration(restDocumentation))
                .build();
    }

    public ExtractableResponse<Response> get(String url) {
        return givenLog(url)
                .get(url)
                .then().log().all()
                .extract();
    }

    public ExtractableResponse<Response> post(String url, Object obj) {
        return givenLog(url)
                .when()
                .body(obj)
                .post(url)
                .then().log().all()
                .extract();
    }

    private RequestSpecification givenLog(String name) {
        return given(this.spec).log().all()
                .filter(document(name,
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                )).contentType(MediaType.APPLICATION_JSON_VALUE);
    }

}
