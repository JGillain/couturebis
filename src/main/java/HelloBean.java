
import javax.faces.bean.ManagedBean;

@ManagedBean
public class HelloBean {
    private String message = "Hello, JSF!";

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String sayHello() {
        return "hello";
    }
}