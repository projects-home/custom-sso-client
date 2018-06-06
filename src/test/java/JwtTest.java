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
        String jwt = "eyJhbGciOiJub25lIn0=.eyJpc3N1ZVRpbWUiOjE1MjgyNTQ5NTM5OTQsImV4cGlyYXRpb25UaW1lIjoxNTI4MjgzNzcyMjk4LCJ1c2VyTmFtZSI6IueOi-awuOaWsCIsInVzZXJJZCI6ImRlNjNlOGE5YTMxIiwianRpIjoiVEdULTUtTnJKOW15WWt0TW5saEIwZlg1bHVOcTV1VGdtZk41OUhaMjgyMUFXaHZtdUxRdWdlSmE3R3RkZEx6SWR4MjV0VEw2OC13YW5neW9uZ3hpbiIsInVzZXJDb2RlIjoid2FuZ3lvbmd4aW4ifQ==";
        String[] str = jwt.split("\\.");

        String payload = new String(Base64.getUrlDecoder().decode(str[1]),Charset.forName("utf-8"));
        System.out.println(payload);
        System.out.println(JSONObjectUtils.parse(payload).getAsString("jti"));
    }
}
