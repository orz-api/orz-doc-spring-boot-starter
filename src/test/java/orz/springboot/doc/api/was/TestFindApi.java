package orz.springboot.doc.api.was;

import lombok.Data;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import orz.springboot.web.annotation.OrzWebApi;

@OrzWebApi(domain = "test", action = "find")
public class TestFindApi {
    public TestFindRsp request(@Validated @RequestBody TestFindReq req) {
        return new TestFindRsp();
    }

    @Data
    public static class TestFindReq {
    }

    @Data
    public static class TestFindRsp {
    }
}
