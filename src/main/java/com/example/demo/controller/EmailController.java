package com.example.demo.controller;

import com.example.demo.dao.idao.IDAOCustomer;
import com.example.demo.dao.idao.IDAOTravelAgency;
import com.example.demo.dao.idao.form.IDAOCustomerForm;
import com.example.demo.dao.idao.form.IDAOTravelAgencyForm;
import com.example.demo.entity.Customer;
import com.example.demo.entity.TravelAgency;
import com.example.demo.forms.CustomerForm;
import com.example.demo.forms.TravelAgencyForm;
import com.example.demo.gmail.MyConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Random;

@Controller
public class EmailController {

    @Autowired
    public JavaMailSender emailSender;
    @Autowired
    private IDAOCustomer<Customer> daoCustomer;
    @Autowired
    private IDAOTravelAgency<TravelAgency> daoTravelAgency;
    @Autowired
    private IDAOCustomerForm daoCustForm;
    @Autowired
    private IDAOTravelAgencyForm daoTravAgenForm;

    private static final boolean enteredCodeIsWrong = true;
    private static final boolean dontHaveTheProblem = false;
    private static final long errorValueCode = -1;
    private static final String customerCheckUrl = "/confirm.mail.customer";
    private static final String travelAgencyCheckUrl = "/confirm.mail.travel.agency";

    @RequestMapping(value= {"/confirm.mail.customer"},method = { RequestMethod.GET })
    public String sendCodeToEmailForCustomerGet(Model model, String email) {
       //  System.out.println(daoCustForm.getCode(email));
       this.sendCode(email,daoCustForm.getName(email),daoCustForm.getCode(email));//todo

        this.setAttributeCheck(model,daoCustForm.getName(email),email,customerCheckUrl,dontHaveTheProblem);

        return "checkOutEmailCodePage";
    }

   // @ResponseBody
    @RequestMapping(value= {"/confirm.mail.customer"},method = { RequestMethod.POST })
    public String sendCodeToEmailForCustomerPost(Model model,String email,Long cod) {

        if(daoCustForm.getCode(email) == ((cod == null)? errorValueCode : cod)) {
            this.daoCustomer.save(daoCustForm.getForm(email,cod).getCustomer());
            return "redirect:/login";
        }

        this.setAttributeCheck(model,daoCustForm.getName(email),email,customerCheckUrl,enteredCodeIsWrong);

        return "checkOutEmailCodePage";
    }

    @RequestMapping(value= {"/confirm.mail.travel.agency"},method = { RequestMethod.GET })
    public String sendCodeToEmailForTravelAgencyGet(Model model, String email) {
        this.sendCode(email,daoTravAgenForm.getName(email),daoTravAgenForm.getCode(email));

        this.setAttributeCheck(model,daoTravAgenForm.getName(email),email,travelAgencyCheckUrl,dontHaveTheProblem);

        return "checkOutEmailCodePage";
    }

    // @ResponseBody
    @RequestMapping(value= {"/confirm.mail.travel.agency"},method = { RequestMethod.POST })
    public String sendCodeToEmailForTravelAgencyPost(Model model,String email,Long cod) {

        if(daoTravAgenForm.getCode(email) == ((cod == null)? errorValueCode : cod)) {
            this.daoTravelAgency.save(daoTravAgenForm.getForm(email,cod).getTravelAgency());
            return "redirect:/login";
        }

        this.setAttributeCheck(model,daoCustForm.getName(email),email,travelAgencyCheckUrl,enteredCodeIsWrong);

        return "checkOutEmailCodePage";
    }

    private void sendCode(String email,String name,long cod){
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);

        message.setSubject("Sign up Email");

        message.setText(String.format("Hello %s, your email use for sign up at site \"Tangerine summer\", use this code --> %d <--.",name,cod));
        // Send Message!
        this.emailSender.send(message);
    }

    private void setAttributeCheck(Model model,String name,String email,String url,boolean error) {
        model.addAttribute("helloUser","Hello " + name + "!!!");
        model.addAttribute("yourEmail","Your email is " + email + ".");
        model.addAttribute("errorCode", error);
        model.addAttribute("cod",0l);
        model.addAttribute("email",email);
        model.addAttribute("userURL",url);
    }

}