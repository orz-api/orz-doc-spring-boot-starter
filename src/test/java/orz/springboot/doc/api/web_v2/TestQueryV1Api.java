package orz.springboot.doc.api.web_v2;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import orz.springboot.doc.annotation.OrzExternalWebError;
import orz.springboot.web.OrzWebException;
import orz.springboot.web.annotation.OrzWebApi;
import orz.springboot.web.annotation.OrzWebError;

@OrzWebApi(domain = "Test", action = "Query", variant = 1, query = true)
public class TestQueryV1Api {
    @OrzWebError(code = "1001", reason = "field1 test error1")
    @OrzWebError(code = "1002", reason = "field1 test error2")
    @OrzExternalWebError(code = "1003", description = "field1 test external error3")
    public TestQueryV1ApiRsp request(@Validated @RequestBody TestQueryV1ApiReq req) {
        if (req.getField1() == 0) {
            throw new OrzWebException("1001");
        } else if (req.getField1() == 1) {
            throw new OrzWebException("1002");
        } else if (req.getField1() == 2) {
            throw new OrzWebException("1003");
        }
        return new TestQueryV1ApiRsp(req.getField1());
    }

    @Data
    public static class TestQueryV1ApiReq {
        private long field1;
        private String field2;
    }

    @Data
    @AllArgsConstructor
    public static class TestQueryV1ApiRsp {
        private long field2;
    }
}
