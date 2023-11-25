package orz.springboot.doc.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import orz.springboot.doc.annotation.OrzExternalWebError;
import orz.springboot.web.annotation.OrzWebApi;
import orz.springboot.web.annotation.OrzWebError;
import orz.springboot.web.exception.OrzWebApiException;

@OrzWebApi(domain = "test", action = "find")
public class TestFindApi {
    @OrzWebError(code = "1001", reason = "field1 test error1")
    @OrzWebError(code = "1002", reason = "field1 test error2")
    @OrzExternalWebError(code = "1003", description = "field1 test external error3")
    public TestFindRsp request(@Validated @RequestBody TestFindReq req) {
        if (req.getField1() == 0) {
            throw new OrzWebApiException("1001");
        } else if (req.getField1() == 1) {
            throw new OrzWebApiException("1002");
        } else if (req.getField1() == 2) {
            throw new OrzWebApiException("1003");
        }
        return new TestFindRsp(req.getField1());
    }

    @Data
    public static class TestFindReq {
        private long field1;
        private String field2;
    }

    @Data
    @AllArgsConstructor
    public static class TestFindRsp {
        private long field2;
    }
}
