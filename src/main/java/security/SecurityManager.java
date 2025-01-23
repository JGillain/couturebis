package security;

import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.PasswordMatcher;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UsernamePasswordToken;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

public class SecurityManager {
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SecurityManager.class);

    /**
     * Process to login boolean.
     *
     * @param username   the username
     * @param password   the password
     * @param rememberMe the remember me
     *
     * @return the boolean
     */
    public static boolean processToLogin(String username, String password, boolean rememberMe) {



        log.debug("1");
        Subject subject = SecurityUtils.getSubject();
        log.debug("2");
        log.debug("user : " +username);
        log.debug("password : " +password);
        UsernamePasswordToken token = new UsernamePasswordToken(username, password, rememberMe);
        log.debug("3");
        try {
            subject.login(token);
            log.debug("4");
            return true;

        } catch (UnknownAccountException| IncorrectCredentialsException ex) {
            log.error(ex.getMessage(), ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Identifiants incorrects" ,
                    "Vous avez probablement introduit un mauvais username, mot de passe ou les deux"));
        } catch (LockedAccountException ex) {
            log.error(ex.getMessage(), ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Error", "Contact admin"));
        } catch (ExcessiveAttemptsException ex) {
            log.error(ex.getMessage(), ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Unknown error", "Contact administrator"));
        } finally {
            token.clear();
        }

        return false;
    }

    /**
     * Process to logout boolean.
     *
     * @return the boolean
     */
    public static boolean processToLogout() {
        Subject subject = SecurityUtils.getSubject();

        if (subject.isAuthenticated()) {
            subject.getSession().stop();
            return true;
        }

        return false;
    }

    /**
     * User is logged boolean.
     *
     * @return the boolean
     */
    public static boolean userIsLogged() {
        return SecurityUtils.getSubject().isAuthenticated();
    }

    /**
     * User is remembered string.
     *
     * @return the string
     */
    public static String userIsRemembered() {
        Subject subject = SecurityUtils.getSubject();

        if (subject.isRemembered())
            return (String) subject.getPrincipal();

        return null;
    }

    /**
     * Gets session attribute.
     *
     * @param name the name
     *
     * @return the session attribute
     */
    public static Object getSessionAttribute(String name) {
        Session session = SecurityUtils.getSubject().getSession();

        Object attribute = session.getAttribute(name);

        if (attribute == null)
            log.debug("No such attribute named : " + name + "find in session : " + session.getId());
        else
            log.debug(name + " retrieve from session : " + session.getId());

        return attribute;
    }

    /**
     * Save attribute in session.
     *
     * @param name  the name
     * @param value the value
     */
    public static void saveAttributeInSession(String name, Object value) {
        Session session = SecurityUtils.getSubject().getSession();

        session.setAttribute(name, value);

        log.debug("Attribute " + name + " with value = " + value + " save in session : " + session.getId() + " with time-out: " + session.getTimeout());
    }

    /**
     * Encrypt password string.
     *
     * @param password the password
     *
     * @return the string
     */
    public static String encryptPassword(String password) {
        PasswordMatcher matcher = new PasswordMatcher();

        return matcher.getPasswordService().encryptPassword(password);
    }

    public static Boolean PasswordMatch(Object NewPassword, String OldPassword)
    {
        PasswordMatcher matcher = new PasswordMatcher();

        return matcher.getPasswordService().passwordsMatch(NewPassword, OldPassword);

    }
}