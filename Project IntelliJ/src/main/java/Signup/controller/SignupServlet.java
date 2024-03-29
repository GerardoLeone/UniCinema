package Signup.controller;

import Signup.service.SignupService;
import Signup.service.SignupServiceMethods;
import model.bean.Account;
import utils.Alert;
import utils.Controller;
import utils.ErrorHandler;
import utils.InvalidRequestException;
import utils.validator.AccountValidator;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "SignupServlet", value = "/signup")
public class SignupServlet extends Controller implements ErrorHandler {

    /**
     * {@link SignupService}
     */
    SignupService signupService;
    /**
     * {@link AccountValidator}
     */
    AccountValidator accountValidator;

    public SignupServlet() throws SQLException {
        this.signupService = new SignupServiceMethods();
        this.accountValidator = new AccountValidator();
    }

    /**
     * Metodo che setta il SignupService con la sua implementazone
     * @param signupService
     */
    public void setSignupService(SignupService signupService) {
        this.signupService = signupService;
    }

    /**
     * Metodo che setta l'AccountValidator con la sua implementazione
     * @param accountValidator
     */
    public void setAccountValidator(AccountValidator accountValidator) {
        this.accountValidator = accountValidator;
    }

    /**
     * Implementa le funzionalità svolte durante una chiamata di tipo GET
     * @param request oggetto rappresentante la chiamata Http request
     * @param response oggetto rappresentante la chiamata Http response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher(view("site/account/signup")).forward(request, response);
    }

    /**
     * Implementa le funzionalità svolte durante una chiamata di tipo POST (nello specifico, le funzionalità di Signup)
     * @param request oggetto rappresentante la chiamata Http request
     * @param response oggetto rappresentante la chiamata Http response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {

            /**
             * Rappresenta la sessione attuale {@link HttpSession}.
             */
            HttpSession session = request.getSession();

            request.setAttribute("back", view("site/account/signup"));
            validate(accountValidator.validateSignup(request, true));
            Account tmpAccountSignup = new Account();
            tmpAccountSignup.setEmail(request.getParameter("email"));
            tmpAccountSignup.setPswrd(request.getParameter("password"));
            tmpAccountSignup.setFirstname(request.getParameter("firstname"));
            tmpAccountSignup.setLastname(request.getParameter("lastname"));
            tmpAccountSignup.setAdministrator(false);
            if(signupService.signup(tmpAccountSignup)) {
                session.setAttribute("accountSession", tmpAccountSignup);
                response.sendRedirect(getServletContext().getContextPath()+"/pages");
            } else {
                request.setAttribute("alert", new Alert(List.of("Errore nell'inserimento dei dati"), "danger"));
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

                throw new InvalidRequestException("Errore nell'inserimento dei dati",
                        List.of("Errore nell'inserimento dei dati"), HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (SQLException | NoSuchAlgorithmException ex) {
            log(ex.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        } catch (InvalidRequestException e) {
            log(e.getMessage());
            if(isAjax(request))
                e.handleAjax(response);
            else
                e.handle(request, response);
        }
    }
}
