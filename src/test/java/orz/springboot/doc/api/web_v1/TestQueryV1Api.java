package orz.springboot.doc.api.web_v1;

import lombok.Data;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import orz.springboot.web.annotation.OrzWebApi;

@OrzWebApi(domain = "Test", action = "Query", variant = 1, query = true)
public class TestQueryV1Api {
    public TestQueryV1ApiRsp request(@Validated @RequestBody TestQueryV1ApiReq req) {
        return new TestQueryV1ApiRsp();
    }

    @Data
    public static class TestQueryV1ApiReq {
    }

    @Data
    public static class TestQueryV1ApiRsp {
    }
}
