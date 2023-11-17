package orz.springboot.doc.api;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import orz.springboot.doc.annotation.OrzExternalWebError;
import orz.springboot.web.annotation.OrzWebApi;
import orz.springboot.web.annotation.OrzWebError;
import orz.springboot.web.exception.OrzWebException;

@OrzWebApi("/test/find")
public class TestFindApi {
    @OrzWebError(code = "1001", reason = "field1 test error1: %s")
    @OrzWebError(code = "1002", reason = "field1 test error2")
    @OrzExternalWebError(code = "1003", description = "field1 test external error3")
    @PostMapping
    public TestFindRsp testFind(@Valid @RequestBody TestFindReq req, @RequestParam long query1, @RequestParam Long query2) {
        if (req.getField1() == 0) {
            throw new OrzWebException("1001", "test");
        } else if (req.getField1() == 1) {
            throw new OrzWebException("1002");
        } else if (req.getField1() == 2) {
            throw new OrzWebException("1003");
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
