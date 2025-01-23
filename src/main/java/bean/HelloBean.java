package bean;

import javax.inject.Named;

@Named
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