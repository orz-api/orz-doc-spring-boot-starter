package orz.springboot.doc.api.web_v3;

import lombok.Data;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import orz.springboot.web.annotation.OrzWebApi;

@OrzWebApi(domain = "Test", action = "Mutation", variant = 1)
public class TestMutationV1Api {
    public TestMutationV1ApiRsp request(@Validated @RequestBody TestMutationV1ApiReq req) {
        return new TestMutationV1ApiRsp();
    }

    @Data
    public static class TestMutationV1ApiReq {
    }

    @Data
    public static class TestMutationV1ApiRsp {
    }
}
