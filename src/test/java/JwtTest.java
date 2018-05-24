import com.nimbusds.jose.util.JSONObjectUtils;
import org.junit.Test;

import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Base64;

/**
 * @author wangyongxin
 * @createAt 2018-05-23 下午3:06
 **/
public class JwtTest {

    @Test
    public void test() throws ParseException {
        String jwt = "eyJhbGciOiJub25lIn0.eyJzdWIiOiJhZG1pbiIsImRpc3BsYXlOYW1lIjoiYWRtaW4iLCJzdWNjZXNzZnVsQXV0aGVudGljYXRpb25IYW5kbGVycyI6WyJDdXN0b21BdXRoZW50aWNhdGlvbkhhbmRsZXIiXSwiaXNzIjoiaHR0cHM6XC9cL2xvY2FsaG9zdDo4MDgwXC9jYXMiLCJhY2Nlc3NUb2tlbiI6IjBEMDcxMjIwMzM5QzMxMThGQzgyRjg4RTVDNTNBRUIwIiwiY3JlZGVudGlhbFR5cGUiOiJVc2VybmFtZVBhc3N3b3JkQ3JlZGVudGlhbCIsImF1ZCI6Imh0dHBzOlwvXC9sb2NhbGhvc3Q6ODA4MFwvY2FzIiwiYXV0aGVudGljYXRpb25NZXRob2QiOiJDdXN0b21BdXRoZW50aWNhdGlvbkhhbmRsZXIiLCJpZCI6MSwiZXhwIjoxNTI3MDkzOTA2LCJpYXQiOjE1MjcwNjUxMDYsImp0aSI6IlRHVC0xLUtudndVeU9aT284R1hEYVlRb2JXbWhxbURka1pYM3dlR3lpeHIzLXFaVnYyTVRSTGlRM3Bmc01jeTNBM0hsWXlld0Etd2FuZ3lvbmd4aW4iLCJ1c2VybmFtZSI6ImFkbWluIn0.";
        String[] str = jwt.split("\\.");

        String payload = new String(Base64.getDecoder().decode(str[1]),Charset.forName("utf-8"));
        System.out.println(payload);
        System.out.println(JSONObjectUtils.parse(payload).getAsString("jti"));
    }
}
