package orz.springboot.doc.api.wau;

import lombok.Data;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import orz.springboot.web.annotation.OrzWebApi;

@OrzWebApi(domain = "Test", action = "Find", variant = 1)
public class TestFindV1Api {
    public TestFindV1ApiRsp request(@Validated @RequestBody TestFindV1ApiReq req) {
        return new TestFindV1ApiRsp();
    }

    @Data
    public static class TestFindV1ApiReq {
    }

    @Data
    public static class TestFindV1ApiRsp {
    }
}
