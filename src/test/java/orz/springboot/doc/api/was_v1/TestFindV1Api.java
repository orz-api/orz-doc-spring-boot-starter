package orz.springboot.doc.api.was_v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import orz.springboot.doc.annotation.OrzExternalWebError;
import orz.springboot.web.OrzWebApiException;
import orz.springboot.web.annotation.OrzWebApi;
import orz.springboot.web.annotation.OrzWebError;

@OrzWebApi(domain = "Test", action = "Find", variant = 1)
public class TestFindV1Api {
    @OrzWebError(code = "1001", reason = "field1 test error1")
    @OrzWebError(code = "1002", reason = "field1 test error2")
    @OrzExternalWebError(code = "1003", description = "field1 test external error3")
    public TestFindV1ApiRsp request(@Validated @RequestBody TestFindV1ApiReq req) {
        if (req.getField1() == 0) {
            throw new OrzWebApiException("1001");
        } else if (req.getField1() == 1) {
            throw new OrzWebApiException("1002");
        } else if (req.getField1() == 2) {
            throw new OrzWebApiException("1003");
        }
        return new TestFindV1ApiRsp(req.getField1());
    }

    @Data
    public static class TestFindV1ApiReq {
        private long field1;
        private String field2;
    }

    @Data
    @AllArgsConstructor
    public static class TestFindV1ApiRsp {
        private long field2;
    }
}
